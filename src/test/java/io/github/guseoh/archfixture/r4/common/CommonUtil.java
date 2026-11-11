package io.github.guseoh.archfixture.r4.common;

import io.github.guseoh.archfixture.r4.id.SharedId;
import io.github.guseoh.archfixture.r4.order.domain.Order;

public class CommonUtil {

	Order order;  // 위반: common이 도메인을 알면 모든 도메인이 그 도메인에 간접 의존하게 된다
	SharedId id;  // 허용
}
