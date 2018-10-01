package org.pharmgkb.parsers.bgee;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import org.pharmgkb.parsers.BadDataFormatException;
import org.pharmgkb.parsers.LineParser;
import org.pharmgkb.parsers.MultilineParser;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * @author Douglas Myers-Turnbull
 */
@NotThreadSafe
public class BgeeExpressionParser implements MultilineParser<BgeeExpression> {

	private static final Splitter sf_tab = Splitter.on("\t");

	private AtomicReference<Map<String, Integer>> m_parts = new AtomicReference<>(new LinkedHashMap<>());
	private AtomicInteger m_lineNumber = new AtomicInteger(0);

	@Nonnull
	@Override
	public Stream<BgeeExpression> parseAll(@Nonnull Stream<String> stream) throws IOException, BadDataFormatException {
		return stream.flatMap(this);
	}


	@Nonnull
	@Override
	public Stream<BgeeExpression> apply(@Nonnull String line) throws BadDataFormatException {
		try {

			int ln = m_lineNumber.getAndIncrement();
			List<String> tabs = sf_tab.splitToList(line);
			Function<String, String> unq = s -> tabs.get(m_parts.get().get(s)).replaceAll("^\"|\"$", "");

			if (ln == 0 && m_parts.get().isEmpty()) {
				for (int i = 0; i < tabs.size(); i++) {
					m_parts.get().put(tabs.get(i).replaceAll("^\"|\"$", ""), i);
				}
				return Stream.empty();
			}
			if (ln == 0 || m_parts.get().isEmpty()) {
				throw new IllegalStateException("Header is missing");
			}

			Map<String, String> info = new LinkedHashMap<>();
			for (Map.Entry<String, Integer> column : m_parts.get().entrySet()) {
				info.put(column.getKey().replaceAll("^\"|\"$", ""), tabs.get(column.getValue()).replaceAll("^\"|\"$", ""));
			}

			//public BgeeExpression(String geneName, String tissueId, String tissueName, String stageId, String stageName, boolean isExpressed, Quality quality, BigDecimal level, ImmutableMap<String, String> extendedInfo
			//Gene ID "Gene name"     Anatomical entity ID    "Anatomical entity name"        Developmental stage ID  "Developmental stage name"      Expression      Call quality    Expression rank
			return Stream.of(new BgeeExpression(
					unq.apply("Gene ID"), unq.apply("Gene name"),
					unq.apply("Anatomical entity ID"), unq.apply("Anatomical entity name"),
					unq.apply("Developmental stage ID"), unq.apply("Developmental stage name"),
					unq.apply("Expression").equals("present"), Quality.find(unq.apply("Call quality").replace(" quality", "")),
					new BigDecimal(unq.apply("Expression rank")), ImmutableMap.copyOf(info)
			));
		} catch (NumberFormatException | NullPointerException | IndexOutOfBoundsException e) {
			throw new BadDataFormatException("Failed to process line #" + m_lineNumber + ": " + line, e);
		}
	}

	@Nonnegative
	@Override
	public long nLinesProcessed() {
		return m_lineNumber.get();
	}

	@Override
	public String toString() {
		return "BgeeExpressionParser{" +
				"m_lineNumber=" + m_lineNumber +
				'}';
	}
}
