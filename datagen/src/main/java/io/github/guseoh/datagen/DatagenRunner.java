package io.github.guseoh.datagen;

import io.github.guseoh.datagen.core.Seeds;
import io.github.guseoh.datagen.csv.CsvExporter;
import io.github.guseoh.datagen.h01.H01Loader;
import io.github.guseoh.datagen.table.ExpLoadArticleGenerator;
import java.nio.file.Path;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 명령줄 진입점. 결과는 한 줄로 출력해 스크립트(scripts/h01.ps1, snapshot.ps1)가 읽는다.
 *
 * <pre>
 * --datagen.job=csv                      exp_load_article CSV만 만든다(체크섬 출력)
 * --datagen.job=h01 --datagen.method=B   적재 방식 하나를 실행한다
 *                                        (B, B2, C는 --spring.profiles.active=batch)
 * </pre>
 */
@Component
public class DatagenRunner implements ApplicationRunner {

	private final DatagenProperties properties;
	private final H01Loader h01Loader;

	public DatagenRunner(DatagenProperties properties, H01Loader h01Loader) {
		this.properties = properties;
		this.h01Loader = h01Loader;
	}

	@Override
	public void run(ApplicationArguments args) {
		switch (properties.job()) {
			case "csv" -> writeCsv();
			case "h01" -> runH01();
			default -> throw new IllegalArgumentException(
					"알 수 없는 job: " + properties.job() + " (csv | h01)");
		}
	}

	private void writeCsv() {
		long rows = properties.scaledRows();
		Path csv = properties.csvDir().resolve(ExpLoadArticleGenerator.TABLE + ".csv");
		long start = System.nanoTime();
		CsvExporter.Result result = CsvExporter.write(new ExpLoadArticleGenerator(
				properties.seed(), rows, properties.baseTime(), properties.spanDays()), csv);
		System.out.printf("CSV_RESULT file=%s rows=%d bytes=%d sha256=%s seconds=%.3f "
						+ "seed=%d subSeed=%d%n",
				csv.toAbsolutePath(), result.rows(), result.bytes(), result.sha256(),
				(System.nanoTime() - start) / 1e9, properties.seed(),
				Seeds.subSeed(properties.seed(), ExpLoadArticleGenerator.TABLE));
	}

	private void runH01() {
		H01Loader.Result r = h01Loader.run();
		System.out.printf("H01_RESULT method=%s rows=%d loaded=%d countInDb=%d seconds=%.3f "
						+ "rowsPerSec=%.1f projectedSeconds=%.1f csvSeconds=%.3f stopped=%s "
						+ "startEpochMs=%d endEpochMs=%d%n",
				r.method(), r.rows(), r.loaded(), r.countInDb(), r.seconds(), r.rowsPerSecond(),
				r.projectedSeconds(), r.csvSeconds(), r.stopped(),
				r.startEpochMs(), r.endEpochMs());
	}
}
