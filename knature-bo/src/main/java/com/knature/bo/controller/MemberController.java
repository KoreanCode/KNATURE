package com.knature.bo.controller;

import com.knature.bo.service.MemberService;
import com.knature.common.domain.member.MemberGrade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) MemberGrade grade,
                       @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        model.addAttribute("members", memberService.getMembers(keyword, grade, pageable));
        model.addAttribute("keyword", keyword);
        model.addAttribute("grade", grade);
        model.addAttribute("grades", MemberGrade.values());
        return "member/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("member", memberService.getMember(id));
        model.addAttribute("grades", MemberGrade.values());
        return "member/detail";
    }
}
