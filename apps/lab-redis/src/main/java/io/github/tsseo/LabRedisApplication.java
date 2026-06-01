package io.github.tsseo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * lab-redis 애플리케이션 진입점.
 *
 * 베이스 패키지: io.github.tsseo
 * 이 패키지 아래 모든 @Component, @Configuration 을 자동 스캔한다.
 * modules/redis (io.github.tsseo.config.redis.*) 도 같은 루트 패키지에 있으므로
 * 별도 scanBasePackages 설정 없이 자동으로 인식된다.
 */
@SpringBootApplication
public class LabRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabRedisApplication.class, args);
    }
}
