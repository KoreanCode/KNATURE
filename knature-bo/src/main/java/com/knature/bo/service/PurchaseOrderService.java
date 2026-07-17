package com.knature.bo.service;

import com.knature.common.domain.admin.Admin;
import com.knature.common.domain.product.Product;
import com.knature.common.domain.scm.*;
import com.knature.common.domain.scm.PurchaseOrder.PoStatus;
import com.knature.common.domain.stock.StockHistory;
import com.knature.common.repository.*;
import com.knature.common.service.AutoOrderService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderService {

    /** 진행 중(미완료) 상태 집합 */
    private static final List<PoStatus> OPEN_STATUSES = List.of(
            PoStatus.REQUESTED, PoStatus.APPROVED, PoStatus.CONFIRMED, PoStatus.IN_PRODUCTION, PoStatus.SHIPPED);

    private final PurchaseOrderRepository poRepository;
    private final FactoryRepository factoryRepository;
    private final FactoryProductRepository factoryProductRepository;
    private final ProductRepository productRepository;
    private final GoodsReceiptRepository receiptRepository;
    private final FactoryStockRepository factoryStockRepository;
    private final StockLotRepository stockLotRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final AdminRepository adminRepository;
    private final AutoOrderService autoOrderService;
    private final com.knature.common.service.RestockAlertService restockAlertService;

    /** 목록 — 공장관리자는 자기 공장 발주만 (factoryScope) */
    public List<PurchaseOrder> getOrders(PoStatus status, Long factoryId, Long factoryScope) {
        Specification<PurchaseOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (factoryScope != null) {
                predicates.add(cb.equal(root.get("factory").get("id"), factoryScope));
            } else if (factoryId != null) {
                predicates.add(cb.equal(root.get("factory").get("id"), factoryId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            query.orderBy(cb.desc(root.get("id")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return poRepository.findAll(spec);
    }

    public PurchaseOrder getOrder(Long id) {
        return poRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));
    }

    /** 수동 발주 생성 (승인대기 상태) */
    @Transactional
    public PurchaseOrder create(Long factoryId, Long productId, int quantity, LocalDate dueDate,
                                String memo, String createdBy) {
        if (quantity <= 0) throw new IllegalArgumentException("발주 수량은 1개 이상이어야 합니다.");
        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new IllegalArgumentException("공장을 찾을 수 없습니다."));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));
        FactoryProduct mapping = factoryProductRepository
                .findByFactoryId(factoryId).stream()
                .filter(fp -> fp.getProduct().getId().equals(productId))
                .findFirst().orElse(null);
        long unitCost = mapping != null ? mapping.getUnitCost() : 0L;
        if (mapping != null && quantity < mapping.getMoq()) {
            throw new IllegalArgumentException("최소 발주 수량(MOQ)은 " + mapping.getMoq() + "개입니다.");
        }
        if (dueDate == null) {
            int lead = mapping != null && mapping.getLeadTimeDays() != null
                    ? mapping.getLeadTimeDays() : factory.getLeadTimeDays();
            dueDate = LocalDate.now().plusDays(lead);
        }
        return poRepository.save(PurchaseOrder.builder()
                .poNumber(generatePoNumber())
                .factory(factory).product(product)
                .quantity(quantity).unitCost(unitCost)
                .dueDate(dueDate).memo(memo).createdBy(createdBy)
                .build());
    }

    /** 자동 발주 — 공통 AutoOrderService 위임 (지역 매칭 포함, FO 주문 트리거와 동일 로직) */
    @Transactional
    public int runAutoOrder() {
        return autoOrderService.runFullScan();
    }

    /**
     * 상태 전이 — 역할별 제약:
     * SUPER_ADMIN: 승인/반려/취소, FACTORY_ADMIN: 자기 공장 발주의 공장확인/생산중/출고만
     */
    @Transactional
    public void updateStatus(Long id, PoStatus to, String username) {
        PurchaseOrder po = getOrder(id);
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("관리자 정보를 찾을 수 없습니다."));
        boolean isSuper = admin.getRole().name().equals("SUPER_ADMIN");

        if (isSuper) {
            if (!List.of(PoStatus.APPROVED, PoStatus.REJECTED, PoStatus.CANCELLED).contains(to)) {
                throw new IllegalArgumentException("최고관리자는 승인/반려/취소만 처리할 수 있습니다. (생산 상태는 공장관리자가 업데이트)");
            }
            if (to == PoStatus.APPROVED && po.getStatus() != PoStatus.REQUESTED) {
                throw new IllegalArgumentException("승인대기 상태의 발주만 승인할 수 있습니다.");
            }
            if (to == PoStatus.CANCELLED && !List.of(PoStatus.REQUESTED, PoStatus.APPROVED).contains(po.getStatus())) {
                throw new IllegalArgumentException("공장 확인 전(승인대기/승인) 발주만 취소할 수 있습니다.");
            }
        } else {
            // 공장관리자: 자기 공장 + 순차 전이만
            if (admin.getFactory() == null || !admin.getFactory().getId().equals(po.getFactory().getId())) {
                throw new IllegalArgumentException("소속 공장의 발주만 처리할 수 있습니다.");
            }
            boolean valid = (to == PoStatus.CONFIRMED && po.getStatus() == PoStatus.APPROVED)
                    || (to == PoStatus.IN_PRODUCTION && po.getStatus() == PoStatus.CONFIRMED)
                    || (to == PoStatus.SHIPPED && po.getStatus() == PoStatus.IN_PRODUCTION);
            if (!valid) {
                throw new IllegalArgumentException("공장확인 → 생산중 → 출고 순서로만 진행할 수 있습니다.");
            }
        }
        po.setStatus(to);
    }

    /**
     * 입고 등록 — 출고(SHIPPED) 상태의 발주에 대해 전량/부분 입고.
     * 검수 양품만 본사 재고 반영(+이력, 품절 자동 해제), LOT 생성, 공장 재고 차감.
     */
    @Transactional
    public GoodsReceipt receive(Long poId, int receivedQty, int goodQty, int defectQty,
                                String defectReason, String lotNumber, LocalDate expiryDate, String receivedBy) {
        PurchaseOrder po = getOrder(poId);
        if (po.getStatus() != PoStatus.SHIPPED) {
            throw new IllegalArgumentException("출고 상태의 발주만 입고 등록할 수 있습니다.");
        }
        if (receivedQty <= 0 || goodQty < 0 || defectQty < 0 || goodQty + defectQty != receivedQty) {
            throw new IllegalArgumentException("입고 수량 = 양품 + 불량이어야 합니다.");
        }
        int remaining = po.getQuantity() - po.getReceivedQuantity();
        if (receivedQty > remaining) {
            throw new IllegalArgumentException("잔여 입고 가능 수량은 " + remaining + "개입니다.");
        }

        GoodsReceipt receipt = receiptRepository.save(GoodsReceipt.builder()
                .purchaseOrder(po).receivedQuantity(receivedQty)
                .goodQuantity(goodQty).defectQuantity(defectQty).defectReason(defectReason)
                .lotNumber(lotNumber).receivedBy(receivedBy)
                .build());

        // 1) 본사 재고 반영 (양품) + 이력 + 품절 자동 해제 + 재입고 알림 (3차)
        Product product = po.getProduct();
        if (goodQty > 0) {
            int before = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
            product.setStockQuantity(before + goodQty);
            product.applyStockStatusRule();
            restockAlertService.notifyIfRestocked(product, before);
            stockHistoryRepository.save(StockHistory.builder()
                    .product(product).beforeQuantity(before).afterQuantity(before + goodQty)
                    .reason("입고 (" + po.getPoNumber() + (lotNumber != null ? ", LOT " + lotNumber : "") + ")")
                    .adjustedBy(receivedBy)
                    .build());
        }

        // 2) LOT 등록
        if (goodQty > 0 && lotNumber != null && !lotNumber.isBlank()) {
            stockLotRepository.save(StockLot.builder()
                    .product(product).factory(po.getFactory())
                    .lotNumber(lotNumber).manufactureDate(LocalDate.now())
                    .expiryDate(expiryDate).quantity(goodQty)
                    .build());
        }

        // 3) 공장 재고 자동 차감 (등록돼 있으면)
        factoryStockRepository.findByFactoryIdAndProductId(po.getFactory().getId(), product.getId())
                .ifPresent(fs -> fs.setQuantity(Math.max(0, fs.getQuantity() - receivedQty)));

        // 4) 발주 누적 입고 → 전량 도달 시 입고완료
        po.setReceivedQuantity(po.getReceivedQuantity() + receivedQty);
        if (po.getReceivedQuantity() >= po.getQuantity()) {
            po.setStatus(PoStatus.RECEIVED);
        }
        log.info("입고: {} +{} (양품 {}, 불량 {}) → 재고 {}", po.getPoNumber(), receivedQty, goodQty, defectQty, product.getStockQuantity());
        return receipt;
    }

    private String generatePoNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        long count = poRepository.countByPoNumberStartingWith("PO-" + date + "-");
        return String.format("PO-%s-%03d", date, count + 1);
    }
}
