package io.github.guseoh.datagen.h01;

/**
 * H-01에서 비교하는 적재 방식. 이름에 작은따옴표를 쓸 수 없어서 A'는 A2, B'는 B2로 쓴다.
 * (Step-0-Design 12절)
 */
public enum LoadMethod {

	/** JPA {@code saveAll}, 배치 설정 없음. 한 건당 왕복 한 번의 비용. */
	A(false),
	/**
	 * A에서 엔티티가 {@code Persistable}을 구현하지 않음. ID를 직접 넣으면 INSERT 전에 SELECT가
	 * 나가는가.
	 */
	A2(false),
	/**
	 * JPA {@code saveAll} + {@code hibernate.jdbc.batch_size} + {@code
	 * rewriteBatchedStatements=true}. JPA의 최선.
	 */
	B(true),
	/** B에서 ID 전략만 IDENTITY. IDENTITY에서는 배치 INSERT가 꺼지는가. */
	B2(true),
	/**
	 * {@code JdbcTemplate.batchUpdate} + {@code rewriteBatchedStatements=true}. 영속성 컨텍스트
	 * 비용을 뺀 값.
	 */
	C(true),
	/** CSV + {@code LOAD DATA INFILE}. 적재 전용 경로의 속도. */
	D(false);

	private final boolean batchProfile;

	LoadMethod(boolean batchProfile) {
		this.batchProfile = batchProfile;
	}

	/** {@code batch} 프로필(배치 크기, rewriteBatchedStatements)로 실행해야 하는가. */
	public boolean requiresBatchProfile() {
		return batchProfile;
	}
}
