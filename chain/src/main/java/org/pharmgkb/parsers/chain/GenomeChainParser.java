package org.pharmgkb.parsers.chain;

import com.google.common.base.Preconditions;
import org.pharmgkb.parsers.BadDataFormatException;
import org.pharmgkb.parsers.LineStructureParser;
import org.pharmgkb.parsers.chain.model.GenomeChain;
import org.pharmgkb.parsers.model.ChromosomeName;
import org.pharmgkb.parsers.model.Locus;
import org.pharmgkb.parsers.model.LocusRange;
import org.pharmgkb.parsers.model.Strand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * See <a href="https://genome.ucsc.edu/goldenPath/help/chain.html">https://genome.ucsc.edu/goldenPath/help/chain.html</a>.
 * Example usage:
 * <code>
 *     // "lift over" a list of loci, ignoring ones that couldn't be lifted over
 *     GenomeChain chain = new GenomeChainParser().apply(Files.lines(null));
 *     List&lt;Locus&gt; liftedOver = lociList.parallelStream()
 *                                      .map(chain).filter(Optional::isPresent)
 *                                      .collect(Collectors.toList());
 * </code>
 * @author Douglas Myers-Turnbull
 */
@ThreadSafe // note that this is still thread safe even though LineConsumer is not
public class GenomeChainParser implements LineStructureParser<GenomeChain> {

	private static final long sf_logEvery = 10000;

	private static final Pattern sf_whitespace = Pattern.compile("\\s+");

	private static final Logger sf_logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private AtomicLong m_lineNumber = new AtomicLong(0l);

	/**
	 * @throws IllegalArgumentException If {@code stream} is parallel
	 */
	@Nonnull
	@Override
	public GenomeChain apply(@Nonnull Stream<String> stream) {
		Preconditions.checkArgument(!stream.isParallel(), "Stream for genome chain cannot be parallel");
		GenomeChain.Builder chain = new GenomeChain.Builder();
		stream.filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
				.forEach(new LineConsumer(chain));
		return chain.build();
	}

	@NotThreadSafe
	private final class LineConsumer implements Consumer<String> {

		private long sourcePosition = 0;
		private long targetPosition = 0;
		private long sourceEnd = 0;
		private long targetEnd = 0;
		private ChromosomeName sourceChr = null;
		private ChromosomeName targetChr = null;
		private Optional<Strand> sourceStrand = Optional.empty();
		private Optional<Strand> targetStrand = Optional.empty();

		private GenomeChain.Builder m_chain;

		public LineConsumer(@Nonnull GenomeChain.Builder chain) {
			m_chain = chain;
		}

		@Override
		public void accept(@Nonnull String line) {

			if (m_lineNumber.incrementAndGet() % sf_logEvery == 0) {
				sf_logger.debug("Reading line #{}", m_lineNumber);
			}

			try {
				String[] parts = sf_whitespace.split(line);

				if (line.startsWith("chain")) {

					sourceChr = new ChromosomeName(parts[2]);
					targetChr = new ChromosomeName(parts[7]);
					sourcePosition = Long.parseLong(parts[5]);
					targetPosition = Long.parseLong(parts[10]);
					sourceEnd = Long.parseLong(parts[6]);
					targetEnd = Long.parseLong(parts[11]);
					sourceStrand = Strand.lookupBySymbol(parts[4]);
					targetStrand = Strand.lookupBySymbol(parts[9]);
					//noinspection OptionalGetWithoutIsPresent
					sf_logger.trace(
							"\nCHAIN: {}    ---->    {}",
							new Locus(sourceChr, sourcePosition, sourceStrand.get()),
							new Locus(targetChr, targetPosition, targetStrand.get())
					);
					sf_logger.trace("------------------------------------------------");

				} else {

					int diagonal = Integer.parseInt(parts[0]);
					int sourceGap = 0, targetGap = 0;
					if (parts.length > 1) {
						sourceGap = Integer.parseInt(parts[1]);
						targetGap = Integer.parseInt(parts[2]);
					}

					//noinspection OptionalGetWithoutIsPresent
					LocusRange source = new LocusRange(
							new Locus(sourceChr, sourcePosition, sourceStrand.get()),
							new Locus(sourceChr, sourcePosition + diagonal, sourceStrand.get())
					);
					//noinspection OptionalGetWithoutIsPresent
					LocusRange target = new LocusRange(
							new Locus(targetChr, targetPosition, targetStrand.get()),
							new Locus(targetChr, targetPosition + diagonal, targetStrand.get())
					);

					sf_logger.trace("{}    ---->    {}", source, target);
					m_chain.add(source, target);
					sourcePosition += diagonal + sourceGap;
					targetPosition += diagonal + targetGap;

					if (parts.length == 1) {
						if (sourcePosition != sourceEnd) {
							throw new BadDataFormatException(
									"Should end block at position " + sourceEnd + ","
									+ " but ended at position " + sourcePosition + ", at line #" + m_lineNumber
							);
						}
						if (targetPosition != targetEnd) {
							throw new BadDataFormatException(
									"Should end block at position " + targetEnd + ","
									+ " but ended at position " + targetPosition + ", at line #" + m_lineNumber
							);
						}
					}

				} // end of if-else

			} catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
				throw new BadDataFormatException("Couldn't parse line #" + m_lineNumber, e);
			} catch (RuntimeException e) {
				// this is a little weird, but it's helpful
				// not that we're not throwing a BadDataFormatException because we don't expect AIOOB, e.g.
				e.addSuppressed(new RuntimeException("Unexpectedly failed to parse line " + m_lineNumber));
				throw e;
			}
		}
	}

	@Nonnegative
	@Override
	public long nLinesProcessed() {
		return m_lineNumber.get();
	}
}
