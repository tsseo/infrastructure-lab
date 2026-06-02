package io.github.tsseo.infrastructure.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tsseo.domain.catalog.CatalogRepository;
import io.github.tsseo.domain.catalog.CatalogSnapshot;
import io.github.tsseo.support.error.CoreException;
import io.github.tsseo.support.error.ErrorType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Redis 기반 카탈로그 저장소 — {@link CatalogRepository} 포트의 L2 구현체.
 *
 * <h2>실험의 핵심 병목 지점</h2>
 * {@link #findById(String)} 내의 {@code objectMapper.readValue(...)} 가
 * 이 실험에서 재현하려는 병목이다.
 * <ul>
 *   <li>LARGE 페이로드(~500KB JSON, 800개 Product): Jackson 역직렬화에 수십 ms 소요</li>
 *   <li>동시 요청이 많으면 JVM CPU 를 포화시켜 Lettuce I/O 이벤트 루프의 스케줄링을 지연시킨다</li>
 *   <li>그 결과 Redis 명령 자체는 빠르지만 앱 측에서 타임아웃으로 관측된다</li>
 * </ul>
 *
 * <p>이 구현체가 {@code domain} 의 인터페이스를 구현하므로, 의존 방향이 역전된다:
 * {@code infrastructure} → {@code domain} (정방향이 아닌 역방향)</p>
 */
@Repository
public class RedisCatalogRepository implements CatalogRepository {

    /**
     * Spring Boot 가 자동 구성하는 {@link StringRedisTemplate}.
     * 키와 값 모두 UTF-8 문자열로 직렬화/역직렬화한다.
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Spring Boot 가 자동 구성하는 Jackson {@link ObjectMapper}.
     * {@code Instant} 등 Java 8 날짜 타입을 올바르게 직렬화하려면
     * {@code JavaTimeModule} 이 등록되어 있어야 한다.
     * Spring Boot 자동 설정에서는 기본으로 포함된다.
     */
    private final ObjectMapper objectMapper;

    public RedisCatalogRepository(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 키로 카탈로그 스냅샷을 Redis 에서 조회한다.
     *
     * <p>키가 없으면 {@code Optional.empty()} 를 반환한다.
     * JSON 문자열 → {@link CatalogSnapshot} 역직렬화는 이 메서드 내부에서 발생하며,
     * LARGE 페이로드의 경우 수십 ms 의 CPU 비용이 든다.
     * 이것이 Redis 커맨드 타임아웃의 "진짜 원인"이다.</p>
     *
     * @param key Redis 키 (예: "catalog:large")
     * @return 스냅샷 Optional
     * @throws CoreException Jackson 역직렬화 실패 시 {@link ErrorType#INTERNAL_ERROR}
     */
    @Override
    public Optional<CatalogSnapshot> findById(String key) {
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json == null) {
            return Optional.empty();
        }

        try {
            // ★ 이 역직렬화가 LARGE 페이로드에서 JVM CPU 를 수십 ms 점유한다.
            //    그 사이 Lettuce I/O 이벤트 루프가 CPU 슬롯을 받지 못해
            //    Redis 커맨드 응답 처리가 지연되고, 타임아웃처럼 관측된다.
            CatalogSnapshot snapshot = objectMapper.readValue(json, CatalogSnapshot.class);
            return Optional.of(snapshot);
        } catch (JsonProcessingException e) {
            throw new CoreException(
                    ErrorType.INTERNAL_ERROR,
                    "Redis 에서 읽은 JSON 을 CatalogSnapshot 으로 역직렬화하는 데 실패했습니다. key=" + key
            );
        }
    }

    /**
     * 카탈로그 스냅샷을 JSON 직렬화하여 Redis 에 저장한다.
     *
     * <p>TTL 을 설정하지 않으므로 명시적으로 삭제하기 전까지 키가 유지된다.
     * 실험용이므로 단순성을 우선한다.</p>
     *
     * @param key      Redis 키 (예: "catalog:large")
     * @param snapshot 저장할 카탈로그 스냅샷
     * @throws CoreException Jackson 직렬화 실패 시 {@link ErrorType#INTERNAL_ERROR}
     */
    @Override
    public void save(String key, CatalogSnapshot snapshot) {
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            stringRedisTemplate.opsForValue().set(key, json);
        } catch (JsonProcessingException e) {
            throw new CoreException(
                    ErrorType.INTERNAL_ERROR,
                    "CatalogSnapshot 을 JSON 으로 직렬화하는 데 실패했습니다. key=" + key
            );
        }
    }
}
