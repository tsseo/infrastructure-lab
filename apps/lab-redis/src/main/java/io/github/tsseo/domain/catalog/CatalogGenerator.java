package io.github.tsseo.domain.catalog;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 카탈로그 스냅샷 생성기 — 테스트/시딩 전용 도메인 헬퍼.
 *
 * <p>랜덤이 아닌 인덱스 기반의 결정론적 데이터를 생성한다.
 * 결과가 항상 동일하므로 재현 가능한 실험 환경을 보장한다.</p>
 *
 * <p>각 {@link Product} 의 {@code tags}, {@code attributes} 에
 * 의도적으로 다수의 원소를 넣어 Jackson 역직렬화 비용을 높인다.
 * LARGE(800개) 기준으로 JSON 크기는 약 500KB 이상이다.</p>
 */
@Component
public class CatalogGenerator {

    // ─── 데이터 풀 (인덱스로 순환하여 다양성 부여) ─────────────────────────────

    private static final String[] BRANDS = {
            "BrandAlpha", "BrandBeta", "BrandGamma", "BrandDelta", "BrandEpsilon"
    };

    private static final String[] CATEGORIES = {
            "전자제품", "패션", "식품", "스포츠", "생활용품", "도서", "완구", "자동차용품"
    };

    private static final String[] COLORS = {
            "레드", "블루", "그린", "블랙", "화이트", "옐로우", "퍼플", "오렌지"
    };

    private static final String[] SIZES = {"XS", "S", "M", "L", "XL", "XXL", "FREE"};

    private static final String[] MATERIALS = {
            "면", "폴리에스터", "나일론", "가죽", "울", "실크", "데님", "린넨"
    };

    /**
     * 주어진 {@link PayloadSize} 에 맞는 {@link CatalogSnapshot} 을 생성한다.
     *
     * <p>인덱스 기반으로 필드 값이 순환되므로 같은 크기를 여러 번 호출해도
     * 동일한 결과가 반환된다. 부하 테스트의 재현성을 보장하기 위함이다.</p>
     *
     * @param size 생성할 페이로드 크기 (상품 수 결정)
     * @return 결정론적 카탈로그 스냅샷
     */
    public CatalogSnapshot generate(PayloadSize size) {
        int count = size.getProductCount();
        String id = "catalog:" + size.name().toLowerCase();

        List<Product> products = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            products.add(buildProduct(i));
        }

        return new CatalogSnapshot(id, Instant.now(), products);
    }

    // ─── 내부 빌더 메서드 ────────────────────────────────────────────────────

    /**
     * 인덱스를 기반으로 다양한 값을 가지는 {@link Product} 를 생성한다.
     *
     * <p>tags 와 attributes 를 풍부하게 구성하여 Jackson 파싱 비용을 높인다.
     * description 도 상품별로 길이를 달리해 JSON 크기를 늘린다.</p>
     *
     * @param idx 상품 순번 (0-based)
     * @return 생성된 상품
     */
    private Product buildProduct(int idx) {
        String brand = BRANDS[idx % BRANDS.length];
        String category = CATEGORIES[idx % CATEGORIES.length];
        String color = COLORS[idx % COLORS.length];
        String size = SIZES[idx % SIZES.length];
        String material = MATERIALS[idx % MATERIALS.length];

        // 가격: 1,000원 ~ 500,000원 사이를 인덱스로 분산
        BigDecimal price = BigDecimal.valueOf(1000L + (long) (idx % 499) * 1000);

        // 상품명: 브랜드 + 카테고리 + 인덱스 조합
        String name = brand + " " + category + " 상품-" + String.format("%04d", idx);

        // 설명: 충분한 길이로 JSON 크기를 늘린다
        String description = buildDescription(idx, brand, category, color, material);

        // 태그: 8개 고정 (List 파싱 비용 유발)
        List<String> tags = buildTags(idx, category, color, size);

        // 속성 맵: 10개 키-값 쌍 (Map 파싱 비용 유발)
        Map<String, String> attributes = buildAttributes(idx, color, size, material);

        return new Product(
                (long) idx + 1,
                name,
                description,
                price,
                brand,
                tags,
                attributes
        );
    }

    /**
     * 상품 설명 문자열을 생성한다.
     * 길이를 의도적으로 늘려 JSON 바이트 수를 증가시킨다.
     */
    private String buildDescription(int idx, String brand, String category, String color, String material) {
        return String.format(
                "[%s] %s 카테고리의 프리미엄 상품입니다. 색상: %s, 소재: %s. " +
                "상품 번호 %d 번. 높은 품질과 합리적인 가격으로 제공되는 이 제품은 " +
                "일상 생활에서 탁월한 성능을 발휘합니다. 고객 만족을 최우선으로 하는 " +
                "%s 의 대표 상품으로, 다양한 용도에 활용 가능합니다.",
                brand, category, color, material, idx + 1, brand
        );
    }

    /**
     * 태그 목록을 생성한다 (8개 고정).
     */
    private List<String> buildTags(int idx, String category, String color, String size) {
        return List.of(
                category,
                color,
                size,
                "신상품",
                "베스트셀러",
                "할인중",
                "tag-" + (idx % 20),
                "series-" + (idx % 10)
        );
    }

    /**
     * 속성 맵을 생성한다 (10개 키-값 쌍 고정).
     */
    private Map<String, String> buildAttributes(int idx, String color, String size, String material) {
        // LinkedHashMap 으로 순서를 보장하여 직렬화 결과를 일관되게 유지
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("color", color);
        attrs.put("size", size);
        attrs.put("material", material);
        attrs.put("origin", "국내산");
        attrs.put("warranty", (idx % 3 + 1) + "년");
        attrs.put("weight", (100 + idx % 900) + "g");
        attrs.put("width", (10 + idx % 50) + "cm");
        attrs.put("height", (5 + idx % 30) + "cm");
        attrs.put("depth", (2 + idx % 20) + "cm");
        attrs.put("sku", "SKU-" + String.format("%08d", idx + 1));
        return attrs;
    }
}
