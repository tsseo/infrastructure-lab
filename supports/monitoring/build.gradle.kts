// supports/monitoring: Actuator + Prometheus 모니터링 애드온 모듈
// 이 모듈을 의존하면 /actuator/prometheus 엔드포인트가 자동으로 활성화됨
// java-library 플러그인: api 스코프를 사용하여 의존성을 전이(transitive)로 노출
plugins {
    `java-library`
}

dependencies {
    // api 스코프: 이 모듈을 사용하는 앱에서도 actuator 와 prometheus 빈을 사용할 수 있도록 전이 의존성 노출
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("io.micrometer:micrometer-registry-prometheus")
}
