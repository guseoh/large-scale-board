package io.github.guseoh.archfixture.r1.alpha.application;

import io.github.guseoh.archfixture.r1.beta.domain.Beta;
import io.github.guseoh.archfixture.r1.common.SharedValue;
import io.github.guseoh.archfixture.r1.id.SharedId;

public class AlphaService {

	Beta beta;            // 위반: 다른 도메인의 엔티티를 직접 참조
	SharedValue shared;   // 허용: common
	SharedId id;          // 허용: board-id
}
