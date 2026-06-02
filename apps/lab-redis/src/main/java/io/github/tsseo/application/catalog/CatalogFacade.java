package io.github.tsseo.application.catalog;

import io.github.tsseo.domain.catalog.CatalogService;
import org.springframework.stereotype.Component;

/**
 * 카탈로그 조회 유스케이스 Facade.
 *
 * <p>응용 레이어의 진입점으로, 하나의 유스케이스("카탈로그 조회")를 조율한다.
 * {@code Controller → Facade → Service} 흐름을 따른다.</p>
 *
 * <p>현재 유스케이스는 단일 서비스 호출로 구성되지만,
 * 나중에 감사 로그, 알림, 다른 서비스 호출이 추가되어도
 * Facade 내부만 수정하면 되므로 Controller 는 변경이 없다.</p>
 */
@Component
public class CatalogFacade {

    private final CatalogService catalogService;

    public CatalogFacade(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * 카탈로그 조회 유스케이스를 실행한다.
     *
     * @param criteria 조회 조건 (페이로드 크기, L1 캐시 사용 여부)
     * @return 카탈로그 조회 결과
     */
    public CatalogResult getCatalog(CatalogCriteria criteria) {
        return CatalogResult.from(
                catalogService.getCatalog(criteria.size(), criteria.useL1())
        );
    }
}
