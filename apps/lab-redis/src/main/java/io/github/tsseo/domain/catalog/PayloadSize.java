package io.github.tsseo.domain.catalog;

/**
 * 페이로드 크기 열거형 — 실험 시나리오 제어용.
 *
 * <p>각 값은 {@link CatalogGenerator} 가 생성할 상품 수를 결정한다.
 * TINY 는 수백 바이트의 극소형 페이로드로 핫 키 실험에 사용된다.
 * SMALL 은 몇 KB 수준이어서 Jackson 역직렬화 비용이 무시할 만큼 작고,
 * LARGE 는 100KB 이상이어서 역직렬화 시 JVM CPU 를 수십 ms 점유한다.
 * 이 CPU 점유가 Lettuce I/O 스레드 스케줄링 지연으로 이어져
 * Redis 커맨드 타임아웃처럼 관측되는 현상을 재현한다.</p>
 *
 * <ul>
 *   <li>TINY  (2개)   — 직렬화 결과 약 400~600 바이트 (핫 키 실험용)</li>
 *   <li>SMALL (25개)  — 직렬화 결과 약 15~20 KB</li>
 *   <li>LARGE (800개) — 직렬화 결과 약 500~600 KB</li>
 * </ul>
 */
public enum PayloadSize {

    /**
     * 초소형 페이로드 — 상품 2개 (약 400~600 바이트).
     * Redis 클러스터 핫 키 실험에 사용된다.
     * 값이 매우 작으므로 하나의 클러스터 노드에 읽기 요청이 집중되어
     * 앱 측 역직렬화 비용 없이 Redis 노드 CPU/네트워크 병목을 관측할 수 있다.
     */
    TINY(2),

    /**
     * 소형 페이로드 — 상품 25개.
     * 역직렬화 비용이 낮아서 Redis 타임아웃이 발생하지 않는 정상 기준선이다.
     */
    SMALL(25),

    /**
     * 대형 페이로드 — 상품 800개.
     * 역직렬화 CPU 비용이 높아서 JVM 이 바쁜 경우 Redis 타임아웃처럼 보이는 현상을 유발한다.
     */
    LARGE(800);

    /** 이 페이로드 크기에서 생성할 상품 수 */
    private final int productCount;

    PayloadSize(int productCount) {
        this.productCount = productCount;
    }

    /**
     * 이 페이로드 크기에 해당하는 상품 수를 반환한다.
     *
     * @return 상품 수
     */
    public int getProductCount() {
        return productCount;
    }
}
