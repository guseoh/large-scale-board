package io.github.guseoh.board.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * 실제 앱 코드에 R1~R6을 적용한다. 테스트 코드는 검사하지 않는다.
 */
class ArchitectureTest {

	private static final String BASE = "io.github.guseoh.board";

	private static JavaClasses classes;

	@BeforeAll
	static void importClasses() {
		// 클래스 파일을 읽는 비용이 커서 한 번만 읽고 모든 규칙이 공유한다.
		classes = new ClassFileImporter()
				.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
				.importPackages(BASE);
	}

	@Test
	void R1_도메인_패키지는_서로_의존하지_않는다() {
		ArchitectureRules.r1DomainsDoNotDependOnEachOther(BASE).check(classes);
	}

	@Test
	void R2_계층_의존_방향을_지킨다() {
		ArchitectureRules.r2LayerDependencyDirection(BASE).check(classes);
	}

	@Test
	void R3_domain_계층은_웹_기술에_의존하지_않는다() {
		ArchitectureRules.r3DomainIsFreeOfWeb(BASE).check(classes);
	}

	@Test
	void R4_common은_도메인에_의존하지_않는다() {
		ArchitectureRules.r4CommonDoesNotDependOnDomains(BASE).check(classes);
	}

	@Test
	void R5_패키지_사이에_순환_의존이_없다() {
		ArchitectureRules.r5NoPackageCycles(BASE).check(classes);
	}

	@Test
	void R6_RestController는_api에만_있다() {
		ArchitectureRules.r6RestControllersResideInApi(BASE).check(classes);
	}

	@Test
	void R6_Transactional은_application에만_있다() {
		ArchitectureRules.r6TransactionalResidesInApplication(BASE).check(classes);
	}
}
