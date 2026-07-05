---
name: bo-feature-scaffold
description: KNATURE 관리자(BO)에 새 도메인/기능을 추가할 때 사용. Entity(common) + REST 엔드포인트(knature-bo) + React 페이지(knature-bo-frontend)를 기존 products/orders 패턴대로 일관되게 스캐폴딩한다.
---

# BO 신규 기능 스캐폴딩

새 도메인(예: coupon, review, notice)을 추가할 때 아래 3계층을 기존 `product`/`order` 패턴 그대로 만든다. 전체 컨벤션은 [[knature-conventions]] 참조.

## 1) knature-common — 도메인 (먼저)

- `com.knature.common.domain.<도메인>/<Xxx>.java` — `BaseEntity` 상속, Lombok(`@Getter @Entity` 등), 상태값은 별도 `enum`(예: `XxxStatus`)
- `com.knature.common.repository.<Xxx>Repository.java` — `JpaRepository<Xxx, Long>`, 검색은 keyword/status 파라미터용 쿼리 메서드 추가

## 2) knature-bo — Service + Controller

`Service`:
- `com.knature.bo.service.<Xxx>Service` — `@Service @RequiredArgsConstructor`, Repository `private final` 주입
- 메서드: `getXxxs(keyword, status, pageable)` → `Page<>`, `getXxx(id)`, `saveXxx(entity)`, `updateStatus(id, status)`, `deleteXxx(id)`

`Controller` (`ProductController` 복사 후 자원명만 교체):
```java
@RestController
@RequestMapping("/api/<자원>")
@RequiredArgsConstructor
public class XxxController {
    private final XxxService xxxService;

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required=false) String keyword,
                                  @RequestParam(required=false) XxxStatus status,
                                  @PageableDefault(size=20, sort="id", direction=Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(xxxService.getXxxs(keyword, status, pageable));
    }
    @GetMapping("/{id}")   public ResponseEntity<?> detail(@PathVariable Long id) { ... }
    @PostMapping           public ResponseEntity<?> create(@RequestBody Xxx x) { ... }
    @PutMapping("/{id}")   public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Xxx x) { x.setId(id); ... }
    @PatchMapping("/{id}/status") // Map<String,String> body → XxxStatus.valueOf(body.get("status"))
    @DeleteMapping("/{id}")       // return ResponseEntity.ok(Map.of("message","삭제되었습니다."));
}
```
- 응답 메시지는 한국어 `Map.of("message", "...")`
- 인증이 필요하면 `SecurityConfig`의 매처에 `/api/<자원>/**` 추가 확인

## 3) knature-bo-frontend — 페이지

- `src/pages/<도메인>/<Xxx>ListPage.jsx` (목록+검색+페이지네이션), 필요 시 `<Xxx>FormPage.jsx`(등록/수정)
- 호출은 반드시 `import api from '../../api/client'` 사용 — 예: `api.get('/<자원>', { params:{ keyword, status, page } })`
- `App.jsx` 라우트 추가 + `Sidebar.jsx` 메뉴 항목 추가
- 폼/목록 스타일은 기존 Product 페이지를 참고해 `global.css` 클래스 재사용

## 마무리

- BO 백엔드 빌드: `./gradlew :knature-bo:build`, 프론트 빌드: `npm run build`
- 실제 화면 동작을 `playwright` 플러그인 또는 `/verify`로 확인 후 커밋(`/commit`).
