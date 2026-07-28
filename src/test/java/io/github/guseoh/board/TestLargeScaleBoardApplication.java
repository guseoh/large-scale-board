package io.github.guseoh.board;

import org.springframework.boot.SpringApplication;

public class TestLargeScaleBoardApplication {

	public static void main(String[] args) {
		SpringApplication.from(LargeScaleBoardApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
