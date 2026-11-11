package io.github.guseoh.datagen.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.SplittableRandom;
import org.junit.jupiter.api.Test;

class ZipfSamplerTest {

	@Test
	void 확률의_합은_1이고_순위가_낮을수록_작아진다() {
		ZipfSampler sampler = new ZipfSampler(100, 1.0);

		double sum = 0;
		for (int k = 1; k <= 100; k++) {
			sum += sampler.probability(k);
		}

		assertThat(sum).isCloseTo(1.0, within(1e-9));
		assertThat(sampler.probability(1)).isGreaterThan(sampler.probability(2));
		// s=1이면 1위 확률 = 1 / H(100) ≈ 0.1928, 1위:2위 = 2:1
		assertThat(sampler.probability(1)).isCloseTo(0.1928, within(0.0005));
		assertThat(sampler.probability(1) / sampler.probability(2)).isCloseTo(2.0, within(1e-9));
	}

	@Test
	void 뽑은_빈도가_이론_확률과_맞는다() {
		ZipfSampler sampler = new ZipfSampler(100, 1.0);
		SplittableRandom random = new SplittableRandom(42);
		int samples = 500_000;
		int[] counts = new int[101];

		for (int i = 0; i < samples; i++) {
			counts[sampler.sample(random)]++;
		}

		for (int k : new int[] {1, 2, 10, 100}) {
			double expected = sampler.probability(k);
			double observed = (double) counts[k] / samples;
			// 표본 50만 개면 표준오차가 1위에서 약 0.0006이다. 넉넉히 상대 오차 5%로 본다.
			assertThat(observed).as("순위 %d", k)
					.isCloseTo(expected, within(expected * 0.05 + 0.0005));
		}
		assertThat(counts[0]).isZero(); // 순위는 1부터
	}

	@Test
	void 지수가_0이면_균등_분포다() {
		ZipfSampler sampler = new ZipfSampler(4, 0.0);

		for (int k = 1; k <= 4; k++) {
			assertThat(sampler.probability(k)).isCloseTo(0.25, within(1e-12));
		}
	}

	@Test
	void 지수가_클수록_1위에_더_쏠린다() {
		assertThat(new ZipfSampler(100, 1.5).probability(1))
				.isGreaterThan(new ZipfSampler(100, 1.0).probability(1));
	}
}
