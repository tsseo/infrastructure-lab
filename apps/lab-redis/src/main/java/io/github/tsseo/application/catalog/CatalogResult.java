package io.github.tsseo.application.catalog;

import io.github.tsseo.domain.catalog.CatalogInfo;
import io.github.tsseo.domain.catalog.Product;

import java.time.Instant;
import java.util.List;

/**
 * 카탈로그 조회 유스케이스 출력 결과 (Facade 출력 DTO).
 *
 * <p>도메인의 {@link CatalogInfo} 를 응용 레이어 DTO 로 변환한 결과 객체다.
 * 현재 구조는 {@link CatalogInfo} 와 동일하지만,
 * 도메인 모델이 변경되어도 응용 레이어 계약이 깨지지 않도록 명시적으로 분리한다.</p>
 *
 * @param id           카탈로그 식별자
 * @param generatedAt  스냅샷 생성 시각
 * @param productCount 포함된 상품 수
 * @param products     상품 목록
 */
public record CatalogResult(
        String id,
        Instant generatedAt,
        int productCount,
        List<Product> products
) {

    /**
     * 도메인 {@link CatalogInfo} 로부터 {@code CatalogResult} 를 생성하는 팩토리 메서드.
     *
     * @param info 도메인 서비스 출력
     * @return CatalogResult 인스턴스
     */
    public static CatalogResult from(CatalogInfo info) {
        return new CatalogResult(
                info.id(),
                info.generatedAt(),
                info.productCount(),
                info.products()
        );
    }
}
