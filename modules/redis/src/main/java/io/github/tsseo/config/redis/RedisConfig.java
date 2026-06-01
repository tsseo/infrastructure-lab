package io.github.tsseo.config.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 재사용 가능한 Redis 설정 모듈.
 *
 * <p>이 클래스는 {@code modules/redis} 모듈에 위치하며,
 * 이 모듈을 의존하는 모든 앱(apps/*)에서 자동으로 스캔된다.
 * 패키지 루트가 {@code io.github.tsseo} 이므로
 * {@code @SpringBootApplication} 의 기본 컴포넌트 스캔 범위에 포함된다.</p>
 *
 * <h2>단순화 결정</h2>
 * <ul>
 *   <li>단일 노드(standalone) Redis 만 지원한다 — master/replica 분리는 생략.</li>
 *   <li>키/값 모두 String 직렬화 사용 ({@link StringRedisTemplate}).</li>
 *   <li>연결 설정은 {@code redis.yml} 에서 Spring Data Redis 자동 설정으로 주입된다.</li>
 * </ul>
 */
@Configuration
public class RedisConfig {

    /**
     * String 타입 키/값 전용 Redis 템플릿 빈.
     *
     * <p>{@link StringRedisTemplate} 은 키와 값을 모두 UTF-8 문자열로 직렬화한다.
     * 별도의 직렬화 설정 없이 사람이 읽을 수 있는 형태로 Redis 에 저장되므로
     * 학습 및 디버깅에 적합하다.</p>
     *
     * @param connectionFactory Spring Data Redis 가 {@code redis.yml} 설정으로 자동 생성하는 연결 팩토리
     * @return StringRedisTemplate 빈
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
