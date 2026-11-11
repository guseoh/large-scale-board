package io.github.guseoh.archfixture.r2.order.domain;

import io.github.guseoh.archfixture.r2.order.application.OrderService;

public class Order {

	OrderService service; // 위반: domain이 application에 의존
}
