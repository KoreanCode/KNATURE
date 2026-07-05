---
name: cafe24-api
description: Cafe24 Admin API 연동 작업 시 사용. API 카테고리 지도, OAuth 인증 흐름, 요청 규칙, KNATURE 도메인과의 매핑 참조. 상품/주문/회원/재고 등을 Cafe24와 동기화하거나 외부 API를 호출하는 코드를 작성할 때.
---

# Cafe24 Admin API 연동 가이드

공식 문서: https://developers.cafe24.com/docs/api/admin/#api-index

## API 카테고리 (16개)

| 카테고리 | 주요 기능 | KNATURE 매핑 |
|---|---|---|
| **Store** | 상점 정보, 결제/배송 설정, 메뉴, SEO | 상점 설정 |
| **Product** | 상품 CRUD, 옵션, 이미지, 재고, 태그, 할인가 | `product` 도메인 |
| **Order** | 주문/취소/교환/반품/환불, 배송, 결제 | `order` 도메인 |
| **Customer** | 회원 정보, 등급, 메모, 소셜 연동 | `member` 도메인 |
| **Category** | 상품분류 관리, 자동진열, SEO | `ProductCategory` |
| **Collection** | 브랜드, 제조사, 원산지, 트렌드 | - |
| **Promotion** | 혜택, 쿠폰, 할인코드, 이벤트 | (신규 도메인 후보) |
| **Shipping** | 배송사, 배송비, 지역별 추가요금 | 주문/배송 |
| **Supply** | 공급사, 배송 공급사 | - |
| **Salesreport** | 일/월/시간별·상품별 매출 | 대시보드 |
| **Mileage** | 적립금/포인트, 리포트 | 회원 |
| **Notification** | 자동메일, SMS, 수신자 그룹 | - |
| **Community** | 게시판, 게시글, 댓글, 긴급문의 | - |
| **Personal** | 장바구니, 위시리스트 | FO |
| **Design** | 테마, 페이지, 아이콘 | - |
| **Translation** | 카테고리/상품/상점/테마 번역 | - |

## 인증 (OAuth 2.0)

- 몰별 인증: `https://{mallid}.cafe24api.com/api/v2/oauth/token`
- Authorization Code Grant → `access_token`(2시간) + `refresh_token`(2주). 만료 시 refresh로 갱신.
- 모든 API 호출 헤더: `Authorization: Bearer {access_token}`, `X-Cafe24-Api-Version: {YYYY-MM-DD}`
- **토큰/시크릿은 절대 하드코딩·커밋 금지** — 환경변수 또는 설정 외부화. `.gitignore` 확인.

## 요청 규칙

- Base: `https://{mallid}.cafe24api.com/api/v2/admin/{resource}`
- REST: `GET`(조회) / `POST`(생성) / `PUT`(수정) / `DELETE`(삭제)
- 페이징: `offset` + `limit`(최대 100), `embed`로 연관 리소스 포함
- Rate limit 존재 → 대량 동기화는 지연/재시도 처리. 429 대비 백오프.
- 응답은 JSON. 리소스명 복수형 래핑(예: `{ "products": [...] }`).

## 최신 스펙 확인

버전·필드·엔드포인트는 자주 바뀐다. 구현 전 **context7 플러그인** 또는 공식 문서로 해당 버전(`X-Cafe24-Api-Version`) 스펙을 반드시 확인한다. 기억에 의존해 필드를 추측하지 않는다.

## KNATURE 연동 시

- Cafe24 리소스를 KNATURE 엔티티에 그대로 복제하기보다, 필요한 필드만 매핑. 동기화 방향(Cafe24→DB / DB→Cafe24)을 먼저 정한다.
- 연동 서비스는 `knature-bo`의 `service` 계층에 두고, 엔티티는 [[knature-conventions]]대로 `knature-common`에 배치.
