package io.github.tsseo.domain.catalog;

import java.util.Optional;

/**
 * 로컬 캐시 포트 (L1 — Caffeine).
 *
 * <p>도메인 레이어에 정의된 L1 캐시 추상화 포트다.
 * 실제 구현({@code CaffeineLocalCache})은 인프라 레이어에 위치한다.
 * 이를 통해 도메인은 Caffeine 이라는 기술에 직접 의존하지 않는다.</p>
 *
 * <p>L1 캐시가 활성화되면 Redis(L2) 까지 가지 않으므로,
 * Jackson 역직렬화가 발생하지 않아 CPU 비용이 없다.
 * 이것이 L1 캐시가 Redis 타임아웃 현상을 완화하는 핵심 이유다.</p>
 */
public interface LocalCachePort {

    /**
     * 키에 해당하는 캐시 항목을 조회한다.
     *
     * @param key 캐시 키 (예: "catalog:large")
     * @return 캐시에 존재하면 {@code Optional.of(snapshot)}, 없으면 {@code Optional.empty()}
     */
    Optional<CatalogSnapshot> get(String key);

    /**
     * 카탈로그 스냅샷을 캐시에 저장한다.
     *
     * <p>이미 존재하는 키라면 덮어쓴다.
     * 만료 정책은 {@code CaffeineLocalCache} 구현체에서 설정한다.</p>
     *
     * @param key      캐시 키 (예: "catalog:large")
     * @param snapshot 저장할 카탈로그 스냅샷
     */
    void put(String key, CatalogSnapshot snapshot);
}
