package io.github.guseoh.archfixture.r3.order.domain;

import jakarta.servlet.http.HttpServletRequest;

public class Order {

	// 위반: 도메인 규칙이 HTTP 요청 객체를 알면 웹 밖(배치, 이벤트 소비자)에서 쓸 수 없다
	public void apply(HttpServletRequest request) {
	}
}
