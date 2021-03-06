package org.pharmgkb.parsers.model;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * A strand of a chromosome.
 * <ul>
 *     <li>PLUS (+)</li>
 *     <li>MINUS (-)</li>
 * </ul>
 * @author Douglas Myers-Turnbull
 */
public enum Strand {

	PLUS("+"), MINUS("-");

	private final String m_symbol;

	Strand(@Nonnull String symbol) {
		m_symbol = symbol;
	}

	@Nonnull
	public String getSymbol() {
		return m_symbol;
	}

	@Nonnull
	public static Optional<Strand> lookupBySymbol(@Nonnull String symbol) {
		return switch (symbol) {
			case "+" -> Optional.of(PLUS);
			case "-" -> Optional.of(MINUS);
			default -> Optional.empty();
		};
	}
}
