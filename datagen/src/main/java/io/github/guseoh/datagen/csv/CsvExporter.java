package io.github.guseoh.datagen.csv;

import io.github.guseoh.datagen.table.ExpLoadArticleRow;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.Iterator;

/**
 * 행을 CSV로 쓰고, 쓴 바이트의 SHA-256을 함께 계산한다. (D0-16)
 *
 * <p>CSV를 중간 산출물로 남기는 이유: 체크섬으로 결정성을 검증할 수 있고, DB를 지워도 생성 없이
 * 다시 적재할 수 있다.
 *
 * <p>형식은 {@code LOAD DATA INFILE}의 기본값에 가깝게 고정한다: UTF-8, 쉼표 구분, 줄바꿈 LF,
 * 머리글 없음, 따옴표 없음. 값에 쉼표·따옴표·줄바꿈이 없다는 것은 생성기({@code TextGenerator})가
 * 보장한다.
 */
public final class CsvExporter {

	private static final DateTimeFormatter DATETIME =
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	private CsvExporter() {
	}

	public record Result(long rows, String sha256, long bytes) {
	}

	public static Result write(Iterator<ExpLoadArticleRow> rows, Path file) {
		try {
			Files.createDirectories(file.toAbsolutePath().getParent());
			try (OutputStream out = Files.newOutputStream(file)) {
				return write(rows, out);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static Result write(Iterator<ExpLoadArticleRow> rows, OutputStream out) {
		MessageDigest sha256 = sha256();
		CountingOutputStream counting =
				new CountingOutputStream(new DigestOutputStream(out, sha256));
		long count = 0;
		try (BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(counting, StandardCharsets.UTF_8), 1 << 16)) {
			while (rows.hasNext()) {
				ExpLoadArticleRow row = rows.next();
				writer.write(Long.toString(row.id()));
				writer.write(',');
				writer.write(Long.toString(row.boardId()));
				writer.write(',');
				writer.write(Long.toString(row.writerId()));
				writer.write(',');
				writer.write(row.title());
				writer.write(',');
				writer.write(row.content());
				writer.write(',');
				writer.write(DATETIME.format(row.createdAt()));
				writer.write('\n');
				count++;
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return new Result(count, HexFormat.of().formatHex(sha256.digest()), counting.count);
	}

	private static MessageDigest sha256() {
		try {
			return MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private static final class CountingOutputStream extends java.io.FilterOutputStream {

		private long count;

		CountingOutputStream(OutputStream out) {
			super(out);
		}

		@Override
		public void write(int b) throws IOException {
			out.write(b);
			count++;
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			count += len;
		}
	}
}
