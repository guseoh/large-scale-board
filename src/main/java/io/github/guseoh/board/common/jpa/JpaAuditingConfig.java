package io.github.guseoh.board.common.jpa;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


@Configuration(proxyBeanMethods = false)
@EnableJpaAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {

    /*
        시각을 Clock에서 가져오므로 테스트가 MutableClock으로 시간을 조작할 수 있다.
     */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
    /*
        마이크로초로 자르는 이유는 DB가 나노초를 반올림하기 때문입니다.
        자르지 않으면 저장한 값과 다시 읽은 값이 달라집니다.
     */
    @Bean
    DateTimeProvider auditingDateTimeProvider(Clock clock) {

        //todo DATETIME(6)이 마이크로초라서 저장 전에 잘라야 저장한 값과 다시 읽은 값이 같다.
        return () -> Optional.of(clock.instant().truncatedTo(ChronoUnit.MICROS));
    }
}

/*
    Optional.of()
    - java의 Optional 객체를 생성하는 정적 메서드.
    - 값이 반드시 존재하고 null이 아니라는 것을 알고 있을 때 사용.
 */