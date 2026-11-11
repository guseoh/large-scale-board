package io.github.guseoh.board.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;

/**
 * 아키텍처 규칙 R1~R6. (Step-0-Design 5절)
 *
 * <p>모든 규칙이 기준 패키지를 인자로 받는다. 실제 코드({@code io.github.guseoh.board})와 규칙을
 * 일부러 어긴 픽스처 ({@code io.github.guseoh.archfixture.*})에 같은 규칙을 적용해, 규칙이 실제로
 * 위반을 잡는지 검증하기 위해서다.
 *
 * <p>0단계에는 도메인이 {@code system} 하나뿐이고 {@code domain}·{@code application} 계층이 아직
 * 없다. ArchUnit은 검사 대상이 0개이면 기본적으로 실패하므로({@code failOnEmptyShould}) 비어 있어도
 * 통과하게 연다. 대신 "아무것도 검사하지 않고 통과"하는 위험은 픽스처 테스트가 막는다.
 */
public final class ArchitectureRules {

	private static final String API = "Api";
	private static final String APPLICATION = "Application";
	private static final String DOMAIN = "Domain";
	private static final String INFRASTRUCTURE = "Infrastructure";

	private ArchitectureRules() {
	}

	/** 도메인이 아닌 공유 패키지: {@code common}(앱 공통)과 {@code id}(board-id 라이브러리). */
	private static String[] sharedPackages(String base) {
		return new String[] {base + ".common..", base + ".id.."};
	}

	/**
	 * R1. 도메인 패키지는 서로 의존하지 않는다. {@code common}과 {@code board-id}에 대한 의존만
	 * 허용한다.
	 */
	public static ArchRule r1DomainsDoNotDependOnEachOther(String base) {
		return slices().matching(base + ".(*)..")
				.should().notDependOnEachOther()
				.ignoreDependency(
						DescribedPredicate.alwaysTrue(), resideInAnyPackage(sharedPackages(base)))
				.as("R1 도메인 패키지는 서로 의존하지 않는다 (common, id만 허용)")
				.allowEmptyShould(true);
	}

	/**
	 * R2. 도메인 안의 의존 방향은 api → application → domain, infrastructure → domain이다.
	 *
	 * <ul>
	 *   <li>domain은 나머지 세 계층에 의존하지 않는다.</li>
	 *   <li>application은 infrastructure에 의존하지 않는다. 리포지토리 인터페이스는 domain에 두고
	 *       구현만 infrastructure에 둔다.</li>
	 *   <li>api가 domain 타입을 쓰는 것(응답 DTO 변환)은 아래 방향이므로 허용한다.</li>
	 * </ul>
	 */
	public static ArchRule r2LayerDependencyDirection(String base) {
		return layeredArchitecture()
				.consideringOnlyDependenciesInLayers()
				.withOptionalLayers(true)
				.layer(API).definedBy(base + ".*.api..")
				.layer(APPLICATION).definedBy(base + ".*.application..")
				.layer(DOMAIN).definedBy(base + ".*.domain..")
				.layer(INFRASTRUCTURE).definedBy(base + ".*.infrastructure..")
				.whereLayer(API).mayNotBeAccessedByAnyLayer()
				.whereLayer(APPLICATION).mayOnlyBeAccessedByLayers(API)
				.whereLayer(INFRASTRUCTURE).mayNotBeAccessedByAnyLayer()
				.whereLayer(DOMAIN).mayOnlyBeAccessedByLayers(API, APPLICATION, INFRASTRUCTURE)
				.as("R2 계층 의존 방향은 api → application → domain, infrastructure → domain");
	}

	/**
	 * R3. domain 계층은 웹 기술에 의존하지 않는다. JPA 애너테이션은 허용한다(엔티티를 도메인 모델로
	 * 쓰므로).
	 */
	public static ArchRule r3DomainIsFreeOfWeb(String base) {
		return noClasses().that().resideInAPackage(base + ".*.domain..")
				.should().dependOnClassesThat()
				.resideInAnyPackage("org.springframework.web..", "jakarta.servlet..")
				.as("R3 domain 계층은 웹 기술(org.springframework.web, jakarta.servlet)에 "
				+ "의존하지 않는다")
				.allowEmptyShould(true);
	}

	/** R4. common은 어떤 도메인에도 의존하지 않는다. */
	public static ArchRule r4CommonDoesNotDependOnDomains(String base) {
		DescribedPredicate<JavaClass> domainClasses = resideInAPackage(base + ".*..")
				.and(not(resideInAnyPackage(sharedPackages(base))));
		return noClasses().that().resideInAPackage(base + ".common..")
				.should().dependOnClassesThat(domainClasses.as("도메인 패키지의 클래스"))
				.as("R4 common은 어떤 도메인에도 의존하지 않는다")
				.allowEmptyShould(true);
	}

	/** R5. 패키지 사이에 순환 의존이 없다. 패키지 하나하나를 조각으로 보고 검사한다. */
	public static ArchRule r5NoPackageCycles(String base) {
		return slices().matching(base + ".(**)")
				.should().beFreeOfCycles()
				.as("R5 패키지 사이에 순환 의존이 없다")
				.allowEmptyShould(true);
	}

	/** R6-a. {@code @RestController}는 api 패키지에만 둔다. */
	public static ArchRule r6RestControllersResideInApi(String base) {
		return classes().that()
				.areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
				.should().resideInAPackage(base + ".*.api..")
				.as("R6 @RestController는 api 패키지에만 둔다")
				.allowEmptyShould(true);
	}

	/**
	 * R6-b. {@code @Transactional}은 application 패키지에만 둔다. 클래스와 메서드 양쪽을 본다.
	 * Spring과 Jakarta 두 애너테이션을 모두 막는다(자동 완성으로 Jakarta 쪽을 잘못 가져오는 경우가
	 * 흔하다).
	 */
	public static ArchRule r6TransactionalResidesInApplication(String base) {
		DescribedPredicate<JavaClass> outsideApplication = resideInAPackage(base + "..")
				.and(resideOutsideOfPackage(base + ".*.application.."));
		String springTx = "org.springframework.transaction.annotation.Transactional";
		String jakartaTx = "jakarta.transaction.Transactional";

		ArchRule onClasses = noClasses().that(outsideApplication.as("application 밖의 클래스"))
				.should().beAnnotatedWith(springTx)
				.orShould().beAnnotatedWith(jakartaTx)
				.allowEmptyShould(true);
		ArchRule onMethods = noMethods().that()
				.areDeclaredInClassesThat(outsideApplication.as("application 밖의 클래스"))
				.should().beAnnotatedWith(springTx)
				.orShould().beAnnotatedWith(jakartaTx)
				.allowEmptyShould(true);
		return CompositeArchRule.of(onClasses).and(onMethods)
				.as("R6 @Transactional은 application 패키지에만 둔다");
	}
}
