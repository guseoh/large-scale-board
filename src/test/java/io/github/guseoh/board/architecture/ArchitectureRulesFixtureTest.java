package io.github.guseoh.board.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

/**
 * 규칙 자체를 검증한다. 규칙을 일부러 어긴 픽스처({@code io.github.guseoh.archfixture.rN})에 규칙을
 * 적용하면 반드시 위반이 나와야 한다.
 *
 * <p>왜 필요한가: 패키지 패턴을 한 글자만 잘못 써도 규칙은 아무 클래스도 고르지 못하고 "통과"한다.
 * 게다가 0단계 규칙은 도메인이 없어도 통과하도록 {@code allowEmptyShould(true)}를 켰기 때문에, 이
 * 테스트가 없으면 그런 실수를 알아챌 방법이 없다.
 *
 * <p>픽스처를 {@code io.github.guseoh.board} 밖에 두는 이유: 앱의 컴포넌트 스캔 범위 안에 두면
 * {@code @SpringBootTest}가 픽스처의 {@code @RestController}까지 빈으로 등록한다.
 */
class ArchitectureRulesFixtureTest {

	private static final String FIXTURE = "io.github.guseoh.archfixture";

	@Test
	void R1은_다른_도메인에_대한_의존을_잡고_공유_패키지_의존은_허용한다() {
		String base = FIXTURE + ".r1";
		EvaluationResult result =
				evaluate(ArchitectureRules.r1DomainsDoNotDependOnEachOther(base), base);

		assertThat(result.hasViolation()).isTrue();
		assertThat(report(result))
				.contains("archfixture.r1.beta.domain.Beta")
				.doesNotContain("archfixture.r1.common.SharedValue")
				.doesNotContain("archfixture.r1.id.SharedId");
	}

	@Test
	void R2는_domain의_application_의존과_application의_infrastructure_의존을_잡는다() {
		String base = FIXTURE + ".r2";
		EvaluationResult result =
				evaluate(ArchitectureRules.r2LayerDependencyDirection(base), base);

		assertThat(result.hasViolation()).isTrue();
		assertThat(report(result))
				.contains("archfixture.r2.order.domain.Order")
				.contains("archfixture.r2.order.infrastructure.OrderJpaStore");
	}

	@Test
	void R3는_domain의_서블릿_의존을_잡는다() {
		String base = FIXTURE + ".r3";
		EvaluationResult result = evaluate(ArchitectureRules.r3DomainIsFreeOfWeb(base), base);

		assertThat(result.hasViolation()).isTrue();
		assertThat(report(result)).contains("jakarta.servlet.http.HttpServletRequest");
	}

	@Test
	void R4는_common의_도메인_의존을_잡고_id_의존은_허용한다() {
		String base = FIXTURE + ".r4";
		EvaluationResult result =
				evaluate(ArchitectureRules.r4CommonDoesNotDependOnDomains(base), base);

		assertThat(result.hasViolation()).isTrue();
		assertThat(report(result))
				.contains("archfixture.r4.order.domain.Order")
				.doesNotContain("archfixture.r4.id.SharedId");
	}

	@Test
	void R5는_패키지_사이의_순환을_잡는다() {
		String base = FIXTURE + ".r5";
		EvaluationResult result = evaluate(ArchitectureRules.r5NoPackageCycles(base), base);

		assertThat(result.hasViolation()).isTrue();
		assertThat(report(result)).contains("Cycle detected");
	}

	@Test
	void R6는_api_밖의_RestController를_잡는다() {
		String base = FIXTURE + ".r6";
		EvaluationResult result =
				evaluate(ArchitectureRules.r6RestControllersResideInApi(base), base);

		assertThat(result.hasViolation()).isTrue();
		assertThat(report(result)).contains("archfixture.r6.order.application.MisplacedController");
	}

	@Test
	void R6는_application_밖의_Transactional을_클래스와_메서드_모두에서_잡는다() {
		String base = FIXTURE + ".r6";
		EvaluationResult result =
				evaluate(ArchitectureRules.r6TransactionalResidesInApplication(base), base);

		assertThat(result.hasViolation()).isTrue();
		assertThat(report(result))
				// 클래스에 붙은 경우
				.contains("archfixture.r6.order.domain.TransactionalEntity")
				// 메서드에 붙은 경우
				.contains("archfixture.r6.order.api.OrderApi.create()")
				// 올바른 위치는 허용
				.doesNotContain("archfixture.r6.order.application.OrderService");
	}

	private static EvaluationResult evaluate(ArchRule rule, String fixturePackage) {
		JavaClasses classes = new ClassFileImporter().importPackages(fixturePackage);
		return rule.evaluate(classes);
	}

	private static String report(EvaluationResult result) {
		return String.join("\n", result.getFailureReport().getDetails());
	}
}
