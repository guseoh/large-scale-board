package io.github.guseoh.datagen.core;

import java.util.SplittableRandom;

/**
 * 고정 단어 목록에서 단어를 뽑아 제목과 본문을 만든다.
 *
 * <p>내용은 의미가 없어도 되지만 크기는 실제와 비슷해야 한다. 행 크기가 페이지당 행 수, 버퍼 풀
 * 적중률, 적재 속도를 정하기 때문이다. 한글 단어를 쓰는 이유도 같다: utf8mb4에서 한글은 글자당
 * 3바이트다.
 *
 * <p>쉼표, 따옴표, 줄바꿈이 들어가지 않는다. CSV에 이스케이프를 넣지 않아도 되도록 일부러 그렇게
 * 만든다.
 */
public final class TextGenerator {

	private static final String[] WORDS = {
			"게시판", "질문", "답변", "공유", "후기", "정리", "오늘", "내일", "서버", "데이터",
			"성능", "인덱스", "쿼리", "트랜잭션", "커넥션", "캐시", "장애", "복구", "측정", "실험",
			"설계", "구현", "테스트", "배포", "로그", "지표", "알림", "요청", "응답", "지연",
			"처리량", "병목", "개선", "비교", "결과", "해석", "비용", "대안", "조건", "기준",
			"사용자", "게시글", "댓글", "좋아요", "조회수", "인기", "검색", "목록", "상세", "작성",
			"수정", "삭제", "권한", "인증", "토큰", "세션", "비밀번호", "회원", "가입", "탈퇴",
			"스프링", "자바", "MySQL", "Redis", "Kafka",
			"도커", "리눅스", "네트워크", "운영체제", "메모리",
	};

	private TextGenerator() {
	}

	/** 단어 {@code minWords}~{@code maxWords}개를 공백으로 이어 붙인다. */
	public static String words(SplittableRandom random, int minWords, int maxWords) {
		int count = random.nextInt(minWords, maxWords + 1);
		StringBuilder builder = new StringBuilder(count * 4);
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				builder.append(' ');
			}
			builder.append(WORDS[random.nextInt(WORDS.length)]);
		}
		return builder.toString();
	}
}
