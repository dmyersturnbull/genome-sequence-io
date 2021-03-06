package org.pharmgkb.parsers.gff.model;

import com.google.common.collect.ImmutableMap;
import org.pharmgkb.parsers.gff.Gff3Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A line of GFF3 data.
 * See {@link Gff3Parser} for more information.
 * <strong>Note that coordinates here are 0-based, but they are 1-based in GFF3.</strong>
 * @author Douglas Myers-Turnbull
 */
@Immutable
public class Gff3Feature extends BaseGffFeature {

	private static final Logger sf_logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final ImmutableMap<String, List<String>> m_attributes;

	private Gff3Feature(@Nonnull Builder builder) {
		super(builder);
		m_attributes = ImmutableMap.copyOf(builder.m_attributes);
	}

	@Nonnull
	public List<String> getAttributes(@Nonnull Gff3Attribute key) {
		return getAttributes(key.getId());
	}

	@Nonnull
	public List<String> getAttributes(@Nonnull String key) {
		List<String> found = m_attributes.get(key);
		return found==null? Collections.emptyList() : found;
	}

	@Nonnull
	public ImmutableMap<String, List<String>> getAttributes() {
		return m_attributes;
	}

	@NotThreadSafe
	public static class Builder extends BaseGffFeature.Builder<Gff3Feature, Builder> {

		private Map<String, List<String>> m_attributes;

		@Nonnull
		public Builder(@Nonnull String coordinateSystemId, @Nonnull String type, @Nonnegative long start, @Nonnegative long end) {
			super(coordinateSystemId, type, start, end);
			m_attributes = new TreeMap<>(); // for obvious sort order
		}

		public Builder(@Nonnull Builder builder) {
			super(builder);
			m_attributes = new TreeMap<>(); // for obvious sort order
			builder.m_attributes.forEach(m_attributes::put);
		}

		public Builder(@Nonnull Gff3Feature feature) {
			super(feature);
			m_attributes = new TreeMap<>(); // for obvious sort order
			feature.m_attributes.forEach(m_attributes::put);
		}

		@Nonnull
		public Builder putAttributes(@Nonnull Map<String, ? extends List<String>> attributes) {
			m_attributes.putAll(attributes);
			return this;
		}

		@Nonnull
		public Builder putAttributes(@Nonnull String key, @Nonnull List<String> values) {
			m_attributes.put(key, values);
			return this;
		}

		@Nonnull
		public Builder clearAttributes() {
			m_attributes.clear();
			return this;
		}

		@Nonnull
		public Gff3Feature build() {
			if ("CDS".equalsIgnoreCase(m_type) && m_phase.isEmpty()) {
				sf_logger.warn("The feature starting at {} and ending at {} is of type CDS but no phase is given", m_start, m_end);
			}
			return new Gff3Feature(this);
		}
	}

}
