package io.github.tsseo.domain.catalog;

import io.github.tsseo.support.error.CoreException;
import io.github.tsseo.support.error.ErrorType;
import org.springframework.stereotype.Service;

/**
 * 카탈로그 도메인 서비스.
 *
 * <p>L1 캐시(Caffeine)와 L2 저장소(Redis) 를 조합하여 카탈로그를 조회한다.
 * 각 요청마다 {@code useL1} 플래그로 L1 캐시 사용 여부를 제어할 수 있어
 * 부하 테스트에서 L1 ON/OFF 시나리오를 쉽게 전환할 수 있다.</p>
 *
 * <h2>캐시 계층 흐름</h2>
 * <pre>
 * useL1=true:
 *   L1 캐시(메모리) → 히트 시 즉시 반환 (역직렬화 없음)
 *   L1 미스 → L2(Redis) 조회 → JSON 역직렬화 → L1 에 저장 → 반환
 *
 * useL1=false:
 *   L2(Redis) 직접 조회 → JSON 역직렬화 (매 요청마다 CPU 비용 발생)
 * </pre>
 *
 * <h2>실험 목적</h2>
 * {@code useL1=false, size=LARGE} 시나리오에서 매 요청마다 약 500KB JSON 을
 * 역직렬화하므로 JVM CPU 포화 → Lettuce I/O 스레드 지연 → Redis 커맨드 타임아웃을 재현한다.
 */
@Service
public class CatalogService {

    private final CatalogRepository catalogRepository;
    private final LocalCachePort localCache;

    public CatalogService(CatalogRepository catalogRepository, LocalCachePort localCache) {
        this.catalogRepository = catalogRepository;
        this.localCache = localCache;
    }

    /**
     * 페이로드 크기와 L1 캐시 사용 여부에 따라 카탈로그를 조회한다.
     *
     * @param size   조회할 페이로드 크기 (SMALL / LARGE)
     * @param useL1  {@code true} 이면 L1(Caffeine) 캐시를 먼저 확인한다
     * @return 카탈로그 Info DTO
     * @throws CoreException Redis 에 해당 키가 없을 경우 {@link ErrorType#NOT_FOUND}
     */
    public CatalogInfo getCatalog(PayloadSize size, boolean useL1) {
        // Redis 키: "catalog:small" 또는 "catalog:large"
        String key = "catalog:" + size.name().toLowerCase();

        if (useL1) {
            // L1 캐시 조회 — 히트 시 역직렬화 없이 바로 반환
            return localCache.get(key)
                    .map(cached -> {
                        // L1 캐시 히트: Redis 호출 없음, Jackson 역직렬화 없음
                        return CatalogInfo.from(cached);
                    })
                    .orElseGet(() -> {
                        // L1 캐시 미스: L2(Redis) 에서 로드 후 L1 에 저장
                        CatalogSnapshot snapshot = loadFromRepository(key);
                        localCache.put(key, snapshot);
                        return CatalogInfo.from(snapshot);
                    });
        } else {
            // L1 캐시 비사용: 매 요청마다 Redis 조회 + Jackson 역직렬화
            CatalogSnapshot snapshot = loadFromRepository(key);
            return CatalogInfo.from(snapshot);
        }
    }

    /**
     * 저장소(L2)에서 스냅샷을 로드한다.
     * 키가 없으면 {@link CoreException} 을 던진다.
     *
     * @param key Redis 키
     * @return 로드된 스냅샷
     * @throws CoreException 키가 없을 경우 {@link ErrorType#NOT_FOUND}
     */
    private CatalogSnapshot loadFromRepository(String key) {
        return catalogRepository.findById(key)
                .orElseThrow(() -> new CoreException(
                        ErrorType.NOT_FOUND,
                        "카탈로그 키 '" + key + "' 를 Redis 에서 찾을 수 없습니다. " +
                        "앱 기동 시 CatalogSeeder 가 정상 실행됐는지 확인하세요."
                ));
    }
}
