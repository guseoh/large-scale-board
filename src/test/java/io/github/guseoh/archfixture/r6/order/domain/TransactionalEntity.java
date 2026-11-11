package io.github.guseoh.archfixture.r6.order.domain;

import org.springframework.transaction.annotation.Transactional;

@Transactional // 위반: 클래스에 붙은 경우
public class TransactionalEntity {
}
