package com.knature.bo.controller;

import com.knature.bo.service.PurchaseOrderService;
import com.knature.bo.util.ExcelUtil;
import com.knature.common.domain.admin.Admin;
import com.knature.common.domain.scm.PurchaseOrder;
import com.knature.common.domain.scm.PurchaseOrder.PoStatus;
import com.knature.common.repository.AdminRepository;
import com.knature.common.repository.GoodsReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** BO 발주 관리 — 목록(공장관리자는 자기 공장만)/생성/자동발주/상태변경/입고/발주서 */
@RestController
@RequestMapping("/api/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService poService;
    private final GoodsReceiptRepository receiptRepository;
    private final AdminRepository adminRepository;

    /** 공장관리자면 소속 공장 id 반환 (강제 스코프), 최고관리자는 null */
    private Long factoryScope(Authentication auth) {
        Admin admin = adminRepository.findByUsername(auth.getName()).orElse(null);
        if (admin != null && admin.getRole().name().equals("FACTORY_ADMIN")) {
            if (admin.getFactory() == null) {
                throw new IllegalArgumentException("소속 공장이 지정되지 않은 계정입니다. 최고관리자에게 문의하세요.");
            }
            return admin.getFactory().getId();
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) PoStatus status,
                                  @RequestParam(required = false) Long factoryId,
                                  Authentication auth) {
        return ResponseEntity.ok(poService.getOrders(status, factoryId, factoryScope(auth)));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, String> body, Authentication auth) {
        if (factoryScope(auth) != null) {
            throw new IllegalArgumentException("발주 생성은 최고관리자만 가능합니다.");
        }
        PurchaseOrder po = poService.create(
                Long.parseLong(body.get("factoryId")),
                Long.parseLong(body.get("productId")),
                Integer.parseInt(body.get("quantity")),
                body.get("dueDate") != null && !body.get("dueDate").isBlank() ? LocalDate.parse(body.get("dueDate")) : null,
                body.get("memo"),
                auth.getName());
        return ResponseEntity.ok(po);
    }

    /** 자동 발주 실행 (안전재고 이하 상품 스캔) */
    @PostMapping("/auto-run")
    public ResponseEntity<?> autoRun(Authentication auth) {
        if (factoryScope(auth) != null) {
            throw new IllegalArgumentException("자동 발주는 최고관리자만 실행할 수 있습니다.");
        }
        int created = poService.runAutoOrder();
        return ResponseEntity.ok(Map.of("message", created + "건의 자동 발주가 생성되었습니다.", "created", created));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body,
                                          Authentication auth) {
        poService.updateStatus(id, PoStatus.valueOf(body.get("status")), auth.getName());
        return ResponseEntity.ok(Map.of("message", "발주 상태가 변경되었습니다."));
    }

    /** 입고 등록 (검수 포함) — 최고관리자 */
    @PostMapping("/{id}/receive")
    public ResponseEntity<?> receive(@PathVariable Long id, @RequestBody Map<String, String> body,
                                     Authentication auth) {
        if (factoryScope(auth) != null) {
            throw new IllegalArgumentException("입고 등록은 본사(최고관리자)만 가능합니다.");
        }
        poService.receive(id,
                Integer.parseInt(body.get("receivedQuantity")),
                Integer.parseInt(body.get("goodQuantity")),
                Integer.parseInt(body.getOrDefault("defectQuantity", "0")),
                body.get("defectReason"),
                body.get("lotNumber"),
                body.get("expiryDate") != null && !body.get("expiryDate").isBlank() ? LocalDate.parse(body.get("expiryDate")) : null,
                auth.getName());
        return ResponseEntity.ok(Map.of("message", "입고가 등록되었습니다. (양품은 본사 재고에 반영)"));
    }

    @GetMapping("/{id}/receipts")
    public ResponseEntity<?> receipts(@PathVariable Long id) {
        return ResponseEntity.ok(receiptRepository.findByPurchaseOrderIdOrderByIdDesc(id));
    }

    /** 발주서 엑셀 다운로드 */
    @GetMapping("/{id}/excel")
    public ResponseEntity<byte[]> excel(@PathVariable Long id) throws IOException {
        PurchaseOrder po = poService.getOrder(id);
        String[] headers = {"발주번호", "공장", "상품", "수량", "단가", "총액", "납기요청일", "상태", "비고"};
        Object[] row = {
                po.getPoNumber(), po.getFactory().getName(), po.getProduct().getName(),
                po.getQuantity(), po.getUnitCost(), po.getTotalCost(),
                po.getDueDate() != null ? po.getDueDate().toString() : "",
                po.getStatus().getLabel(), po.getMemo()
        };
        List<Object[]> rows = java.util.Collections.singletonList(row);
        return ExcelUtil.download("발주서_" + po.getPoNumber() + ".xlsx", "발주서", headers, rows);
    }
}
