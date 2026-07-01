# KNATURE

KNATURE 화장품 브랜드 쇼핑몰 프로젝트 (아람티앤씨)

## 프로젝트 구조

```
KNATURE/
├── knature-common/    # 공통 모듈 (Entity, Repository, Config)
├── knature-fo/        # Front Office (고객 웹사이트, port: 8080)
├── knature-bo/        # Back Office (관리자 페이지, port: 9090)
└── docs/              # 기능명세서
```

## 기술 스택

- Java 21
- Spring Boot 3.3.5
- Spring Security
- Spring Data JPA
- Thymeleaf
- Gradle (Multi-module)
- MySQL (운영) / H2 (개발)

## 브랜치 전략

- `main` - 운영 브랜치
- `develop` - 개발 브랜치

## 실행 방법

```bash
# FO (고객 사이트) - localhost:8080
./gradlew :knature-fo:bootRun

# BO (관리자 사이트) - localhost:9090
./gradlew :knature-bo:bootRun
```

## 문서

- `docs/KNATURE_기능명세서.xlsx` - BO 기능명세서 (1차/2차/3차)
- `docs/KNATURE_FO_기능명세서.xlsx` - FO 기능명세서 (1차/2차/3차)
