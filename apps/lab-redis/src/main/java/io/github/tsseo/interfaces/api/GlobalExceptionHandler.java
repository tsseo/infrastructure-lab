package io.github.tsseo.interfaces.api;

import io.github.tsseo.support.error.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외 처리기.
 *
 * <p>모든 {@link RestController} 에서 발생한 {@link CoreException} 을 가로채어
 * {@link ApiResponse#fail(String, String)} 형태로 변환한다.
 * HTTP 상태 코드는 {@link CoreException#getErrorType()} 에서 가져온다.</p>
 *
 * <h2>처리 흐름</h2>
 * <pre>
 *   Controller 에서 CoreException 발생
 *     → @ExceptionHandler 가 가로챔
 *     → ErrorType 의 HttpStatus + code + message 로 ApiResponse.fail 생성
 *     → 클라이언트에 JSON 형태로 반환
 * </pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 비즈니스 예외({@link CoreException}) 처리.
     *
     * @param e 발생한 CoreException
     * @return 에러 정보를 담은 ApiResponse 와 적절한 HTTP 상태 코드
     */
    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ApiResponse<Void>> handleCoreException(CoreException e) {
        log.warn("CoreException 발생: errorType={}, message={}",
                e.getErrorType().name(), e.getMessage());

        ApiResponse<Void> body = ApiResponse.fail(
                e.getErrorType().getCode(),
                e.getMessage()
        );

        return ResponseEntity
                .status(e.getErrorType().getStatus())
                .body(body);
    }

    /**
     * 예상치 못한 예외 처리 — 내부 오류 발생 시 500 반환.
     *
     * @param e 처리되지 않은 예외
     * @return 500 에러 ApiResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("처리되지 않은 예외 발생", e);

        ApiResponse<Void> body = ApiResponse.fail(
                "Internal Server Error",
                "일시적인 오류가 발생했습니다."
        );

        return ResponseEntity.internalServerError().body(body);
    }
}
