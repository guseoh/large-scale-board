package io.github.guseoh.archfixture.r7.order.api;

public class OrderResponse {

	private long id; // 위반: ID가 long
	private Long orderId; // 위반: ID가 Long
	private String customerId; // 허용: 문자열 ID
	private long likeCount; // 허용: ID가 아닌 진짜 숫자
}
