package com.knature.bo.service;

import com.knature.common.domain.deposit.DepositHistory.DepositType;
import com.knature.common.domain.member.Member;
import com.knature.common.domain.mileage.MileageHistory.MileageType;
import com.knature.common.domain.order.Order;
import com.knature.common.domain.order.OrderItem;
import com.knature.common.domain.order.OrderStatus;
import com.knature.common.domain.order.OrderStatusHistory;
import com.knature.common.domain.order.PaymentMethod;
import com.knature.common.repository.MemberCouponRepository;
import com.knature.common.repository.OrderItemRepository;
import com.knature.common.repository.OrderRepository;
import com.knature.common.repository.OrderStatusHistoryRepository;
import com.knature.common.service.DepositService;
import com.knature.common.service.MileageService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final MileageService mileageService;
    private final DepositService depositService;
    private final com.knature.common.repository.ShopSettingRepository shopSettingRepository;
    private final com.knature.common.repository.StockLotRepository stockLotRepository;

    public Page<Order> getOrders(String keyword, OrderStatus status, LocalDate from, LocalDate to, Pageable pageable) {
        return orderRepository.findAll(buildSpec(keyword, status, from, to), pageable);
    }

    public List<Order> getOrdersForExcel(String keyword, OrderStatus status, LocalDate from, LocalDate to) {
        return orderRepository.findAll(buildSpec(keyword, status, from, to));
    }

    /** 품목별 조회 — 주문 조건으로 OrderItem 필터 */
    public Page<OrderItem> getOrderItems(String keyword, OrderStatus status, LocalDate from, LocalDate to, Pageable pageable) {
        Specification<OrderItem> spec = (root, query, cb) -> {
            var order = root.join("order");
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(order.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(order.get("orderNumber"), like),
                        cb.like(order.get("ordererName"), like),
                        cb.like(root.get("productName"), like)
                ));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(order.get("createdAt"), from.atStartOfDay()));
            }
            if (to != null) {
                predicates.add(cb.lessThan(order.get("createdAt"), to.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return orderItemRepository.findAll(spec, pageable);
    }

    private Specification<Order> buildSpec(String keyword, OrderStatus status, LocalDate from, LocalDate to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.isBlank()) {
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("orderNumber"), like),
                        cb.like(root.get("ordererName"), like)
                ));
            }
            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
            }
            if (to != null) {
                predicates.add(cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + id));
    }

    @Transactional
    public void updateStatus(Long id, OrderStatus status) {
        Order order = getOrder(id);
        OrderStatus from = order.getStatus();
        recordStatusChange(order, status, currentUsername());
        order.setStatus(status);
        // 취소/반품 완료 시: 재고 복원 + 적립금 환급 + 쿠폰 복구
        if ((status == OrderStatus.CANCELLED || status == OrderStatus.RETURN_COMPLETED)
                && from != OrderStatus.CANCELLED && from != OrderStatus.RETURN_COMPLETED) {
            restoreStock(order);
            refundBenefits(order);
        }
        // 배송완료 시: 구매확정 — 실적 누적 + 등급 자동 승급 + 등급별 구매 적립 (2차)
        if (status == OrderStatus.DELIVERED && from != OrderStatus.DELIVERED) {
            confirmPurchase(order);
        }
    }

    /** 주문 상품 재고 복원 + 품절 자동 해제 */
    private void restoreStock(Order order) {
        order.getItems().forEach(item -> {
            var product = item.getProduct();
            if (product == null) return;
            int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            product.setStockQuantity(stock + item.getQuantity());
            product.applyStockStatusRule();
        });
    }

    /** 취소 시 사용 적립금·예치금 환급 + 사용 쿠폰 복구 */
    private void refundBenefits(Order order) {
        Member member = order.getMember();
        if (member == null) return;
        long usedMileage = order.getUsedMileage() == null ? 0 : order.getUsedMileage();
        if (usedMileage > 0) {
            mileageService.change(member, usedMileage, MileageType.REFUND,
                    "주문 취소 환급 (" + order.getOrderNumber() + ")");
        }
        long usedDeposit = order.getUsedDeposit() == null ? 0 : order.getUsedDeposit();
        if (usedDeposit > 0) {
            depositService.change(member, usedDeposit, DepositType.REFUND,
                    "주문 취소 환급 (" + order.getOrderNumber() + ")");
        }
        if (order.getUsedMemberCouponId() != null) {
            memberCouponRepository.findById(order.getUsedMemberCouponId())
                    .ifPresent(mc -> mc.restore());
        }
        // 반품 등으로 아직 사용가능 전환 전인 구매 대기 적립이 있으면 무효화
        mileageService.voidPendingByOrder(member.getId(), order.getOrderNumber());
    }

    /**
     * 구매확정 — 총구매금액 누적 → 등급 자동 재계산 → 등급별 적립율 구매 적립.
     * 적립은 설정 mileage.usableAfterDays(기본 20일, 0=즉시) 후 사용 가능 (2차 정책)
     */
    private void confirmPurchase(Order order) {
        Member member = order.getMember();
        if (member == null) return;
        long amount = order.getPaymentAmount() == null ? 0 : order.getPaymentAmount();
        member.setTotalPurchaseAmount((member.getTotalPurchaseAmount() == null ? 0 : member.getTotalPurchaseAmount()) + amount);
        member.recalculateGrade();
        int rate = member.getGrade().getRewardRate();
        long reward = amount * rate / 100;
        if (reward > 0) {
            int usableAfterDays = settingInt("mileage.usableAfterDays", 20);
            String reason = "구매 적립 " + rate + "% (" + order.getOrderNumber() + ")";
            if (usableAfterDays > 0) {
                mileageService.changePending(member, reward, LocalDate.now().plusDays(usableAfterDays),
                        MileageType.PURCHASE, reason + " — " + usableAfterDays + "일 후 사용 가능");
            } else {
                mileageService.change(member, reward, MileageType.PURCHASE, reason);
            }
        }
        log.info("구매확정: {} → 총구매 {}원, 등급 {}, 적립 {}P",
                member.getUsername(), member.getTotalPurchaseAmount(), member.getGrade(), reward);
    }

    private int settingInt(String key, int defaultValue) {
        return shopSettingRepository.findBySettingKey(key)
                .map(s -> { try { return Integer.parseInt(s.getSettingValue().trim()); } catch (Exception e) { return defaultValue; } })
                .orElse(defaultValue);
    }

    @Transactional
    public void updateMemo(Long id, String memo) {
        Order order = getOrder(id);
        order.setAdminMemo(memo);
    }

    /** 송장 입력 → 배송중 전환 */
    @Transactional
    public void updateShipping(Long id, String courierCompany, String trackingNumber) {
        if (courierCompany == null || courierCompany.isBlank() || trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("택배사와 송장번호를 모두 입력해주세요.");
        }
        if (trackingNumber.length() > 30) {
            throw new IllegalArgumentException("송장번호는 30자 이내로 입력해주세요.");
        }
        if (!trackingNumber.matches("[0-9-]+")) {
            throw new IllegalArgumentException("송장번호는 숫자(하이픈 허용)만 입력할 수 있습니다.");
        }
        Order order = getOrder(id);
        order.setCourierCompany(courierCompany);
        order.setTrackingNumber(trackingNumber);
        recordStatusChange(order, OrderStatus.SHIPPING, currentUsername());
        order.setStatus(OrderStatus.SHIPPING);
        deductLotsFifo(order);
    }

    /**
     * 출고(송장 등록) 시 LOT 재고 FIFO 차감 (2차) — 제조일 오래된 LOT부터.
     * 본사 판매 재고는 주문 시점에 이미 차감되므로 LOT 관리 수량만 동기화한다.
     */
    private void deductLotsFifo(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getProduct() == null) continue;
            int remaining = item.getQuantity();
            var lots = stockLotRepository
                    .findByProductIdAndQuantityGreaterThanOrderByManufactureDateAscIdAsc(item.getProduct().getId(), 0);
            for (var lot : lots) {
                if (remaining <= 0) break;
                int take = Math.min(lot.getQuantity(), remaining);
                lot.setQuantity(lot.getQuantity() - take);
                remaining -= take;
            }
            if (remaining > 0) {
                log.info("LOT FIFO: {} — LOT 미등록분 {}개는 일반 재고에서 출고", item.getProductName(), remaining);
            }
        }
    }

    /**
     * 송장 일괄 등록 (CSV 행: 주문번호,택배사,송장번호)
     * @return 처리 결과 요약 {success, fail, errors[]}
     */
    @Transactional
    public Map<String, Object> bulkShipping(List<String[]> rows) {
        int success = 0;
        List<String> errors = new ArrayList<>();
        String username = currentUsername();
        for (int i = 0; i < rows.size(); i++) {
            String[] row = rows.get(i);
            int lineNo = i + 2; // 헤더 다음 줄부터
            if (row.length < 3 || row[0].isBlank() || row[1].isBlank() || row[2].isBlank()) {
                errors.add(lineNo + "행: 주문번호/택배사/송장번호가 비어 있습니다.");
                continue;
            }
            String tracking = row[2].trim();
            if (tracking.length() > 30 || !tracking.matches("[0-9-]+")) {
                errors.add(lineNo + "행: 송장번호 형식 오류 (숫자·하이픈, 30자 이내)");
                continue;
            }
            var found = orderRepository.findByOrderNumber(row[0].trim());
            if (found.isEmpty()) {
                errors.add(lineNo + "행: 주문번호 없음 (" + row[0].trim() + ")");
                continue;
            }
            Order order = found.get();
            order.setCourierCompany(row[1].trim());
            order.setTrackingNumber(tracking);
            recordStatusChange(order, OrderStatus.SHIPPING, username);
            order.setStatus(OrderStatus.SHIPPING);
            deductLotsFifo(order);
            success++;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", success);
        result.put("fail", errors.size());
        result.put("errors", errors);
        return result;
    }

    /** 무통장입금 미입금 7일 경과 자동 취소 (스케줄러에서 호출) */
    @Transactional
    public int cancelExpiredBankTransferOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        List<Order> expired = orderRepository.findByStatusAndPaymentMethodAndCreatedAtBefore(
                OrderStatus.PENDING_PAYMENT, PaymentMethod.BANK_TRANSFER, threshold);
        for (Order order : expired) {
            recordStatusChange(order, OrderStatus.CANCELLED, "system");
            order.setStatus(OrderStatus.CANCELLED);
            restoreStock(order); // 자동취소 시에도 재고 복원
            String memo = order.getAdminMemo() == null ? "" : order.getAdminMemo() + "\n";
            order.setAdminMemo(memo + "[시스템] 무통장입금 7일 미입금 자동취소");
        }
        if (!expired.isEmpty()) {
            log.info("무통장 미입금 자동취소 처리: {}건", expired.size());
        }
        return expired.size();
    }

    private void recordStatusChange(Order order, OrderStatus toStatus, String changedBy) {
        if (order.getStatus() == toStatus) return;
        statusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .fromStatus(order.getStatus())
                .toStatus(toStatus)
                .changedBy(changedBy)
                .build());
    }

    private String currentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }
}
