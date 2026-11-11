package io.github.guseoh.board.common.error;

/**
 * 오류 응답 {@code errors} 배열의 원소. 어떤 입력 값이 왜 거부됐는지를 담는다.
 */
public record FieldErrorDetail(String field, String reason) {
}
