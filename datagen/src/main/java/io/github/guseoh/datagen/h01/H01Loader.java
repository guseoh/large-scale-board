package io.github.guseoh.datagen.h01;

import io.github.guseoh.datagen.DatagenProperties;
import io.github.guseoh.datagen.csv.CsvExporter;
import io.github.guseoh.datagen.table.ExpLoadArticleGenerator;
import io.github.guseoh.datagen.table.ExpLoadArticleRow;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * H-01: 같은 행을 방식만 바꿔 적재하고 시간을 잰다. (Step-0-Design 12절)
 *
 * <p>공정하게 비교하려고 고정한 것
 * <ul>
 *   <li>같은 시드와 행 수 → 모든 방식이 같은 행을 넣는다(B'만 ID를 DB가 정한다).</li>
 *   <li>묶음 크기 1,000. 묶음마다 트랜잭션 하나를 커밋한다. 커밋하면 영속성 컨텍스트가 비워지므로
 *       "묶음마다 flush와 clear"와 같은 효과다. 100만 건을 한 트랜잭션에 넣으면 방식 A·B는 영속성
 *       컨텍스트에 엔티티 100만 개가 쌓여 메모리와 변경 감지 비용이 결과를 덮는다.</li>
 *   <li>시작 전에 테이블을 비운다(TRUNCATE). 이미 있는 행 때문에 B-트리 모양이 달라지지 않게
 *       한다.</li>
 * </ul>
 *
 * <p>측정 범위: A~C는 행 생성 시간이 적재 시간 안에 섞여 있다. D는 CSV 생성 시간을 따로 보고하며,
 * 이것이 행 생성 비용의 근삿값이다.
 */
@Component
public class H01Loader {

	private static final String INSERT_SQL = "INSERT INTO exp_load_article "
			+ "(id, board_id, writer_id, title, content, created_at) VALUES (?, ?, ?, ?, ?, ?)";

	private final DatagenProperties properties;
	private final Environment environment;
	private final JdbcTemplate jdbcTemplate;
	private final TransactionTemplate transactionTemplate;
	private final ExpLoadArticleRepository persistableRepository;
	private final ExpLoadArticleAssignedIdRepository assignedIdRepository;
	private final ExpLoadArticleIdentityRepository identityRepository;

	public H01Loader(DatagenProperties properties, Environment environment,
			JdbcTemplate jdbcTemplate,
			TransactionTemplate transactionTemplate, ExpLoadArticleRepository persistableRepository,
			ExpLoadArticleAssignedIdRepository assignedIdRepository,
			ExpLoadArticleIdentityRepository identityRepository) {
		this.properties = properties;
		this.environment = environment;
		this.jdbcTemplate = jdbcTemplate;
		this.transactionTemplate = transactionTemplate;
		this.persistableRepository = persistableRepository;
		this.assignedIdRepository = assignedIdRepository;
		this.identityRepository = identityRepository;
	}

	/**
	 * @param seconds    적재에 걸린 시간(중단했다면 중단까지)
	 * @param csvSeconds D에서 CSV를 만드는 데 걸린 시간. 다른 방식은 0
	 * @param stopped    중단 조건(maxSeconds)에 걸려 멈췄는가
	 */
	public record Result(LoadMethod method, long rows, long loaded, long countInDb, double seconds,
			double csvSeconds, boolean stopped, long startEpochMs, long endEpochMs) {

		public double rowsPerSecond() {
			return loaded / seconds;
		}

		/** 중단했다면 적재한 속도로 전체 행 수를 넣는 데 걸릴 시간을 환산한다. */
		public double projectedSeconds() {
			return rows / rowsPerSecond();
		}
	}

	public Result run() {
		LoadMethod method = properties.method();
		if (method == null) {
			throw new IllegalArgumentException("--datagen.method=A|A2|B|B2|C|D 가 필요합니다");
		}
		verifyProfile(method);
		long rows = properties.scaledRows();

		jdbcTemplate.execute("TRUNCATE TABLE exp_load_article");
		jdbcTemplate.execute("TRUNCATE TABLE exp_load_article_identity");

		ExpLoadArticleGenerator generator = new ExpLoadArticleGenerator(
				properties.seed(), rows, properties.baseTime(), properties.spanDays());

		Result result = method == LoadMethod.D
				? loadWithLoadData(generator, rows)
				: loadInChunks(method, generator, rows);
		return result;
	}

	private Result loadInChunks(
			LoadMethod method, Iterator<ExpLoadArticleRow> generator, long rows) {
		int chunkSize = properties.chunkSize();
		List<ExpLoadArticleRow> chunk = new ArrayList<>(chunkSize);
		long loaded = 0;
		boolean stopped = false;
		long startMs = System.currentTimeMillis();
		long startNanos = System.nanoTime();

		while (generator.hasNext()) {
			chunk.clear();
			while (generator.hasNext() && chunk.size() < chunkSize) {
				chunk.add(generator.next());
			}
			insertChunk(method, chunk);
			loaded += chunk.size();
			if ((System.nanoTime() - startNanos) / 1e9 > properties.maxSeconds()) {
				stopped = true; // 중단 조건: 이후는 적재한 속도로 환산한다
				break;
			}
		}

		double seconds = (System.nanoTime() - startNanos) / 1e9;
		long endMs = System.currentTimeMillis();
		return new Result(method, rows, loaded, count(method), seconds, 0, stopped, startMs, endMs);
	}

	private void insertChunk(LoadMethod method, List<ExpLoadArticleRow> chunk) {
		transactionTemplate.executeWithoutResult(status -> {
			switch (method) {
				case A, B -> persistableRepository.saveAll(
						chunk.stream().map(ExpLoadArticleEntity::from).toList());
				case A2 -> assignedIdRepository.saveAll(
						chunk.stream().map(ExpLoadArticleAssignedIdEntity::from).toList());
				case B2 -> identityRepository.saveAll(
						chunk.stream().map(ExpLoadArticleIdentityEntity::from).toList());
				case C -> jdbcTemplate.batchUpdate(INSERT_SQL, chunk, chunk.size(), (ps, row) -> {
					ps.setLong(1, row.id());
					ps.setLong(2, row.boardId());
					ps.setLong(3, row.writerId());
					ps.setString(4, row.title());
					ps.setString(5, row.content());
					ps.setObject(6, row.createdAt());
				});
				case D -> throw new IllegalStateException("D는 묶음 적재를 쓰지 않는다");
			}
		});
	}

	/**
	 * CSV를 만든 뒤 {@code LOAD DATA INFILE}로 한 번에 넣는다.
	 *
	 * <p>{@code LOCAL}을 쓰지 않는 이유: LOCAL은 클라이언트(앱)가 파일을 읽어 네트워크로 보낸다.
	 * 여기서는 MySQL 서버가 자기 디스크(마운트한 폴더)에서 직접 읽는다. 대신 접속 계정에 {@code
	 * FILE} 권한이 있어야 하고, 파일은 {@code secure_file_priv} 폴더 안에 있어야 한다.
	 */
	private Result loadWithLoadData(Iterator<ExpLoadArticleRow> generator, long rows) {
		Path csv = properties.csvDir().resolve(ExpLoadArticleGenerator.TABLE + ".csv");
		long csvStart = System.nanoTime();
		CsvExporter.Result written = CsvExporter.write(generator, csv);
		double csvSeconds = (System.nanoTime() - csvStart) / 1e9;
		System.out.printf("CSV_RESULT file=%s rows=%d bytes=%d sha256=%s seconds=%.3f%n",
				csv.toAbsolutePath(), written.rows(), written.bytes(), written.sha256(),
				csvSeconds);

		String inContainer = properties.mysqlFileDir() + "/" + csv.getFileName();
		long startMs = System.currentTimeMillis();
		long startNanos = System.nanoTime();
		jdbcTemplate.execute("LOAD DATA INFILE '" + inContainer + "' INTO TABLE exp_load_article "
				+ "CHARACTER SET utf8mb4 FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' "
				+ "(id, board_id, writer_id, title, content, created_at)");
		double seconds = (System.nanoTime() - startNanos) / 1e9;
		long endMs = System.currentTimeMillis();
		return new Result(LoadMethod.D, rows, written.rows(), count(LoadMethod.D), seconds,
				csvSeconds, false,
				startMs, endMs);
	}

	private long count(LoadMethod method) {
		String table = method == LoadMethod.B2 ? "exp_load_article_identity" : "exp_load_article";
		Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
		return count == null ? 0 : count;
	}

	/**
	 * 방식과 설정이 맞는지 확인한다. 프로필을 빠뜨리고 B를 돌리면 사실상 A를 잰 셈이 되는데, 결과만
	 * 봐서는 알 수 없다.
	 */
	private void verifyProfile(LoadMethod method) {
		int batchSize = environment.getProperty(
				"spring.jpa.properties.hibernate.jdbc.batch_size", Integer.class, 0);
		boolean rewrite = environment.getProperty("spring.datasource.url", "")
				.contains("rewriteBatchedStatements=true");
		if (method.requiresBatchProfile()) {
			if (!rewrite || (method != LoadMethod.C && batchSize <= 0)) {
				throw new IllegalStateException(method + "는 batch 프로필로 실행해야 합니다 "
						+ "(--spring.profiles.active=batch). 현재 batch_size=" + batchSize
						+ ", rewrite=" + rewrite);
			}
		} else if (rewrite || batchSize > 0) {
			throw new IllegalStateException(method
					+ "는 batch 프로필 없이 실행해야 합니다. 현재 batch_size="
					+ batchSize + ", rewrite=" + rewrite);
		}
	}
}
