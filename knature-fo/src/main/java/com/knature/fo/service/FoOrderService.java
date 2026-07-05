package com.knature.fo.service;

import com.knature.common.domain.member.Member;
import com.knature.common.domain.order.*;
import com.knature.common.domain.product.Product;
import com.knature.common.domain.product.ProductOption;
import com.knature.common.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final ShopSettingRepository shopSettingRepository;

    /** 주문 항목 요청 (productId, optionId nullable, quantity) */
    public record OrderItemRequest(Long productId, Long optionId, int quantity) {}

    /**
     * 주문 생성 — 가격은 서버에서 재계산(클라이언트 금액 불신), 재고 검증·차감, 주문번호 발급.
     * 무통장입금 → 입금전 / 그 외(카드·카카오·네이버는 1차 모의결제) → 배송준비중
     */
    @Transactional
    public Order createOrder(String username, List<OrderItemRequest> items, PaymentMethod paymentMethod,
                             String receiverName, String receiverPhone, String zipcode,
                             String address, String addressDetail, String deliveryMemo) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("주문할 상품이 없습니다.");
        }
        if (receiverName == null || receiverName.isBlank() || address == null || address.isBlank()) {
            throw new IllegalArgumentException("배송지 정보를 입력해주세요.");
        }
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 1) 상품 검증 + 금액 계산 + 재고 차감
        long totalAmount = 0;
        record Line(Product product, ProductOption option, int quantity, long unitPrice) {}
        List<Line> lines = new java.util.ArrayList<>();
        for (OrderItemRequest req : items) {
            if (req.quantity() <= 0) {
                throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
            }
            Product product = productRepository.findById(req.productId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + req.productId()));
            if (!Boolean.TRUE.equals(product.getDisplayed())) {
                throw new IllegalArgumentException("판매 중이 아닌 상품입니다: " + product.getName());
            }
            int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            if (stock < req.quantity()) {
                throw new IllegalArgumentException("재고가 부족합니다: " + product.getName() + " (남은 재고 " + stock + "개)");
            }
            ProductOption option = null;
            long unitPrice = product.getDisplayPrice();
            if (req.optionId() != null) {
                option = product.getOptions().stream()
                        .filter(o -> o.getId().equals(req.optionId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("옵션을 찾을 수 없습니다."));
                unitPrice += option.getAdditionalPrice();
            }
            // 재고 차감 + 품절 자동 처리
            product.setStockQuantity(stock - req.quantity());
            product.applyStockStatusRule();

            totalAmount += unitPrice * req.quantity();
            lines.add(new Line(product, option, req.quantity(), unitPrice));
        }

        // 2) 배송비 — BO 설정(배송 정책) 연동
        long deliveryFee = calculateDeliveryFee(totalAmount);

        // 3) 주문 생성
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .member(member)
                .ordererName(member.getName())
                .ordererEmail(member.getEmail())
                .ordererPhone(member.getPhone())
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .receiverZipcode(zipcode)
                .receiverAddress(address)
                .receiverAddressDetail(addressDetail)
                .deliveryMemo(deliveryMemo)
                .totalAmount(totalAmount)
                .deliveryFee(deliveryFee)
                .paymentAmount(totalAmount + deliveryFee)
                .paymentMethod(paymentMethod)
                .build();
        // 무통장은 입금 대기, 그 외는 결제 완료로 간주(1차 모의결제) → 배송준비중
        if (paymentMethod != PaymentMethod.BANK_TRANSFER) {
            order.setStatus(OrderStatus.PREPARING);
        }
        for (Line line : lines) {
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(line.product())
                    .productOption(line.option())
                    .productName(line.product().getName())
                    .optionName(line.option() != null ? line.option().getName() : null)
                    .quantity(line.quantity())
                    .price(line.unitPrice())
                    .build();
            order.getItems().add(item);
        }
        Order saved = orderRepository.save(order);
        log.info("FO 주문 생성: {} ({}원, {})", saved.getOrderNumber(), saved.getPaymentAmount(), paymentMethod);
        return saved;
    }

    /** 주문번호 규칙: ORD-yyyyMMdd-일련번호(3자리) */
    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        long todayCount = orderRepository.countByOrderNumberStartingWith("ORD-" + date + "-");
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = String.format("ORD-%s-%03d", date, todayCount + 1 + attempt);
            if (orderRepository.findByOrderNumber(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("주문번호 생성에 실패했습니다. 다시 시도해주세요.");
    }

    private long calculateDeliveryFee(long totalAmount) {
        long baseFee = settingLong("delivery.baseFee", 0);
        long freeThreshold = settingLong("delivery.freeThreshold", 0);
        if (freeThreshold > 0 && totalAmount >= freeThreshold) return 0;
        return baseFee;
    }

    private long settingLong(String key, long defaultValue) {
        return shopSettingRepository.findBySettingKey(key)
                .map(s -> {
                    try { return Long.parseLong(s.getSettingValue().trim()); }
                    catch (Exception e) { return defaultValue; }
                })
                .orElse(defaultValue);
    }

    // ===== 조회 (본인 주문만) =====

    public List<Order> getMyOrders(String username) {
        Member member = memberRepository.findByUsername(username).orElseThrow();
        return orderRepository.findByMemberIdOrderByIdDesc(member.getId());
    }

    public Order getMyOrder(String username, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        if (order.getMember() == null || !order.getMember().getUsername().equals(username)) {
            throw new IllegalArgumentException("본인의 주문만 조회할 수 있습니다.");
        }
        return order;
    }

    /** 마이쇼핑 홈 — 상태별 건수 요약 */
    public Map<String, Long> getMyOrderSummary(String username) {
        List<Order> orders = getMyOrders(username);
        Map<String, Long> summary = new LinkedHashMap<>();
        for (OrderStatus s : List.of(OrderStatus.PENDING_PAYMENT, OrderStatus.PREPARING, OrderStatus.SHIPPING,
                OrderStatus.DELIVERED, OrderStatus.CANCEL_REQUESTED, OrderStatus.EXCHANGE_REQUESTED,
                OrderStatus.RETURN_REQUESTED)) {
            summary.put(s.getLabel(), orders.stream().filter(o -> o.getStatus() == s).count());
        }
        return summary;
    }

    // ===== CS 신청 (취소/교환/반품) — BO CS 연동 =====

    @Transactional
    public void requestCs(String username, Long orderId, String type, String reason) {
        Order order = getMyOrder(username, orderId);
        OrderStatus from = order.getStatus();
        OrderStatus to = switch (type) {
            case "cancel" -> {
                if (from != OrderStatus.PENDING_PAYMENT && from != OrderStatus.PREPARING) {
                    throw new IllegalArgumentException("입금전/배송준비중 상태에서만 취소 신청이 가능합니다.");
                }
                yield OrderStatus.CANCEL_REQUESTED;
            }
            case "exchange" -> {
                if (from != OrderStatus.DELIVERED) {
                    throw new IllegalArgumentException("배송완료 후에 교환 신청이 가능합니다.");
                }
                yield OrderStatus.EXCHANGE_REQUESTED;
            }
            case "return" -> {
                if (from != OrderStatus.DELIVERED) {
                    throw new IllegalArgumentException("배송완료 후에 반품 신청이 가능합니다.");
                }
                yield OrderStatus.RETURN_REQUESTED;
            }
            default -> throw new IllegalArgumentException("잘못된 신청 유형입니다.");
        };
        statusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order).fromStatus(from).toStatus(to).changedBy(username).build());
        order.setStatus(to);
        String memo = order.getAdminMemo() == null ? "" : order.getAdminMemo() + "\n";
        String label = switch (type) { case "cancel" -> "취소"; case "exchange" -> "교환"; default -> "반품"; };
        order.setAdminMemo(memo + "[고객 " + label + "신청] 사유: " + (reason == null ? "-" : reason));
    }
}
