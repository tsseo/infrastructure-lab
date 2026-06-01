// apps/lab-redis: Redis 부하 테스트 및 학습용 Spring Boot 애플리케이션
// modules/redis (Redis 설정), supports/monitoring (모니터링) 에 의존
// ApiResponse, CoreException, ErrorType 은 이 앱의 interfaces/api, support/error 패키지에 직접 포함
dependencies {
    // ─── 내부 모듈 의존성 ─────────────────────────────────────────────────────
    implementation(project(":modules:redis"))
    implementation(project(":supports:monitoring"))

    // ─── 웹 레이어 ─────────────────────────────────────────────────────────────
    implementation("org.springframework.boot:spring-boot-starter-web")
}
