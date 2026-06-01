/**
 * 인프라(Infrastructure) 레이어 — infrastructure 패키지
 *
 * <h2>책임</h2>
 * <ul>
 *   <li>{@code Repository 구현체}: domain 레이어에 정의된 Repository 인터페이스(포트)의 실제 구현.
 *       JPA, Redis, 외부 HTTP API 등 기술에 직접 의존한다.</li>
 *   <li>외부 시스템 연동 어댑터 (예: 결제 게이트웨이 클라이언트, S3 업로더 등)</li>
 * </ul>
 *
 * <h2>의존 방향 — 의존 역전(DIP)</h2>
 * <pre>
 *   infrastructure → domain (Repository 인터페이스를 구현)
 * </pre>
 * infrastructure 는 domain 의 Repository 인터페이스를 implements 한다.
 * 이로 인해 의존 방향이 역전된다:
 *   고수준(domain) ← 저수준(infrastructure)
 * domain 은 infrastructure 를 모르지만,
 * infrastructure 는 domain 의 인터페이스를 알고 구현한다.
 *
 * <h2>핵심 규칙</h2>
 * <ul>
 *   <li>infrastructure 구현체는 Spring Bean 으로 등록되어 domain Service 에 주입된다.</li>
 *   <li>application, interfaces 레이어를 직접 참조해서는 안 된다.</li>
 *   <li>기술 교체(예: Redis → Caffeine)는 이 레이어만 수정하면 된다.</li>
 * </ul>
 */
package io.github.tsseo.infrastructure;
