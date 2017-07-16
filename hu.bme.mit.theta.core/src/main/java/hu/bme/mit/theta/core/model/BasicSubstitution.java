package hu.bme.mit.theta.core.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import hu.bme.mit.theta.common.ObjectUtils;
import hu.bme.mit.theta.core.Decl;
import hu.bme.mit.theta.core.Expr;
import hu.bme.mit.theta.core.Type;

/**
 * Basic, immutable implementation of a substitution.
 */
public final class BasicSubstitution implements Substitution {
	private static final int HASH_SEED = 2521;
	private volatile int hashCode = 0;

	private final Collection<Decl<?>> decls;
	private final Map<Decl<?>, Expr<?>> declToExpr;

	private BasicSubstitution(final Builder builder) {
		this.declToExpr = builder.declToExpr;
		this.decls = Collections.unmodifiableSet(this.declToExpr.keySet());
	}

	@Override
	public <DeclType extends Type> Optional<Expr<DeclType>> eval(final Decl<DeclType> decl) {
		checkNotNull(decl);
		if (declToExpr.containsKey(decl)) {
			@SuppressWarnings("unchecked")
			final Expr<DeclType> val = (Expr<DeclType>) declToExpr.get(decl);
			return Optional.of(val);
		}
		return Optional.empty();
	}

	@Override
	public Collection<? extends Decl<?>> getDecls() {
		return decls;
	}

	@Override
	public String toString() {
		return ObjectUtils.toStringBuilder("Substitution")
				.addAll(declToExpr.entrySet(), e -> e.getKey().getName() + " <- " + e.getValue()).toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof BasicSubstitution) {
			final BasicSubstitution that = (BasicSubstitution) obj;
			return this.declToExpr.equals(that.declToExpr);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = hashCode;
		if (result == 0) {
			result = HASH_SEED;
			result = 31 * result + declToExpr.hashCode();
			hashCode = result;
		}
		return result;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private final Map<Decl<?>, Expr<?>> declToExpr;
		private boolean built;

		private Builder() {
			this(new HashMap<>());
		}

		private Builder(final Map<Decl<?>, Expr<?>> declToExpr) {
			// LinkedHashMap is used for deterministic order
			this.declToExpr = new LinkedHashMap<>(declToExpr);
			this.built = false;
		}

		public Builder put(final Decl<?> decl, final Expr<?> value) {
			checkState(!built, "Builder was already built.");
			checkArgument(value.getType().equals(decl.getType()), "Type mismatch.");
			declToExpr.put(decl, value);
			return this;
		}

		public Builder putAll(final Map<Decl<?>, Expr<?>> declToExpr) {
			checkState(!built, "Builder was already built.");
			declToExpr.entrySet().forEach(e -> put(e.getKey(), e.getValue()));
			return this;
		}

		public Substitution build() {
			built = true;
			return new BasicSubstitution(this);
		}
	}
}