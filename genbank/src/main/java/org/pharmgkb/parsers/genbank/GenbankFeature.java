package org.pharmgkb.parsers.genbank;

import com.google.common.base.MoreObjects;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class GenbankFeature {

	private String m_kind;
	private GenbankSequenceRange m_range;
	private LinkedHashMap<String, String> m_properties;
	private List<String> m_extraLines;

	public GenbankFeature(String kind, GenbankSequenceRange range, LinkedHashMap<String, String> properties, List<String> extraLines) {
		m_kind = kind;
		m_range = range;
		m_properties = properties;
		m_extraLines = extraLines;
	}

	@Nonnull
	public String getKind() {
		return m_kind;
	}

	@Nonnull
	public GenbankSequenceRange getRange() {
		return m_range;
	}

	/**
	 * All of the properties beginning with '/'.
	 * The slash is removed from the key, and quotes are stripped from the values.
	 */
	@Nonnull
	public LinkedHashMap<String, String> getProperties() {
		return m_properties;
	}

	/**
	 * Lines at the end not conforming to GenBank format. Whitespace is still trimmed.
	 */
	@Nonnull
	public List<String> getExtraLines() {
		return m_extraLines;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("m_kind", m_kind)
				.add("m_range", m_range)
				.add("m_properties", m_properties)
				.add("m_extraLines", m_extraLines)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GenbankFeature that = (GenbankFeature) o;
		return Objects.equals(m_kind, that.m_kind) &&
				Objects.equals(m_range, that.m_range) &&
				Objects.equals(m_properties, that.m_properties) &&
				Objects.equals(m_extraLines, that.m_extraLines);
	}

	@Override
	public int hashCode() {
		return Objects.hash(m_kind, m_range, m_properties, m_extraLines);
	}
}