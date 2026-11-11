package io.github.guseoh.archfixture.r6.order.api;

import org.springframework.transaction.annotation.Transactional;

public class OrderApi {

	@Transactional // 위반: 컨트롤러 메서드에 트랜잭션을 걸면 응답 직렬화까지 커넥션을 쥐고 있게 된다
	public void create() {
	}
}
