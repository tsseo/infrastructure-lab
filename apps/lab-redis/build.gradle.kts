// apps/lab-redis: Redis 부하 테스트 및 학습용 Spring Boot 애플리케이션
// modules/redis (Redis 설정), supports/monitoring (모니터링) 에 의존
// ApiResponse, CoreException, ErrorType 은 이 앱의 interfaces/api, support/error 패키지에 직접 포함
dependencies {
    // ─── 내부 모듈 의존성 ─────────────────────────────────────────────────────
    implementation(project(":modules:redis"))
    implementation(project(":supports:monitoring"))

    // ─── 웹 레이어 ─────────────────────────────────────────────────────────────
    implementation("org.springframework.boot:spring-boot-starter-web")

    // ─── L1 로컬 캐시 (Caffeine) ───────────────────────────────────────────────
    // Spring Boot BOM 이 버전을 관리하므로 버전을 명시하지 않는다.
    // spring-boot-starter-cache 를 사용하지 않고 Caffeine 라이브러리만 직접 주입한다.
    // 이렇게 하면 CacheManager 자동 설정 없이 포트 + 어댑터로 캐시를 완전히 직접 관리할 수 있다.
    implementation("com.github.ben-manes.caffeine:caffeine")
}
