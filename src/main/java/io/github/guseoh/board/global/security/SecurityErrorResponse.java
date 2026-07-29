package io.github.guseoh.board.global.security;

import java.time.Instant;

/**
 * 보안 오류 응답
 */
public record SecurityErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path
) {

    public static SecurityErrorResponse of(
            int status,
            String code,
            String message,
            String path
    ) {
        return new SecurityErrorResponse(
                Instant.now(),
                status,
                code,
                message,
                path
        );
    }
}

/*
    응답 예시:
    {
      "timestamp": "2026-07-29T02:30:00Z",
      "status": 401,
      "code": "AUTHENTICATION_REQUIRED",
      "message": "인증이 필요합니다.",
      "path": "/api/private"
    }
 */