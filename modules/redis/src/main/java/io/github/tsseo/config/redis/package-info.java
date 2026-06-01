/**
 * 도메인 비의존 · 재사용 가능한 Redis 설정 모듈.
 *
 * <p>이 패키지는 특정 비즈니스 도메인에 종속되지 않는다.
 * Redis 연결, 직렬화, 템플릿 빈 등 인프라 기술 설정만 포함한다.</p>
 *
 * <p>어떤 앱({@code apps/*})이든 {@code :modules:redis} 모듈을 의존하면
 * 이 패키지의 설정이 자동으로 적용된다.
 * 실제 연결 정보(host/port)는 {@code redis.yml} 을 통해 외부에서 주입된다.</p>
 */
package io.github.tsseo.config.redis;
