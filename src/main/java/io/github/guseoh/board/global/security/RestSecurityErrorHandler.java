package io.github.guseoh.board.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 인증·인가 실패 처리기
 */
@Component
public class RestSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final JsonMapper jsonMapper;

    public RestSecurityErrorHandler(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        writeErrorResponse(
                request,
                response,
                HttpStatus.UNAUTHORIZED,
                "AUTHENTICATION_REQUIRED",
                "인증이 필요합니다."
        );
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        writeErrorResponse(
                request,
                response,
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "접근 권한이 없습니다."
        );

    }

    private void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String code,
            String message
    ) throws IOException{
        SecurityErrorResponse body = SecurityErrorResponse.of(
                status.value(),
                code,
                message,
                request.getRequestURI()

        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        jsonMapper.writeValue(response.getOutputStream(), body);
    }
}

/*
    AuthenticationEntryPoint
    - 인증되지 않은 사용자가 인증이 필요한 경로에 접근했을 때 호출된다.
    - 로그인 하지 않음 -> 보호 API 요청 -> 401 Unauthorized

    AccessDeniedHandler
    - 인증은 됐지만 필요한 권한이 없을 때 호출된다.
    - USER로 로그인 -> ADMIN API 요청 -> 403 Forbidden
 */