/**
 * 응용(Application) 레이어 — application 패키지
 *
 * <h2>책임</h2>
 * <ul>
 *   <li>{@code Facade}: 하나의 유스케이스를 조합하는 진입점.
 *       여러 도메인 Service 를 호출하여 트랜잭션 경계를 조율한다.</li>
 *   <li>{@code Criteria}: Facade 의 입력 파라미터를 담는 불변 객체 (record 권장)</li>
 *   <li>{@code Result}: Facade 의 출력 결과를 담는 불변 객체 (record 권장)</li>
 * </ul>
 *
 * <h2>의존 방향</h2>
 * <pre>
 *   application → domain (Service, Repository 인터페이스)
 * </pre>
 * Facade 는 {@code domain} 레이어의 Service 만 호출한다.
 * Repository 인터페이스를 직접 호출하거나 JPA/Redis 등 인프라 구현체를 참조해서는 안 된다.
 *
 * <h2>DIP(의존 역전 원칙) 관점</h2>
 * Facade 는 domain 의 인터페이스(포트)에만 의존하므로,
 * 실제 DB 기술이 변경되어도 응용 레이어 코드는 수정이 필요 없다.
 */
package io.github.tsseo.application;
