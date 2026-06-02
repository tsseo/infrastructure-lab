package io.github.tsseo.domain.catalog;

import java.util.Optional;

/**
 * 카탈로그 저장소 포트 (L2 — Redis).
 *
 * <p>이 인터페이스는 도메인 레이어에 위치하는 포트(port)다.
 * 의존 역전 원칙(DIP)에 따라 도메인이 포트를 정의하고,
 * 실제 구현({@code RedisCatalogRepository})은 인프라 레이어에 존재한다.</p>
 *
 * <p>도메인 코드는 Redis, JPA 등 기술적 세부 사항을 전혀 알지 못하며,
 * 오직 이 인터페이스에만 의존한다. 기술이 교체되어도 도메인 코드는 변경이 없다.</p>
 */
public interface CatalogRepository {

    /**
     * 키로 카탈로그 스냅샷을 조회한다.
     *
     * <p>키가 Redis 에 없으면 {@code Optional.empty()} 를 반환한다.
     * JSON 역직렬화는 이 메서드 내부에서 발생하며, LARGE 페이로드의 경우
     * 수십 ms 의 CPU 비용을 유발할 수 있다.</p>
     *
     * @param key Redis 키 (예: "catalog:large")
     * @return 스냅샷이 존재하면 {@code Optional.of(snapshot)}, 없으면 {@code Optional.empty()}
     */
    Optional<CatalogSnapshot> findById(String key);

    /**
     * 카탈로그 스냅샷을 저장한다.
     *
     * <p>JSON 직렬화 후 Redis 에 문자열로 저장한다.
     * TTL 을 별도로 설정하지 않으면 키가 만료되지 않는다.</p>
     *
     * @param key      Redis 키 (예: "catalog:large")
     * @param snapshot 저장할 카탈로그 스냅샷
     */
    void save(String key, CatalogSnapshot snapshot);
}
