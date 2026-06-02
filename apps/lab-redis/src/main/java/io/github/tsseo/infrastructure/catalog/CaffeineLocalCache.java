package io.github.tsseo.infrastructure.catalog;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.tsseo.domain.catalog.CatalogSnapshot;
import io.github.tsseo.domain.catalog.LocalCachePort;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Caffeine 기반 L1 로컬 캐시 — {@link LocalCachePort} 포트의 구현체.
 *
 * <h2>왜 spring-boot-starter-cache 를 쓰지 않는가?</h2>
 * <ul>
 *   <li>Spring Cache 추상화({@code @Cacheable})는 AOP 프록시를 통해 캐시를 적용하므로
 *       캐시 미스/히트 분기가 코드에서 명시적으로 드러나지 않는다.</li>
 *   <li>이 실험에서는 L1 히트 시 Jackson 역직렬화가 발생하지 않는다는 사실을
 *       코드 레벨에서 명확히 보여주기 위해 직접 제어한다.</li>
 *   <li>포트 + 어댑터 패턴으로 구현하면 나중에 Redis 자체를 L1 로 교체해도
 *       도메인 코드는 변경 없이 이 클래스만 바꾸면 된다.</li>
 * </ul>
 *
 * <h2>캐시 설정</h2>
 * <ul>
 *   <li>최대 100개 항목 (실험에는 2개 키만 사용하므로 여유 충분)</li>
 *   <li>쓰기 후 10분 만료 — 갱신 없이 오래 방치된 캐시가 메모리를 점유하지 않도록</li>
 *   <li>히트/미스 통계 기록 — {@code recordStats()} 로 Caffeine 내부 통계를 수집</li>
 * </ul>
 */
@Component
public class CaffeineLocalCache implements LocalCachePort {

    /**
     * Caffeine 캐시 인스턴스.
     * 스레드 안전하며, 내부적으로 ConcurrentHashMap 기반으로 동작한다.
     */
    private final Cache<String, CatalogSnapshot> cache;

    public CaffeineLocalCache() {
        this.cache = Caffeine.newBuilder()
                // 최대 100개 항목 — 실험용이므로 충분히 크게 설정
                .maximumSize(100)
                // 쓰기 후 10분 만료 — 오래된 스냅샷이 메모리를 점유하지 않도록
                .expireAfterWrite(Duration.ofMinutes(10))
                // 히트/미스 통계 활성화 — 나중에 /actuator/metrics 등으로 확인 가능
                .recordStats()
                .build();
    }

    /**
     * 키에 해당하는 캐시 항목을 조회한다.
     *
     * <p>캐시 히트 시: 메모리에서 직접 반환, Jackson 역직렬화 없음, CPU 비용 최소.
     * 캐시 미스 시: {@code Optional.empty()} 반환, 호출자가 L2(Redis) 를 조회해야 함.</p>
     *
     * @param key 캐시 키
     * @return 히트 시 {@code Optional.of(snapshot)}, 미스 시 {@code Optional.empty()}
     */
    @Override
    public Optional<CatalogSnapshot> get(String key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    /**
     * 카탈로그 스냅샷을 캐시에 저장한다.
     *
     * @param key      캐시 키
     * @param snapshot 저장할 스냅샷
     */
    @Override
    public void put(String key, CatalogSnapshot snapshot) {
        cache.put(key, snapshot);
    }
}
