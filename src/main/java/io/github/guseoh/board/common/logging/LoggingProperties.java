package io.github.guseoh.board.common.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * {@code board.logging.*} 설정.
 *
 * @param accessEnabled 접근 로그를 남길지 여부. 부하 실험에서는 로그 쓰기 자체가 처리량에 영향을 주므로 끌 수 있게 하고,
 *                      실험 기록에 켰는지 여부를 적는다.
 */
@ConfigurationProperties("board.logging")
public record LoggingProperties(@DefaultValue("true") boolean accessEnabled) {
}
