package io.github.guseoh.board.common.id;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.guseoh.board.id.Snowflake;
import io.github.guseoh.board.id.SnowflakeIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * 설정 클래스 하나만 올린 작은 컨텍스트로 빈 등록과 노드 ID 검증을 확인한다. DB도 웹 서버도 띄우지
 * 않는다.
 */
class SnowflakeConfigTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(SnowflakeConfig.class);

	@Test
	void 설정이_없으면_노드_0으로_생성기를_등록한다() {
		contextRunner.run(context -> {
			SnowflakeIdGenerator generator = context.getBean(SnowflakeIdGenerator.class);

			assertThat(generator.nodeId()).isZero();
			assertThat(Snowflake.nodeIdOf(generator.nextId())).isZero();
		});
	}

	@Test
	void 설정한_노드_ID가_ID에_들어간다() {
		contextRunner.withPropertyValues("board.snowflake.node-id=5").run(context ->
				assertThat(Snowflake.nodeIdOf(context.getBean(SnowflakeIdGenerator.class).nextId()))
						.isEqualTo(5));
	}

	@Test
	void 노드_ID가_범위를_벗어나면_기동이_실패한다() {
		contextRunner.withPropertyValues("board.snowflake.node-id=1024").run(context -> {
			assertThat(context).hasFailed();
			assertThat(context.getStartupFailure()).rootCause()
					.hasMessageContaining("board.snowflake");
		});
	}
}
