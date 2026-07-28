package io.github.guseoh.board;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class LargeScaleBoardApplicationTests {

	@Test
	void contextLoads() {
	}

}
