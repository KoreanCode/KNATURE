---
name: fo-dev
description: KNATURE 사용자 쇼핑몰(FO, knature-fo) 개발 시 사용. 현재 상태, 기능 명세서 위치, 상품조회/장바구니/주문 플로우, BO/common과의 관계를 안내. FO 화면·기능을 구현하거나 FO 아키텍처를 정할 때.
---

# FO (Front Office / 사용자 쇼핑몰) 개발 가이드

## 현재 상태

- `knature-fo`는 **Thymeleaf 스켈레톤 상태(미개발)**: `HomeController`, `templates/index.html`, `static/css/common.css`, `SecurityConfig`, `application.yml`만 존재.
- 참고: BO는 Thymeleaf → **React SPA + REST API로 전환 완료**. FO도 동일 방식(React+REST)으로 갈지, Thymeleaf SSR로 갈지 **아직 미정 → 착수 전 사용자에게 확인**.
- 엔티티/Repository는 `knature-common`을 BO와 공유한다 ([[knature-conventions]]).

## 기능 명세서

- `docs/KNATURE_FO_기능명세서.xlsx` — FO 기능 정의의 단일 출처. 구현 전 반드시 확인(엑셀은 `xlsx` 스킬로 읽기).
- 화면/기능은 명세서를 우선하며, 임의 추가 금지.

## 핵심 플로우 (일반 이커머스 기준, 명세서로 확정)

1. **상품 조회** — 카테고리별 목록, 상세, 검색/정렬/필터, 이미지·옵션·재고 표시
2. **장바구니** — 담기/수량변경/삭제, 옵션별 가격, 로그인/비로그인 처리 (Cafe24 Personal API 참조 가능 → [[cafe24-api]])
3. **주문/결제** — 주문서 작성, 배송지, 결제수단(`PaymentMethod`), 주문 생성(`Order`/`OrderItem`, `OrderStatus`)
4. **회원** — 가입/로그인, 등급(`MemberGrade`), 마이페이지(주문내역/적립금)

## 구현 원칙

- 주문/회원 도메인은 BO와 **동일 엔티티(common)를 재사용** — FO에서 새로 만들지 말 것.
- FO 인증은 고객용(회원)이며 BO 관리자 세션(`admin`)과 분리.
- React 방식 선택 시 BO 프론트 컨벤션(axios `client.js`, 페이지 구조) 재사용, 백엔드는 `/api/**` REST로 노출.
- 실제 브라우저 동작은 `playwright` 플러그인 / `/verify`로 확인.

## 착수 시 첫 단계

1. `docs/KNATURE_FO_기능명세서.xlsx` 정독
2. FO 스택(React SPA vs Thymeleaf) 사용자 확정
3. 명세 기반 화면·API 목록 도출 후 [[bo-feature-scaffold]] 패턴 응용
