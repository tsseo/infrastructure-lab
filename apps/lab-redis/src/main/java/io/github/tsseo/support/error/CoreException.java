package io.github.tsseo.support.error;

/**
 * 애플리케이션 전역 비즈니스 예외 클래스.
 *
 * <p>참고 프로젝트(Kotlin)의 {@code CoreException} 을 Java 로 포팅한 것이다.
 * 모든 도메인/응용 레이어의 비즈니스 예외는 이 클래스를 사용한다.</p>
 *
 * <h2>사용 예</h2>
 * <pre>{@code
 * // 기본 메시지 사용 (ErrorType 에 정의된 메시지)
 * throw new CoreException(ErrorType.NOT_FOUND);
 *
 * // 커스텀 메시지 사용 (상세한 문맥 정보 포함)
 * throw new CoreException(ErrorType.NOT_FOUND, "사용자 ID " + userId + " 를 찾을 수 없습니다.");
 * }</pre>
 *
 * <h2>처리 방법</h2>
 * {@code @RestControllerAdvice} 에서 이 예외를 잡아
 * {@link ErrorType#getStatus()} 로 HTTP 상태 코드를,
 * {@link ErrorType#getCode()} 로 에러 코드를 추출하여
 * {@code ApiResponse.fail()} 형태로 응답한다.
 */
public class CoreException extends RuntimeException {

    /** 이 예외의 원인이 되는 에러 타입 */
    private final ErrorType errorType;

    /**
     * 기본 메시지를 사용하는 생성자.
     * 예외 메시지로 {@link ErrorType#getMessage()} 가 사용된다.
     *
     * @param errorType 에러 타입
     */
    public CoreException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }

    /**
     * 커스텀 메시지를 사용하는 생성자.
     * 커스텀 메시지가 있으면 해당 메시지를, 없으면 {@link ErrorType#getMessage()} 를 사용한다.
     *
     * @param errorType     에러 타입
     * @param customMessage 커스텀 에러 메시지 (null 이면 errorType 의 기본 메시지 사용)
     */
    public CoreException(ErrorType errorType, String customMessage) {
        super(customMessage != null ? customMessage : errorType.getMessage());
        this.errorType = errorType;
    }

    /**
     * 이 예외의 에러 타입을 반환한다.
     *
     * @return 에러 타입
     */
    public ErrorType getErrorType() {
        return errorType;
    }
}
