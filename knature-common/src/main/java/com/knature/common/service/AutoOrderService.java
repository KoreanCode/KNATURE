package com.knature.common.service;

import com.knature.common.domain.product.Product;
import com.knature.common.domain.scm.FactoryProduct;
import com.knature.common.domain.scm.PurchaseOrder;
import com.knature.common.domain.scm.PurchaseOrder.PoStatus;
import com.knature.common.repository.FactoryProductRepository;
import com.knature.common.repository.ProductRepository;
import com.knature.common.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

/**
 * 자동 발주 공통 서비스 — BO(수동 실행 버튼)·FO(주문 접수 트리거) 공용.
 * 공장 선택: 배송지 주소가 담당 지역(region)에 해당하는 공장 우선, 없으면 최저 단가 공장.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutoOrderService {

    private static final List<PoStatus> OPEN_STATUSES = List.of(
            PoStatus.REQUESTED, PoStatus.APPROVED, PoStatus.CONFIRMED, PoStatus.IN_PRODUCTION, PoStatus.SHIPPED);

    private final PurchaseOrderRepository poRepository;
    private final FactoryProductRepository factoryProductRepository;
    private final ProductRepository productRepository;

    /**
     * 단일 상품 자동 발주 — 안전재고 이하 + 진행 중 발주 없음일 때만 생성.
     * @param shippingAddress 트리거한 주문의 배송지 (지역 매칭용, null 가능)
     * @return 생성된 발주 (조건 미충족 시 null)
     */
    @Transactional
    public PurchaseOrder createIfNeeded(Product product, String shippingAddress, String triggerNote) {
        int stock = product.getStockQuantity() == null ? 0 : product.getStockQuantity();
        int safety = product.getSafetyStock() == null ? 0 : product.getSafetyStock();
        if (safety <= 0 || stock > safety) return null;
        if (!poRepository.findByProductIdAndStatusIn(product.getId(), OPEN_STATUSES).isEmpty()) return null;

        FactoryProduct mapping = selectFactory(product.getId(), shippingAddress);
        if (mapping == null) return null;

        int shortage = safety * 2 - stock; // 안전재고의 2배까지 확보
        int quantity = Math.max(shortage, mapping.getMoq());
        int lead = mapping.getLeadTimeDays() != null ? mapping.getLeadTimeDays()
                : mapping.getFactory().getLeadTimeDays();

        String regionNote = shippingAddress != null && mapping.getFactory().coversAddress(shippingAddress)
                ? " / 지역매칭: " + mapping.getFactory().getRegion() : "";
        PurchaseOrder po = poRepository.save(PurchaseOrder.builder()
                .poNumber(generatePoNumber())
                .factory(mapping.getFactory()).product(product)
                .quantity(quantity).unitCost(mapping.getUnitCost())
                .dueDate(LocalDate.now().plusDays(lead))
                .memo("[자동발주] 재고 " + stock + " / 안전재고 " + safety
                        + (triggerNote != null ? " / " + triggerNote : "") + regionNote)
                .createdBy("auto")
                .build());
        log.info("자동발주: {} {}개 → {} ({})", product.getName(), quantity,
                mapping.getFactory().getName(), regionNote.isBlank() ? "최저단가" : "지역매칭");
        return po;
    }

    /** 전 상품 스캔 (BO 자동발주 실행 버튼) — 생성 건수 반환 */
    @Transactional
    public int runFullScan() {
        int created = 0;
        for (Product product : productRepository.findAll()) {
            if (createIfNeeded(product, null, "전체 스캔") != null) created++;
        }
        return created;
    }

    /**
     * 공장 선택 — 1순위: 배송지 주소를 담당 지역으로 가진 공장 (여러 곳이면 최저 단가),
     * 2순위: 전체 매핑 중 최저 단가
     */
    private FactoryProduct selectFactory(Long productId, String shippingAddress) {
        List<FactoryProduct> mappings = factoryProductRepository.findByProductId(productId).stream()
                .filter(fp -> Boolean.TRUE.equals(fp.getFactory().getActive()))
                .toList();
        if (mappings.isEmpty()) return null;

        if (shippingAddress != null && !shippingAddress.isBlank()) {
            var regionMatch = mappings.stream()
                    .filter(fp -> fp.getFactory().coversAddress(shippingAddress))
                    .min(Comparator.comparingLong(FactoryProduct::getUnitCost));
            if (regionMatch.isPresent()) return regionMatch.get();
        }
        return mappings.stream()
                .min(Comparator.comparingLong(FactoryProduct::getUnitCost))
                .orElse(null);
    }

    private String generatePoNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        long count = poRepository.countByPoNumberStartingWith("PO-" + date + "-");
        for (int attempt = 0; attempt < 10; attempt++) {
            String candidate = String.format("PO-%s-%03d", date, count + 1 + attempt);
            if (poRepository.findByPoNumber(candidate).isEmpty()) return candidate;
        }
        throw new IllegalStateException("발주번호 생성에 실패했습니다.");
    }
}
