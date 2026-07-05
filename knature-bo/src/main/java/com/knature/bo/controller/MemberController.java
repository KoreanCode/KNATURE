package com.knature.bo.controller;

import com.knature.bo.service.MemberService;
import com.knature.bo.util.ExcelUtil;
import com.knature.common.domain.member.Member;
import com.knature.common.domain.member.MemberGrade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) MemberGrade grade,
                                  @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(memberService.getMembers(keyword, grade, pageable));
    }

    /** 회원 목록 엑셀(.xlsx) 다운로드 — 셀 타입 지정(연락처=텍스트, 금액=숫자, 가입일=날짜) */
    @GetMapping("/excel")
    public ResponseEntity<byte[]> excel(@RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) MemberGrade grade) throws IOException {
        String[] headers = {"아이디", "이름", "이메일", "연락처", "등급", "총구매금액", "활성", "가입일"};
        List<Object[]> rows = new ArrayList<>();
        for (Member m : memberService.getMembersForExcel(keyword, grade)) {
            rows.add(new Object[]{
                    m.getUsername(),
                    m.getName(),
                    m.getEmail(),
                    m.getPhone(),
                    m.getGrade().getLabel(),
                    m.getTotalPurchaseAmount(),   // 숫자 셀 (#,##0)
                    Boolean.TRUE.equals(m.getActive()) ? "활성" : "비활성",
                    m.getCreatedAt()              // 날짜 셀
            });
        }
        return ExcelUtil.download("회원목록.xlsx", "회원목록", headers, rows);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> detail(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMember(id));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<?> orders(@PathVariable Long id) {
        return ResponseEntity.ok(memberService.getMemberOrders(id));
    }

    /** 회원 등급 수동 변경 (자동 승급/강등은 2차) */
    @PatchMapping("/{id}/grade")
    public ResponseEntity<?> updateGrade(@PathVariable Long id, @RequestBody Map<String, String> body) {
        memberService.updateGrade(id, MemberGrade.valueOf(body.get("grade")));
        return ResponseEntity.ok(Map.of("message", "회원 등급이 변경되었습니다."));
    }
}
