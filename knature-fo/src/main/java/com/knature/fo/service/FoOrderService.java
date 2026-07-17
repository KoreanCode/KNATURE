package com.knature.fo.service;

import com.knature.common.domain.coupon.MemberCoupon;
import com.knature.common.domain.deposit.DepositHistory.DepositType;
import com.knature.common.domain.member.Member;
import com.knature.common.domain.mileage.MileageHistory.MileageType;
import com.knature.common.domain.order.*;
import com.knature.common.domain.product.Product;
import com.knature.common.domain.product.ProductOption;
import com.knature.common.repository.*;
import com.knature.common.service.AutoOrderService;
import com.knature.common.service.CouponService;
import com.knature.common.service.DepositService;
import com.knature.common.service.MileageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final MemberCouponRepository memberCouponRepository;
    private final MileageService mileageService;
    private final CouponService couponService;
    private final DepositService depositService;
    private final AutoOrderService autoOrderService;
    private final PasswordEncoder passwordEncoder;

    /** 주문 항목 요청 (productId, optionId nullable, quantity) */
    public record OrderItemRequest(Long productId, Long optionId, int quantity) {}

    /** 비회원 주문자 정보 (2차) — password 는 주문 조회용 */
    public record GuestInfo(String name, String email, String phone, String password) {}

    @Transactional
    public Order createOrder(String username, List<OrderItemRequest> items, PaymentMethod paymentMethod,
                             String receiverName, String receiverPhone, String zipcode,
                             String address, String addressDetail, String deliveryMemo,
                             long useMileage, Long memberCouponId) {
        return createOrder(username, items, paymentMethod, receiverName, receiverPhone, zipcode,
                address, addressDetail, deliveryMemo, useMileage, memberCouponId, 0, null);
    }

    /**
     * 주문 생성 — 가격은 서버에서 재계산(클라이언트 금액 불신), 재고 검증·차감, 주문번호 발급.
     * 적립금/예치금/쿠폰은 서버에서 재검증. 무통장 → 입금전 / 그 외(1차 모의결제) → 배송준비중.
     * username == null 이면 비회원 주문 (guest 필수, 혜택 사용 불가) — 2차
     */
    @Transactional
    public Order createOrder(String username, List<OrderItemRequest> items, PaymentMethod paymentMethod,
                             String receiverName, String receiverPhone, String zipcode,
                             String address, String addressDetail, String deliveryMemo,
                             long useMileage, Long memberCouponId, long useDeposit, GuestInfo guest) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("주문할 상품이 없습니다.");
        }
        if (receiverName == null || receiverName.isBlank() || address == null || address.isBlank()) {
            throw new IllegalArgumentException("배송지 정보를 입력해주세요.");
        }
        Member member = null;
        if (username != null) {
            member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        } else {
            if (guest == null || guest.name() == null || guest.name().isBlank()
                    || guest.password() == null || guest.password().length() < 4) {
                throw new IllegalArgumentException("비회원 주문자 정보와 주문 비밀번호(4자 이상)를 입력해주세요.");
            }
            if (useMileage > 0 || useDeposit > 0 || memberCouponId != null) {
                throw new IllegalArgumentException("비회원 주문은 쿠폰/적립금/예치금을 사용할 수 없습니다.");
            }
        }

        // 1) 상품 검증 + 금액 계산 + 재고 차감 — 회원 등급별 추가 할인 적용 (3차)
        int gradeDiscountRate = member != null ? gradeDiscountRate(member) : 0;
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
            if (gradeDiscountRate > 0) {
                unitPrice = unitPrice * (100 - gradeDiscountRate) / 100; // 등급별 회원가 (원 단위 내림)
            }
            // 재고 차감 + 품절 자동 처리
            product.setStockQuantity(stock - req.quantity());
            product.applyStockStatusRule();

            totalAmount += unitPrice * req.quantity();
            lines.add(new Line(product, option, req.quantity(), unitPrice));
        }

        // 2) 배송비 — BO 설정(배송 정책) 연동
        long deliveryFee = calculateDeliveryFee(totalAmount);

        // 3) 쿠폰 적용 (서버 재검증) — 회원 전용
        long couponDiscount = 0;
        MemberCoupon memberCoupon = null;
        if (memberCouponId != null) {
            memberCoupon = memberCouponRepository.findByIdAndMemberId(memberCouponId, member.getId())
                    .orElseThrow(() -> new IllegalArgumentException("보유하지 않은 쿠폰입니다."));
            couponDiscount = couponService.validateAndCalculate(memberCoupon, totalAmount);
            memberCoupon.use();
        }

        // 4) 적립금 사용 (잔액 검증 + 결제금액 초과 방지) — 회원 전용
        long payable = totalAmount + deliveryFee - couponDiscount;
        if (useMileage < 0) {
            throw new IllegalArgumentException("적립금 사용액이 올바르지 않습니다.");
        }
        if (useMileage > payable) {
            useMileage = payable; // 결제금액까지만 사용
        }
        if (useMileage > 0) {
            mileageService.change(member, -useMileage, MileageType.USE, "주문 사용");
        }

        // 4-1) 예치금 사용 (2차) — 회원 전용, 적립금 차감 후 잔여 결제금액까지만
        long payableAfterMileage = payable - useMileage;
        if (useDeposit < 0) {
            throw new IllegalArgumentException("예치금 사용액이 올바르지 않습니다.");
        }
        if (useDeposit > payableAfterMileage) {
            useDeposit = payableAfterMileage;
        }
        if (useDeposit > 0) {
            depositService.change(member, -useDeposit, DepositType.USE, "주문 사용");
        }

        // 5) 주문 생성
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .member(member)
                .ordererName(member != null ? member.getName() : guest.name())
                .ordererEmail(member != null ? member.getEmail() : guest.email())
                .ordererPhone(member != null ? member.getPhone() : guest.phone())
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .receiverZipcode(zipcode)
                .receiverAddress(address)
                .receiverAddressDetail(addressDetail)
                .deliveryMemo(deliveryMemo)
                .totalAmount(totalAmount)
                .deliveryFee(deliveryFee)
                .paymentAmount(payable - useMileage - useDeposit)
                .paymentMethod(paymentMethod)
                .build();
        order.setUsedMileage(useMileage);
        order.setUsedDeposit(useDeposit);
        order.setCouponDiscount(couponDiscount);
        order.setUsedMemberCouponId(memberCoupon != null ? memberCoupon.getId() : null);
        if (member == null) {
            order.setGuestPassword(passwordEncoder.encode(guest.password()));
        }
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

        // 6) 주문으로 재고가 안전재고 이하로 떨어진 상품 → 배송지 지역 담당 공장에 자동 발주 (2차 SCM)
        for (Line line : lines) {
            autoOrderService.createIfNeeded(line.product(), address,
                    "주문 접수 트리거 (" + saved.getOrderNumber() + ")");
        }
        return saved;
    }

    /** 비회원 주문 조회 — 주문번호 + 주문 비밀번호 (2차) */
    public Order getGuestOrder(String orderNumber, String password) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("주문번호 또는 비밀번호가 올바르지 않습니다."));
        if (!order.isGuest() || order.getGuestPassword() == null
                || password == null || !passwordEncoder.matches(password, order.getGuestPassword())) {
            throw new IllegalArgumentException("주문번호 또는 비밀번호가 올바르지 않습니다.");
        }
        return order;
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

    /** 등급별 추가 할인율(%) — 설정 gradeDiscount.{GRADE}, 0~90 범위로 방어 (3차) */
    private int gradeDiscountRate(Member member) {
        long rate = settingLong("gradeDiscount." + member.getGrade().name(), 0);
        return (int) Math.max(0, Math.min(90, rate));
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
        return getMyOrders(username, null);
    }

    /** 기간별 주문 내역 (months: 1/3/6, null=전체) */
    public List<Order> getMyOrders(String username, Integer months) {
        Member member = memberRepository.findByUsername(username).orElseThrow();
        if (months != null && months > 0) {
            return orderRepository.findByMemberIdAndCreatedAtAfterOrderByIdDesc(
                    member.getId(), LocalDateTime.now().minusMonths(months));
        }
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
