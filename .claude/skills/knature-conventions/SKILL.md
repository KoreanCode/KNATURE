---
name: knature-conventions
description: KNATURE 프로젝트의 모듈 구조, 스택, 코딩 컨벤션 참조. KNATURE 저장소에서 코드를 작성/수정하거나, 모듈 배치·엔티티·API·프론트 구조를 판단해야 할 때 사용.
---

# KNATURE 프로젝트 컨벤션

Cafe24 기반 이커머스. Gradle 멀티모듈, Java 21, Spring Boot 3.3.5, Lombok. group=`com.knature`.

## 모듈 구조

| 모듈 | 역할 | 포트 | 스택 |
|---|---|---|---|
| `knature-common` | Entity / Repository / JpaConfig (전 모듈 공유) | - | Spring Data JPA |
| `knature-bo` | 관리자(BO) REST API | 9090 | Spring Boot REST + Security(세션) |
| `knature-bo-frontend` | BO 관리자 화면 | 5173 | React + Vite + axios + React Router |
| `knature-fo` | 사용자 쇼핑몰(FO) | - | (현재 Thymeleaf 스켈레톤, 미개발) → [[fo-dev]] |

- 실행: `./gradlew :knature-bo:bootRun` (JAVA_HOME=Corretto 21) / `cd knature-bo-frontend && npm run dev`
- BO 로그인 계정: `admin` / `admin1004` (세션 기반, `DataInitializer`가 시드)

## knature-common — 도메인

- 패키지: `com.knature.common.domain.<도메인>`, 공통 상위 `BaseEntity`
- 도메인: `admin`(Admin, AdminRole), `member`(Member, MemberGrade), `order`(Order, OrderItem, OrderStatus, PaymentMethod), `product`(Product, ProductCategory, ProductImage, ProductOption, ProductStatus)
- Repository: `com.knature.common.repository.*Repository` (Admin/Member/Order/Product/ProductCategory)
- **새 엔티티/Repository는 반드시 common에 둔다** (bo·fo가 공유).

## knature-bo — REST API 컨벤션

- 계층: `controller` → `service`, 설정은 `config`(SecurityConfig, AdminUserDetailsService, DataInitializer)
- Controller 패턴:
  - `@RestController @RequestMapping("/api/<자원>") @RequiredArgsConstructor`, 서비스는 `private final` 생성자 주입
  - 반환은 `ResponseEntity.ok(...)`, 메시지 응답은 한국어 `Map.of("message", "...")`
  - 목록: `list(@RequestParam(required=false) String keyword, @RequestParam(required=false) <Status> status, @PageableDefault(size=20, sort="id", direction=DESC) Pageable)`
  - 상세 `GET /{id}`, 생성 `POST`, 수정 `PUT /{id}`(body.setId(id)), 상태변경 `PATCH /{id}/status`(`Map<String,String>` body → `Enum.valueOf`), 삭제 `DELETE /{id}`
- 엔드포인트: `/api/auth`(login/logout/me), `/api/dashboard`, `/api/products`, `/api/orders`, `/api/members`

## knature-bo-frontend — React 컨벤션

- API 호출은 항상 `src/api/client.js`의 axios 인스턴스 사용 (baseURL `http://localhost:9090/api`, `withCredentials:true`, 401 시 `/login` 리다이렉트)
- 페이지: `src/pages/<도메인>/<Xxx>Page.jsx` (List/Form 분리, 예: `product/ProductListPage.jsx`, `product/ProductFormPage.jsx`)
- 공통 레이아웃: `src/components/Layout.jsx` / `Sidebar.jsx` / `TopBar.jsx`, 라우팅은 `App.jsx`
- 전역 스타일 `src/styles/global.css`

## 새 기능 추가 시

BO 기능 추가는 [[bo-feature-scaffold]] 패턴을 따른다. 외부 Cafe24 연동은 [[cafe24-api]] 참조.
