package io.github.guseoh.board.common.id;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * {@code board.snowflake.*} 설정. (D0-10)
 *
 * @param nodeId 이 앱 인스턴스의 노드 ID(0~1023). 범위를 벗어나면 기동이 실패한다.
 *     인스턴스가 2대가 되는 4단계부터는 인스턴스마다 다른 값을 환경 변수로 준다.
 *     datagen은 1023을 쓰므로 앱에서는 피한다.
 */
@Validated
@ConfigurationProperties("board.snowflake")
public record SnowflakeProperties(@DefaultValue("0") @Min(0) @Max(1023) int nodeId) {
}
