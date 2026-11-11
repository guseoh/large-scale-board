package io.github.guseoh.datagen.core;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.SplittableRandom;

/**
 * 테이블마다 독립된 난수열을 만든다. (Step-0-Design 11절 결정성)
 *
 * <p>전체 시드 하나에서 "시드 + 테이블 이름"으로 하위 시드를 만든다. 그래서 테이블을 새로
 * 추가하거나 다른 테이블의 생성 규칙을 바꿔도 기존 테이블의 데이터가 바뀌지 않는다. 난수열 하나를
 * 모든 테이블이 나눠 쓰면, 앞 테이블의 행 수가 바뀌는 순간 뒤 테이블의 데이터가 전부 달라진다.
 *
 * <p>하위 시드는 SHA-256으로 만든다. {@code String.hashCode()}는 32비트라 충돌이 쉽고, 해시 함수
 * 구현에 기대지 않아야 JVM이나 버전이 바뀌어도 같은 값이 나온다.
 */
public final class Seeds {

	private Seeds() {
	}

	public static SplittableRandom forTable(long seed, String table) {
		return new SplittableRandom(subSeed(seed, table));
	}

	/** 하위 시드 값. 데이터셋 매니페스트에 기록하고 테스트에서 확인한다. */
	public static long subSeed(long seed, String table) {
		try {
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			byte[] digest = sha256.digest((seed + ":" + table).getBytes(StandardCharsets.UTF_8));
			return ByteBuffer.wrap(digest).getLong();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256은 모든 JVM이 지원해야 한다", e);
		}
	}
}
