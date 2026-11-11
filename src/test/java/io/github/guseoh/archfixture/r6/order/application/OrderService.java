package io.github.guseoh.archfixture.r6.order.application;

import org.springframework.transaction.annotation.Transactional;

public class OrderService {

	@Transactional // 허용: 트랜잭션 경계는 application 계층
	public void create() {
	}
}
