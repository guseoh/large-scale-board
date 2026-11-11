package io.github.guseoh.datagen.core;

import java.util.Arrays;
import java.util.SplittableRandom;

/**
 * Zipf 분포 추출기. 순위 k(1부터 n)가 나올 확률이 1/k^s에 비례한다.
 *
 * <p>쓰는 곳: 게시판별 글 수, 글별 댓글·좋아요·조회 수(2~5단계). 소수의 인기 게시판·인기 글에
 * 활동이 몰리는 모습을 재현해야 인기 글 쏠림, 핫스팟 행 락, 샤드 쏠림(H-12) 같은 문제가 드러난다.
 * 균등 분포로는 이런 병목이 보이지 않는다.
 *
 * <p>구현: 누적 분포 배열을 한 번 만들고, 0~1 난수가 들어가는 칸을 이진 탐색으로 찾는다. 만들 때
 * O(n) 메모리, 한 번 뽑는 데 O(log n). n이 1,000만이면 배열이 80MB라 그때는 다른 방법(거절
 * 샘플링)을 검토한다.
 */
public final class ZipfSampler {

	private final double[] cumulative;
	private final double exponent;

	/**
	 * @param n        순위 개수(1 이상)
	 * @param exponent 지수 s(0 이상). 0이면 균등 분포, 클수록 1위에 더 쏠린다.
	 */
	public ZipfSampler(int n, double exponent) {
		if (n < 1) {
			throw new IllegalArgumentException("n은 1 이상이어야 합니다: " + n);
		}
		if (exponent < 0) {
			throw new IllegalArgumentException("지수는 0 이상이어야 합니다: " + exponent);
		}
		this.exponent = exponent;
		this.cumulative = new double[n];
		double sum = 0;
		for (int k = 1; k <= n; k++) {
			sum += 1.0 / Math.pow(k, exponent);
			cumulative[k - 1] = sum;
		}
		for (int i = 0; i < n; i++) {
			cumulative[i] /= sum;
		}
		cumulative[n - 1] = 1.0; // 부동소수 오차로 마지막 값이 1보다 작아지는 것을 막는다
	}

	/** 순위(1부터 n)를 하나 뽑는다. */
	public int sample(SplittableRandom random) {
		double u = random.nextDouble();
		int index = Arrays.binarySearch(cumulative, u);
		// 정확히 같은 값이 없으면 (-(삽입 위치) - 1)을 돌려준다. 삽입 위치가 u 이상인 첫 칸이다.
		int rank = index >= 0 ? index : -index - 1;
		return rank + 1;
	}

	/** 순위 k가 나올 이론 확률. 테스트와 실험 기록에 쓴다. */
	public double probability(int rank) {
		double previous = rank == 1 ? 0 : cumulative[rank - 2];
		return cumulative[rank - 1] - previous;
	}

	public int size() {
		return cumulative.length;
	}

	public double exponent() {
		return exponent;
	}
}
