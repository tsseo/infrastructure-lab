package io.github.tsseo.interfaces.api.catalog;

import io.github.tsseo.application.catalog.CatalogResult;
import io.github.tsseo.domain.catalog.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 카탈로그 API v1 DTO 컨테이너.
 *
 * <p>HTTP 요청/응답에 사용하는 DTO 를 중첩 record 로 묶어 관리한다.
 * 내부 record 이름만으로 계층을 구분할 수 있고, 파일 수가 줄어든다.</p>
 */
public class CatalogV1Dto {

    // ─── 생성 방지 ────────────────────────────────────────────────────────────
    // 이 클래스는 DTO 네임스페이스 역할만 하므로 인스턴스 생성을 막는다.
    private CatalogV1Dto() {
    }

    /**
     * 카탈로그 조회 API 응답 DTO — 요약 형태.
     *
     * <p>실험에서 역직렬화 비용만 변수로 분리하기 위해 응답을 작게 유지한다.
     * Redis 에서 읽어온 전체 JSON 은 Jackson 이 {@code CatalogSnapshot} 으로 완전히 역직렬화하고,
     * 이 DTO 는 역직렬화된 객체 그래프를 순회하여 집계 값만 뽑아낸다.
     * 따라서 CPU/역직렬화 비용은 그대로 발생하되, HTTP 응답 직렬화 비용과
     * 네트워크 이그레스(egress)는 최소화된다.</p>
     *
     * <p>{@code totalPrice} 는 모든 상품의 가격을 순회하여 합산한 값으로,
     * JIT 또는 Jackson 이 상품 목록을 실체화(materialize)하지 않고 건너뛸 수 없도록
     * 의도적으로 전체 객체 그래프를 탐색한다.</p>
     *
     * @param id               카탈로그 식별자
     * @param generatedAt      스냅샷 생성 시각
     * @param productCount     포함된 상품 수
     * @param totalPrice       전체 상품 가격 합계 (역직렬화 완전성 보장용)
     * @param firstProductName 첫 번째 상품명 (없으면 null)
     */
    public record Response(
            String id,
            Instant generatedAt,
            int productCount,
            BigDecimal totalPrice,
            String firstProductName
    ) {

        /**
         * 응용 레이어 {@link CatalogResult} 로부터 HTTP 응답 DTO 를 생성하는 팩토리 메서드.
         *
         * <p>역직렬화된 상품 목록 전체를 순회하여 {@code totalPrice} 를 계산한다.
         * 이로써 Jackson 이 생성한 객체 그래프가 실제로 사용되었음을 보장하고,
         * JIT 최적화(dead-code elimination)를 방지한다.</p>
         *
         * @param result Facade 반환 결과
         * @return HTTP 응답 요약 DTO
         */
        public static Response from(CatalogResult result) {
            List<Product> products = result.products();

            // 전체 상품 가격 합산 — 역직렬화된 객체 그래프를 반드시 순회하도록 의도된 연산
            BigDecimal totalPrice = products.stream()
                    .map(Product::price)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 첫 번째 상품명 (없으면 null)
            String firstProductName = products.isEmpty() ? null : products.get(0).name();

            return new Response(
                    result.id(),
                    result.generatedAt(),
                    result.productCount(),
                    totalPrice,
                    firstProductName
            );
        }
    }
}
