package io.github.tsseo.domain.catalog;

import java.time.Instant;
import java.util.List;

/**
 * 카탈로그 스냅샷 — Redis 에 저장되는 도메인 모델.
 *
 * <p>이 객체 전체가 JSON 으로 직렬화되어 Redis 에 저장된다.
 * LARGE 사이즈일 경우 약 800개의 {@link Product} 를 포함하며,
 * JSON 크기는 100KB 이상이 된다. 이 크기 때문에
 * Redis 에서 읽어온 JSON 을 Jackson 으로 역직렬화할 때 CPU 비용이 크다.</p>
 *
 * <p>Jackson 역직렬화를 위해 record 가 제공하는 기본 생성자(canonical constructor) 를 그대로 사용한다.
 * Jackson 2.12 이상에서는 record 를 별도 어노테이션 없이 역직렬화할 수 있다.</p>
 *
 * @param id          카탈로그 식별자 (예: "catalog:large")
 * @param generatedAt 스냅샷 생성 시각 (ISO-8601 형태로 직렬화됨)
 * @param products    상품 목록 (SMALL: 25개, LARGE: 800개)
 */
public record CatalogSnapshot(
        String id,
        Instant generatedAt,
        List<Product> products
) {
}
