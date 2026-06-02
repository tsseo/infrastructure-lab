package io.github.tsseo.infrastructure.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tsseo.domain.catalog.CatalogGenerator;
import io.github.tsseo.domain.catalog.CatalogRepository;
import io.github.tsseo.domain.catalog.CatalogSnapshot;
import io.github.tsseo.domain.catalog.PayloadSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 앱 기동 시 Redis 에 카탈로그 데이터를 시딩하는 컴포넌트.
 *
 * <p>{@link ApplicationRunner} 를 구현하므로 Spring Boot 가 완전히 기동된 직후
 * {@link #run(ApplicationArguments)} 가 자동으로 호출된다.
 * 모든 {@link PayloadSize} 에 대해 카탈로그를 생성하여 Redis 에 저장하므로,
 * 부하 테스트를 시작하기 전 데이터가 항상 준비되어 있다.</p>
 *
 * <h2>Redis 연결 실패 처리</h2>
 * Redis 가 아직 준비되지 않은 경우를 대비해 예외를 잡아 경고 로그만 출력하고
 * 앱이 계속 기동되도록 한다. 정상 환경에서는 docker-compose 가 Redis 를 먼저 올리므로
 * 이 경우는 발생하지 않는다.
 */
@Component
public class CatalogSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogSeeder.class);

    private final CatalogRepository catalogRepository;
    private final CatalogGenerator catalogGenerator;

    /**
     * JSON 바이트 크기 측정에만 사용한다 (저장은 Repository 가 담당).
     * Spring Boot 가 자동 구성한 ObjectMapper 를 재사용하여 직렬화 결과를 일관되게 유지한다.
     */
    private final ObjectMapper objectMapper;

    public CatalogSeeder(CatalogRepository catalogRepository,
                         CatalogGenerator catalogGenerator,
                         ObjectMapper objectMapper) {
        this.catalogRepository = catalogRepository;
        this.catalogGenerator = catalogGenerator;
        this.objectMapper = objectMapper;
    }

    /**
     * 앱 기동 완료 후 모든 PayloadSize 에 대해 카탈로그를 Redis 에 시딩한다.
     *
     * <p>이미 해당 키가 Redis 에 존재해도 덮어쓴다.
     * 항상 최신 생성기 결과로 초기화하므로 실험 환경이 일관되게 유지된다.</p>
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("=== CatalogSeeder: Redis 시딩 시작 ===");

        for (PayloadSize size : PayloadSize.values()) {
            seedForSize(size);
        }

        log.info("=== CatalogSeeder: Redis 시딩 완료 ===");
    }

    /**
     * 특정 PayloadSize 에 대해 시딩을 수행한다.
     * Redis 연결 실패 등 예외 발생 시 경고를 출력하고 다음 크기로 넘어간다.
     *
     * @param size 시딩할 페이로드 크기
     */
    private void seedForSize(PayloadSize size) {
        String key = "catalog:" + size.name().toLowerCase();
        try {
            CatalogSnapshot snapshot = catalogGenerator.generate(size);

            // JSON 바이트 크기 측정 (로그 목적, 실제 저장은 Repository 가 처리)
            long byteSize = measureJsonBytes(snapshot);

            // Redis 에 저장
            catalogRepository.save(key, snapshot);

            log.info("[시딩 완료] key={}, 상품수={}, JSON크기={}바이트 (약 {}KB)",
                    key,
                    snapshot.products().size(),
                    byteSize,
                    byteSize / 1024);

        } catch (Exception e) {
            // Redis 연결 실패 등 예외 시 앱 기동을 막지 않는다
            log.warn("[시딩 실패] key={} 시딩 중 오류 발생 — Redis 가 준비됐는지 확인하세요. 원인: {}",
                    key, e.getMessage());
        }
    }

    /**
     * 스냅샷을 JSON 으로 직렬화했을 때의 바이트 수를 반환한다.
     * 실제 저장 없이 크기만 측정하는 용도다.
     *
     * @param snapshot 측정 대상 스냅샷
     * @return JSON UTF-8 바이트 수
     */
    private long measureJsonBytes(CatalogSnapshot snapshot) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(snapshot);
            return bytes.length;
        } catch (JsonProcessingException e) {
            // 크기 측정 실패는 치명적이지 않으므로 -1 을 반환
            log.debug("JSON 크기 측정 실패: {}", e.getMessage());
            return -1;
        }
    }
}
