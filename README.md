# KNATURE

**KNATURE**(케이네이쳐)는 아람티앤씨(주)에서 운영하는 프리미엄 화장품 브랜드 쇼핑몰입니다.
자연 원료와 과학 기술을 결합한 스킨케어 제품을 판매하며, 본 프로젝트는 기존 카페24 기반 쇼핑몰을 자체 플랫폼으로 전환하기 위한 프로젝트입니다.

## 프로젝트 구조

Gradle 기반 **모노레포 멀티모듈** 구조로 FO(고객)와 BO(관리자)를 분리하여 운영합니다.

```
KNATURE/
├── knature-common/          # 공통 모듈 (Entity, Repository, Config)
│   └── com.knature.common
├── knature-fo/              # Front Office - 고객 웹사이트 (port: 8080)
│   └── com.knature.fo
├── knature-bo/              # Back Office - 관리자 페이지 (port: 9090)
│   └── com.knature.bo
├── docs/                    # 기능명세서 (BO/FO)
├── build.gradle             # 루트 빌드 설정
├── settings.gradle          # 멀티모듈 설정
└── gradle/wrapper/          # Gradle Wrapper
```

### 모듈별 역할

| 모듈 | 설명 | 포트 |
|------|------|------|
| `knature-common` | 공통 Entity, Repository, Config (FO/BO 공유) | - |
| `knature-fo` | 고객용 웹사이트 (상품 조회, 주문, 마이페이지 등) | 8080 |
| `knature-bo` | 관리자용 페이지 (주문/상품/재고/고객 관리 등) | 9090 |

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security |
| ORM | Spring Data JPA + Hibernate |
| Template | Thymeleaf |
| Build | Gradle 8.10 (Multi-module) |
| DB (개발) | H2 (In-memory) |
| DB (운영) | MySQL 8.x |

## 개발 단계

FO와 BO 모두 3단계로 나누어 개발합니다.

### BO (Back Office)

| 단계 | 주요 기능 |
|------|----------|
| **1차 MVP** | 대시보드, 주문 관리, 상품 관리, 고객 관리, 재고(수동), 설정 |
| **2차** | 공장/발주/입고/출고, 재고 고도화, 적립금/예치금/쿠폰, 게시판, 상품 진열, 등급 6단계 |
| **3차** | 자동 알림(SMS), 매출 분석, PG 정산, 등급별 가격, 공장 평가, 발주 예측 |

### FO (Front Office)

| 단계 | 주요 기능 |
|------|----------|
| **1차 MVP** | 메인, 회원(가입/로그인), 상품(목록/상세/검색), 장바구니, 주문/결제, 마이페이지 |
| **2차** | 메인 고도화, 멤버십 6등급, 적립금/예치금/쿠폰, 위시리스트, 리뷰, 커뮤니티, 갤러리 |
| **3차** | 다국어(9개), 정기배송, SNS 연동, 최근 본 상품, 자동 알림, 재입고 알림 |

## 브랜치 전략

| 브랜치 | 용도 |
|--------|------|
| `main` | 운영 배포 브랜치 |
| `develop` | 개발 통합 브랜치 |
| `feature/*` | 기능 개발 브랜치 (develop에서 분기) |
| `hotfix/*` | 긴급 수정 브랜치 (main에서 분기) |

```
main ────────────────────────────── (운영 배포)
  └── develop ───────────────────── (개발 통합)
        ├── feature/order-management
        ├── feature/product-crud
        └── feature/member-auth
```

## 시작하기

### 사전 요구사항

- Java 21+
- MySQL 8.x (운영 환경)

### 실행

```bash
# FO (고객 사이트) - http://localhost:8080
./gradlew :knature-fo:bootRun

# BO (관리자 사이트) - http://localhost:9090
./gradlew :knature-bo:bootRun
```

### 빌드

```bash
# 전체 빌드
./gradlew build

# 개별 모듈 빌드
./gradlew :knature-fo:build
./gradlew :knature-bo:build
```

### 운영 환경 실행

```bash
# 환경 변수 설정 후 실행
DB_USERNAME=your_user DB_PASSWORD=your_pass \
  java -jar knature-fo/build/libs/knature-fo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

## 문서

| 문서 | 경로 | 설명 |
|------|------|------|
| BO 기능명세서 | `docs/KNATURE_기능명세서.xlsx` | 1차/2차/3차 + 프로세스 흐름 + 권한 매트릭스 + 재고 구조 + FO-BO 정합성 점검 |
| FO 기능명세서 | `docs/KNATURE_FO_기능명세서.xlsx` | 1차/2차/3차 + 페이지 맵 + FO-BO 연동 매트릭스 |

## 상품 라인

| 라인 | 설명 |
|------|------|
| HYDRA CALMING LINE | 수분 진정 라인 |
| VOLUME LINE | 볼륨 라인 |
| AMPOULE LINE | 앰플 라인 |
| CREAM LINE | 크림 라인 |
