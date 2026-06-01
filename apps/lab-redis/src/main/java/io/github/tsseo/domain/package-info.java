/**
 * 도메인(Domain) 레이어 — domain 패키지
 *
 * <h2>책임</h2>
 * <ul>
 *   <li>{@code Entity}: 식별자를 가진 핵심 비즈니스 객체. 상태와 행위를 함께 보유.</li>
 *   <li>{@code Service}: 도메인 로직을 수행하는 서비스. Repository(인터페이스)를 주입받아 사용 가능.</li>
 *   <li>{@code DomainService}: 특정 엔티티에 속하지 않는 도메인 로직. 외부 의존성 주입 없음.</li>
 *   <li>{@code Repository 인터페이스(포트)}: 영속성 계층에 대한 추상화. 구현체는 infrastructure 레이어에 존재.</li>
 *   <li>{@code Command}: Service 의 입력 파라미터 (record 권장)</li>
 *   <li>{@code Info}: Service 의 출력 결과 (record 권장)</li>
 * </ul>
 *
 * <h2>의존 방향 — DIP(의존 역전 원칙)의 중심</h2>
 * <pre>
 *   domain → (없음: 어떤 상위 레이어에도, 어떤 인프라 기술에도 의존하지 않음)
 * </pre>
 * domain 레이어는 순수한 Java 코드로만 구성되어야 한다.
 * Spring Data, JPA, Redis 등 외부 기술에 직접 의존하면 안 된다.
 * Repository 인터페이스를 domain 레이어에 정의함으로써,
 * 실제 구현체(infrastructure)가 domain 을 향해 의존하는 방향으로 역전된다.
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>domain 은 infrastructure 패키지를 import 해서는 안 된다.</li>
 *   <li>domain 은 application, interfaces 패키지를 import 해서는 안 된다.</li>
 *   <li>이 레이어가 가장 안정적이며, 테스트가 가장 용이해야 한다.</li>
 * </ul>
 */
package io.github.tsseo.domain;
