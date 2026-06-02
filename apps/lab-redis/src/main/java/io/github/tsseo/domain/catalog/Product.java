package io.github.tsseo.domain.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 상품 도메인 모델.
 *
 * <p>JSON 역직렬화 비용을 높이기 위해 의도적으로 필드를 풍부하게 구성했다.
 * {@code tags} (List)와 {@code attributes} (Map)는 중첩 컬렉션으로,
 * Jackson 이 각 원소를 재귀적으로 파싱해야 하므로 CPU 비용이 크다.
 * 이 비용이 LARGE 페이로드에서 JVM CPU 포화 → Lettuce I/O 스레드 지연 →
 * Redis 커맨드 타임아웃처럼 보이는 현상의 핵심 원인이다.</p>
 *
 * <p>Jackson 역직렬화를 위해 기본 생성자가 필요하므로, compact constructor 로 대체하지 않는다.
 * Spring Boot 의 ObjectMapper 는 Jackson Databind 를 사용하며,
 * record 에 대해서도 @JsonCreator 없이 기본 역직렬화가 가능하다 (Jackson 2.12+).</p>
 *
 * @param id          상품 식별자
 * @param name        상품명
 * @param description 상품 설명 (길이가 있어야 JSON 크기가 커진다)
 * @param price       판매가 (BigDecimal → JSON 에서 정밀도 보존)
 * @param brand       브랜드명
 * @param tags        검색 태그 목록 (List 파싱 비용 유발)
 * @param attributes  추가 속성 맵 (Map 파싱 비용 유발)
 */
public record Product(
        long id,
        String name,
        String description,
        BigDecimal price,
        String brand,
        List<String> tags,
        Map<String, String> attributes
) {
}
