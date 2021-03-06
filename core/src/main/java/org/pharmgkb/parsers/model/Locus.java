package org.pharmgkb.parsers.model;


import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A locus on a reference genome. As always in this package, the position is 0-based. Negative values are permitted.
 * @author Douglas Myers-Turnbull
 */
@Immutable
public class Locus implements Comparable<Locus> {

	private static final Pattern sf_pattern = Pattern.compile("^(chr(?:(?:\\d{1,2})|X|Y|M))\\(([+\\-?])\\):(-?\\d+)$");

	private final ChromosomeName m_chromosome;

    private final long m_position;

    private final Strand m_strand;

	@Nonnull
	public static Locus parse(@Nonnull String string) {
        Preconditions.checkNotNull(string);
		Matcher matcher = sf_pattern.matcher(string);
		if (matcher.matches()) {
			ChromosomeName chr = new ChromosomeName(matcher.group(1));
			Optional<Strand> strand = Strand.lookupBySymbol(matcher.group(2));
            if (strand.isPresent()) {
                long pos = Long.parseLong(matcher.group(3));
                return new Locus(chr, pos, strand.get());
            }
            throw new IllegalArgumentException("String " + string + " is not a valid locus: the strand is wrong");
		}
		throw new IllegalArgumentException("String " + string + " is not a valid locus");
	}

	/**
	 * @param chromosome A chromosome name from GRCh38 (starts with "chr"; the mitochrondrial chromosome is "chrM")
	 * @param position A 0-based position on the chromosome
	 * @param strand "+", "-", "?"
	 */
	public Locus(@Nonnull String chromosome, @Nonnegative long position, @Nonnull String strand) {
		Preconditions.checkNotNull(chromosome);
		Preconditions.checkNotNull(strand);
        Optional<Strand> strandInstance = Strand.lookupBySymbol(strand);
        Preconditions.checkArgument(strandInstance.isPresent(), "Unknown strand " + strand);
		m_chromosome = new ChromosomeName(chromosome);
		m_position = position;
		m_strand = strandInstance.get();
	}

    /**
     * @param chromosome A chromosome name from GRCh38 (starts with "chr"; the mitochrondrial chromosome is "chrM")
     * @param position A 0-based position on the chromosome
     */
    public Locus(@Nonnull ChromosomeName chromosome, long position, @Nonnull Strand strand) {
		Preconditions.checkNotNull(chromosome);
		Preconditions.checkNotNull(strand);
		m_chromosome = chromosome;
		m_position = position;
		m_strand = strand;
    }

	public Locus(@Nonnull String chromosome, long position, @Nonnull Strand strand) {
		Preconditions.checkNotNull(chromosome);
		Preconditions.checkNotNull(strand);
		m_chromosome = new ChromosomeName(chromosome);
		m_position = position;
		m_strand = strand;
	}

	/**
     * @return A standard chromosome name
     */
    @Nonnull
    public ChromosomeName getChromosome() {
        return m_chromosome;
    }

    /**
     * @return A 0-based position on the chromosome.
     */
    public long getPosition() {
        return m_position;
    }

	@Nonnull
    public Strand getStrand() {
        return m_strand;
    }

    public boolean isCompatibleWith(@Nonnull Locus locus) {
        return m_chromosome.equals(locus.m_chromosome) && m_strand == locus.m_strand;
    }

	@Nonnull
    @Override
    public String toString() {
        return m_chromosome + "(" + m_strand.getSymbol() + ")" + ":" + m_position;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Locus locus = (Locus) o;
        return m_position == locus.m_position && m_chromosome.equals(locus.m_chromosome) && m_strand == locus.m_strand;
    }

    @Override
    public int hashCode() {
	    return Objects.hash(m_chromosome, m_position, m_strand);
    }

    /**
     * Compares the chromosome, followed by the position, followed by the strand.
     */
    @Override
    public int compareTo(@Nonnull Locus o) {
        return ComparisonChain.start()
                .compare(m_chromosome, o.m_chromosome)
                .compare(m_position, o.m_position)
                .compare(m_strand, o.m_strand)
                .result();
    }
}
