// modules/redis: 재사용 가능한 Redis 설정 모듈
// 이 모듈을 의존하는 앱 프로젝트에 Redis 관련 자동 설정을 제공
// java-library 플러그인: api 스코프를 사용하여 의존성을 전이(transitive)로 노출
plugins {
    `java-library`
}

dependencies {
    // api: 이 모듈을 사용하는 프로젝트에도 Redis 스타터가 전이(transitive)되도록 api 스코프 사용
    api("org.springframework.boot:spring-boot-starter-data-redis")
}
