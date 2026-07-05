package com.knature.fo.controller;

import com.knature.common.domain.member.Member;
import com.knature.common.domain.member.MemberAddress;
import com.knature.common.repository.MemberAddressRepository;
import com.knature.common.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 마이페이지 배송지 관리 — 본인 배송지만 CRUD */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final MemberAddressRepository addressRepository;
    private final MemberRepository memberRepository;

    private Member me(Authentication auth) {
        return memberRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication auth) {
        return ResponseEntity.ok(addressRepository.findByMemberIdOrderByIsDefaultDescIdDesc(me(auth).getId()));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Map<String, String> body, Authentication auth) {
        Member member = me(auth);
        boolean isDefault = "true".equals(body.get("isDefault"))
                || addressRepository.countByMemberId(member.getId()) == 0; // 첫 배송지는 기본
        if (isDefault) {
            clearDefault(member.getId());
        }
        MemberAddress address = MemberAddress.builder()
                .member(member)
                .alias(orDefault(body.get("alias"), "배송지"))
                .receiverName(required(body.get("receiverName"), "수령인"))
                .receiverPhone(required(body.get("receiverPhone"), "연락처"))
                .zipcode(required(body.get("zipcode"), "우편번호"))
                .address(required(body.get("address"), "주소"))
                .addressDetail(body.get("addressDetail"))
                .isDefault(isDefault)
                .build();
        return ResponseEntity.ok(addressRepository.save(address));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, String> body,
                                    Authentication auth) {
        Member member = me(auth);
        MemberAddress address = addressRepository.findByIdAndMemberId(id, member.getId())
                .orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));
        if (body.get("alias") != null) address.setAlias(body.get("alias"));
        if (body.get("receiverName") != null) address.setReceiverName(body.get("receiverName"));
        if (body.get("receiverPhone") != null) address.setReceiverPhone(body.get("receiverPhone"));
        if (body.get("zipcode") != null) address.setZipcode(body.get("zipcode"));
        if (body.get("address") != null) address.setAddress(body.get("address"));
        if (body.get("addressDetail") != null) address.setAddressDetail(body.get("addressDetail"));
        if ("true".equals(body.get("isDefault"))) {
            clearDefault(member.getId());
            address.setIsDefault(true);
        }
        return ResponseEntity.ok(address);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        MemberAddress address = addressRepository.findByIdAndMemberId(id, me(auth).getId())
                .orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));
        addressRepository.delete(address);
        return ResponseEntity.ok(Map.of("message", "배송지가 삭제되었습니다."));
    }

    private void clearDefault(Long memberId) {
        addressRepository.findByMemberIdAndIsDefaultTrue(memberId).forEach(a -> a.setIsDefault(false));
    }

    private String required(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + "을(를) 입력해주세요.");
        return value;
    }

    private String orDefault(String value, String def) {
        return value == null || value.isBlank() ? def : value;
    }
}
