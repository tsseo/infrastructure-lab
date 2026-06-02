package io.github.tsseo.domain.catalog;

import java.time.Instant;
import java.util.List;

/**
 * 카탈로그 서비스 출력 DTO (도메인 Info).
 *
 * <p>도메인 레이어의 {@link CatalogService} 가 반환하는 결과 객체다.
 * 레이어 규칙에 따라 도메인 Info 는 {@link CatalogSnapshot} 을 직접 노출하지 않고
 * 필요한 필드만 추출하여 반환한다.</p>
 *
 * <p>다만 이 실험에서는 역직렬화 비용을 상위 레이어까지 관통시키기 위해
 * {@code products} 목록을 포함한다. 덕분에 HTTP 응답 직렬화 시에도 CPU 비용이 발생한다.</p>
 *
 * @param id           카탈로그 식별자
 * @param generatedAt  스냅샷 생성 시각
 * @param productCount 포함된 상품 수
 * @param products     상품 목록 (상위 레이어를 통해 HTTP 응답까지 전달됨)
 */
public record CatalogInfo(
        String id,
        Instant generatedAt,
        int productCount,
        List<Product> products
) {

    /**
     * {@link CatalogSnapshot} 으로부터 {@code CatalogInfo} 를 생성하는 팩토리 메서드.
     *
     * @param snapshot Redis 에서 읽어온 도메인 스냅샷
     * @return CatalogInfo 인스턴스
     */
    public static CatalogInfo from(CatalogSnapshot snapshot) {
        return new CatalogInfo(
                snapshot.id(),
                snapshot.generatedAt(),
                snapshot.products().size(),
                snapshot.products()
        );
    }
}
