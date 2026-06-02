package io.github.tsseo.config;

import io.lettuce.core.metrics.MicrometerCommandLatencyRecorder;
import io.lettuce.core.metrics.MicrometerOptions;
import io.lettuce.core.resource.ClientResources;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lettuce Redis 클라이언트 Micrometer 메트릭 설정.
 *
 * <h2>목적</h2>
 * Lettuce 는 기본적으로 Redis 커맨드 레이턴시 메트릭을 수집하지 않는다.
 * 이 설정을 통해 {@code lettuce.command.firstresponse.latency} 와 같은
 * 히스토그램 메트릭을 {@code /actuator/prometheus} 에서 관측할 수 있다.
 *
 * <h2>왜 앱 레이어에 두는가?</h2>
 * {@link MeterRegistry} 빈은 {@code supports/monitoring} 에서 제공하는
 * {@code micrometer-registry-prometheus} 에 의해 자동 등록된다.
 * modules/redis 모듈은 Micrometer 에 의존하지 않으므로 (불필요한 결합 방지),
 * 이 설정은 Micrometer 가 classpath 에 있는 앱 레이어에 위치한다.
 *
 * <h2>관측 가능한 메트릭 (예시)</h2>
 * <pre>
 *   lettuce_command_firstresponse_latency_seconds_*
 *   lettuce_command_completion_latency_seconds_*
 * </pre>
 * 이 메트릭으로 Redis 커맨드 레이턴시가 앱 측에서 어떻게 관측되는지 확인할 수 있다.
 * JVM CPU 포화 시 레이턴시가 급격히 증가하는 현상을 이 메트릭으로 포착한다.
 */
@Configuration
public class RedisMetricsConfig {

    /**
     * Lettuce 클라이언트에 Micrometer 기반 커맨드 레이턴시 레코더를 등록한다.
     *
     * <p>{@link LettuceClientConfigurationBuilderCustomizer} 는 Spring Boot 의
     * Lettuce 자동 설정 과정에서 호출되므로, 별도 ConnectionFactory 를 생성할 필요가 없다.</p>
     *
     * <p>{@link MicrometerOptions#create()} 는 히스토그램과 퍼센타일 활성화를 포함한
     * 기본 옵션으로 생성된다. 필요하면 {@link MicrometerOptions#builder()} 로 세부 조정 가능하다.</p>
     *
     * @param meterRegistry Micrometer 레지스트리 (prometheus exporter 포함)
     * @return Lettuce 클라이언트 설정 커스터마이저
     */
    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceMetricsCustomizer(MeterRegistry meterRegistry) {
        return builder -> builder.clientResources(
                ClientResources.builder()
                        // Micrometer 기반 커맨드 레이턴시 레코더 등록
                        // Redis 커맨드의 첫 번째 응답 레이턴시와 완료 레이턴시를 측정한다
                        .commandLatencyRecorder(
                                new MicrometerCommandLatencyRecorder(meterRegistry, MicrometerOptions.create())
                        )
                        .build()
        );
    }
}
