package io.github.guseoh.archfixture.r5.order.domain.line;

import io.github.guseoh.archfixture.r5.order.domain.Order;

public class OrderLine {

	Order order; // 위반: domain ↔ domain.line 순환
}
