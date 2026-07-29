package io.github.guseoh.board.global.system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HTTP 요청
 * → Spring Security Filter Chain
 * → DispatcherServlet
 * → SystemController
 * → JSON 응답
 *
 * 확인하는 Controller
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

    @GetMapping("/ping")
    public SystemPingResResponse pint() {
        return new SystemPingResResponse("UP");
    }

    public record SystemPingResResponse(
            String status
    ) {
    }
}
