package io.github.tsseo.interfaces.api.catalog;

import io.github.tsseo.application.catalog.CatalogCriteria;
import io.github.tsseo.application.catalog.CatalogFacade;
import io.github.tsseo.application.catalog.CatalogResult;
import io.github.tsseo.domain.catalog.PayloadSize;
import io.github.tsseo.interfaces.api.ApiResponse;
import io.github.tsseo.support.error.CoreException;
import io.github.tsseo.support.error.ErrorType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 카탈로그 조회 REST API 컨트롤러 (v1).
 *
 * <h2>엔드포인트</h2>
 * {@code GET /api/catalog?size=large&l1=off}
 *
 * <h2>쿼리 파라미터</h2>
 * <ul>
 *   <li>{@code size} — 페이로드 크기. {@code small} 또는 {@code large} (기본값: large)</li>
 *   <li>{@code l1}   — L1(Caffeine) 캐시 사용 여부. {@code on}/{@code true} 이면 활성화 (기본값: off)</li>
 * </ul>
 *
 * <h2>실험 시나리오</h2>
 * <ul>
 *   <li>{@code size=large&l1=off}: 매 요청마다 Redis 조회 + 대용량 JSON 역직렬화 → CPU 부하</li>
 *   <li>{@code size=large&l1=on}: 첫 요청 후 L1 캐시 히트 → 역직렬화 없음 → 낮은 CPU</li>
 *   <li>{@code size=small&l1=off}: 작은 JSON 역직렬화 → 낮은 CPU</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/catalog")
public class CatalogV1Controller {

    private final CatalogFacade catalogFacade;

    public CatalogV1Controller(CatalogFacade catalogFacade) {
        this.catalogFacade = catalogFacade;
    }

    /**
     * 카탈로그를 조회한다.
     *
     * <p>쿼리 파라미터로 페이로드 크기와 L1 캐시 사용 여부를 제어하여
     * 다양한 부하 시나리오를 실험할 수 있다.</p>
     *
     * @param size 페이로드 크기 문자열 ("small" / "large", 기본값: "large")
     * @param l1   L1 캐시 활성화 문자열 ("on"/"true"=활성, 그 외=비활성, 기본값: "off")
     * @return ApiResponse 봉투로 감싼 카탈로그 응답
     * @throws CoreException 알 수 없는 size 값인 경우 {@link ErrorType#BAD_REQUEST}
     */
    @GetMapping
    public ApiResponse<CatalogV1Dto.Response> getCatalog(
            @RequestParam(defaultValue = "large") String size,
            @RequestParam(name = "l1", defaultValue = "off") String l1
    ) {
        // size 파라미터 → PayloadSize 변환
        PayloadSize payloadSize = parsePayloadSize(size);

        // l1 파라미터 → boolean 변환 ("on" 또는 "true" 이면 L1 활성화)
        boolean useL1 = "on".equalsIgnoreCase(l1) || "true".equalsIgnoreCase(l1);

        CatalogCriteria criteria = new CatalogCriteria(payloadSize, useL1);
        CatalogResult result = catalogFacade.getCatalog(criteria);

        return ApiResponse.success(CatalogV1Dto.Response.from(result));
    }

    /**
     * 문자열을 {@link PayloadSize} enum 으로 변환한다.
     *
     * <p>대소문자를 무시하여 파싱한다.
     * 알 수 없는 값이 들어오면 {@link CoreException}({@link ErrorType#BAD_REQUEST}) 를 던진다.</p>
     *
     * @param size 파싱할 문자열 (예: "large", "SMALL")
     * @return 해당하는 PayloadSize
     * @throws CoreException 알 수 없는 값일 경우
     */
    private PayloadSize parsePayloadSize(String size) {
        try {
            return PayloadSize.valueOf(size.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CoreException(
                    ErrorType.BAD_REQUEST,
                    "알 수 없는 size 파라미터입니다: '" + size + "'. 허용 값: small, large"
            );
        }
    }
}
