package io.github.tsseo.support.error;

import org.springframework.http.HttpStatus;

/**
 * 애플리케이션 전역 에러 타입 열거형.
 *
 * <p>참고 프로젝트(Kotlin)의 {@code ErrorType} 을 Java enum 으로 포팅한 것이다.
 * 각 에러 타입은 HTTP 상태 코드({@link HttpStatus}), 에러 코드 문자열, 사용자 메시지를 보유한다.</p>
 *
 * <h2>사용 예</h2>
 * <pre>{@code
 * throw new CoreException(ErrorType.NOT_FOUND);
 * throw new CoreException(ErrorType.BAD_REQUEST, "요청 파라미터가 잘못되었습니다.");
 * }</pre>
 *
 * <h2>확장 방법</h2>
 * 도메인별 에러 타입이 필요하면 이 enum 에 항목을 추가하거나,
 * 별도 도메인 모듈에서 이 타입을 참조하는 예외 클래스를 정의한다.
 */
public enum ErrorType {

    // ─── 범용 에러 ─────────────────────────────────────────────────────────

    /** 서버 내부 오류 — 예상치 못한 예외 발생 시 사용 */
    INTERNAL_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "일시적인 오류가 발생했습니다."
    ),

    /** 잘못된 요청 — 클라이언트가 유효하지 않은 파라미터를 전송한 경우 */
    BAD_REQUEST(
            HttpStatus.BAD_REQUEST,
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "잘못된 요청입니다."
    ),

    /** 인증 실패 — 유효한 인증 정보가 없는 경우 */
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            "인증에 실패했습니다."
    ),

    /** 접근 권한 없음 — 인증은 되었지만 해당 리소스에 접근 권한이 없는 경우 */
    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            "접근 권한이 없습니다."
    ),

    /** 리소스 없음 — 요청한 리소스가 존재하지 않는 경우 */
    NOT_FOUND(
            HttpStatus.NOT_FOUND,
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            "존재하지 않는 요청입니다."
    ),

    /** 충돌 — 이미 존재하는 리소스를 중복 생성하려는 경우 */
    CONFLICT(
            HttpStatus.CONFLICT,
            HttpStatus.CONFLICT.getReasonPhrase(),
            "이미 존재하는 리소스입니다."
    );

    // ─── 필드 ──────────────────────────────────────────────────────────────

    /** HTTP 응답 상태 코드 */
    private final HttpStatus status;

    /** 에러 코드 문자열 (예: "Not Found", "BAD_REQUEST") */
    private final String code;

    /** 사용자에게 표시할 에러 메시지 */
    private final String message;

    // ─── 생성자 ────────────────────────────────────────────────────────────

    ErrorType(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    // ─── 접근자 ────────────────────────────────────────────────────────────

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
