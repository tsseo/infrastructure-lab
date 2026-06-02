package io.github.tsseo.application.catalog;

import io.github.tsseo.domain.catalog.PayloadSize;

/**
 * 카탈로그 조회 유스케이스 입력 파라미터 (Facade 입력 DTO).
 *
 * <p>Facade 에 전달되는 불변 파라미터 객체다.
 * {@code record} 를 사용하여 불변성과 간결함을 동시에 확보한다.</p>
 *
 * @param size   조회할 페이로드 크기 (SMALL / LARGE)
 * @param useL1  {@code true} 이면 L1(Caffeine) 캐시를 먼저 확인한다
 */
public record CatalogCriteria(
        PayloadSize size,
        boolean useL1
) {
}
