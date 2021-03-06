package org.pharmgkb.parsers.turtle.model;

import com.google.common.base.MoreObjects;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import java.util.Objects;


/**
 * @author Douglas Myers-Turnbull
 */
@Immutable
public class Triple {

	private final Node m_subject;
	private final Node m_predicate;
	private final Node m_object;

	public Triple(@Nonnull Node subject, @Nonnull Node predicate, @Nonnull Node object) {
		m_subject = subject;
		m_predicate = predicate;
		m_object = object;
	}

	@Nonnull
	public Node getSubject() {
		return m_subject;
	}

	@Nonnull
	public Node getPredicate() {
		return m_predicate;
	}

	@Nonnull
	public Node getObject() {
		return m_object;
	}

	@Nonnull
	public String asLine() {
		return m_subject.asString() + " " + m_predicate.asString() + " " + m_object.asString() + " .";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Triple triple = (Triple) o;
		return Objects.equals(m_subject, triple.m_subject) &&
				Objects.equals(m_predicate, triple.m_predicate) &&
				Objects.equals(m_object, triple.m_object);
	}

	@Override
	public int hashCode() {
		return Objects.hash(m_subject, m_predicate, m_object);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("subject", m_subject)
				.add("predicate", m_predicate)
				.add("object", m_object)
				.toString();
	}
}
