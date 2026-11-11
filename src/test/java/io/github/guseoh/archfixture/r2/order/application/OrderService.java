package io.github.guseoh.archfixture.r2.order.application;

import io.github.guseoh.archfixture.r2.order.infrastructure.OrderJpaStore;

public class OrderService {

	OrderJpaStore store; // 위반: application이 infrastructure 구현체에 직접 의존
}
