package io.github.tsseo.interfaces.api;

/**
 * 공통 API 응답 봉투(Envelope) 클래스.
 *
 * <p>모든 HTTP 응답은 이 래퍼로 감싸서 반환한다.
 * 참고 프로젝트(Kotlin)의 {@code ApiResponse<T>} 를 Java record 로 포팅한 것이다.</p>
 *
 * <h2>구조</h2>
 * <pre>
 * {
 *   "meta": {
 *     "result": "SUCCESS" | "FAIL",
 *     "errorCode": null | "NOT_FOUND",
 *     "message": null | "존재하지 않는 요청입니다."
 *   },
 *   "data": { ... } | null
 * }
 * </pre>
 *
 * <h2>사용 예</h2>
 * <pre>{@code
 * // 성공 (데이터 없음)
 * ApiResponse.success()
 *
 * // 성공 (데이터 포함)
 * ApiResponse.success(someData)
 *
 * // 실패
 * ApiResponse.fail("NOT_FOUND", "존재하지 않는 요청입니다.")
 * }</pre>
 *
 * @param <T> 응답 데이터 타입
 */
public record ApiResponse<T>(
        Metadata meta,
        T data
) {

    /**
     * 응답 메타데이터 — 성공/실패 여부, 에러 코드, 메시지를 포함.
     *
     * @param result    요청 처리 결과 ({@link Result#SUCCESS} 또는 {@link Result#FAIL})
     * @param errorCode 실패 시 에러 코드 (성공 시 {@code null})
     * @param message   실패 시 사람이 읽을 수 있는 에러 메시지 (성공 시 {@code null})
     */
    public record Metadata(
            Result result,
            String errorCode,
            String message
    ) {
        /**
         * 요청 처리 결과를 나타내는 열거형.
         */
        public enum Result {
            /** 요청이 정상적으로 처리됨 */
            SUCCESS,
            /** 요청 처리 중 오류 발생 */
            FAIL
        }

        /**
         * 성공 메타데이터 팩토리 — errorCode, message 는 null.
         */
        public static Metadata success() {
            return new Metadata(Result.SUCCESS, null, null);
        }

        /**
         * 실패 메타데이터 팩토리.
         *
         * @param errorCode    에러 코드 (예: "NOT_FOUND")
         * @param errorMessage 사람이 읽을 수 있는 에러 메시지
         */
        public static Metadata fail(String errorCode, String errorMessage) {
            return new Metadata(Result.FAIL, errorCode, errorMessage);
        }
    }

    // ─── 정적 팩토리 메서드 ──────────────────────────────────────────────────

    /**
     * 데이터 없는 성공 응답.
     */
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(Metadata.success(), null);
    }

    /**
     * 데이터 포함 성공 응답.
     *
     * @param data 응답에 포함할 데이터 (null 허용)
     * @param <T>  데이터 타입
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(Metadata.success(), data);
    }

    /**
     * 실패 응답.
     *
     * @param errorCode    에러 코드 (예: "NOT_FOUND")
     * @param errorMessage 사람이 읽을 수 있는 에러 메시지
     */
    public static ApiResponse<Void> fail(String errorCode, String errorMessage) {
        return new ApiResponse<>(Metadata.fail(errorCode, errorMessage), null);
    }
}
