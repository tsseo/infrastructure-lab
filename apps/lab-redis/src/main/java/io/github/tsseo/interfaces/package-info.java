/**
 * 표현(Presentation) 레이어 — interfaces 패키지
 *
 * <h2>책임</h2>
 * <ul>
 *   <li>HTTP 요청/응답을 처리하는 {@code @RestController} 클래스</li>
 *   <li>요청 데이터를 담는 Request DTO (예: {@code CreateOrderRequest})</li>
 *   <li>응답 데이터를 감싸는 {@code ApiResponse} 래퍼</li>
 * </ul>
 *
 * <h2>의존 방향</h2>
 * <pre>
 *   interfaces → application (Facade 호출)
 * </pre>
 * {@code ApiResponse} 는 이 레이어의 {@code io.github.tsseo.interfaces.api} 패키지에 직접 포함된다.
 * 공유 모듈이 아닌 앱별 복사 방식(참고 프로젝트 컨벤션)을 따른다.
 * interfaces 레이어는 {@code application} 레이어의 Facade 만 호출한다.
 * domain 레이어나 infrastructure 레이어를 직접 참조해서는 안 된다.
 *
 * <h2>주의</h2>
 * {@code interfaces} 는 Java 예약어가 아니므로 패키지 이름으로 사용 가능하다.
 */
package io.github.tsseo.interfaces;
