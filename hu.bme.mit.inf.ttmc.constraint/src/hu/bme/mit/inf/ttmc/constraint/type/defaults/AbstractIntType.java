package hu.bme.mit.inf.ttmc.constraint.type.defaults;

import java.util.Optional;

import hu.bme.mit.inf.ttmc.constraint.ConstraintManager;
import hu.bme.mit.inf.ttmc.constraint.expr.Expr;
import hu.bme.mit.inf.ttmc.constraint.type.IntType;
import hu.bme.mit.inf.ttmc.constraint.type.RatType;
import hu.bme.mit.inf.ttmc.constraint.type.Type;
import hu.bme.mit.inf.ttmc.constraint.utils.TypeVisitor;

public abstract class AbstractIntType extends AbstractBaseType implements IntType {

	private static final int HASH_SEED = 222670;

	private static final String TYPE_LABEL = "Int";

	private final ConstraintManager manager;

	public AbstractIntType(final ConstraintManager manager) {
		this.manager = manager;
	}

	@Override
	public final Expr<IntType> getAny() {
		return manager.getExprFactory().Int(0);
	}

	@Override
	public final boolean isLeq(final Type type) {
		return type instanceof AbstractIntType || type instanceof AbstractRatType;
	}

	@Override
	public final Optional<? extends IntType> meet(final Type type) {
		if (type.isLeq(this)) {
			assert type instanceof IntType;
			final IntType that = (IntType) type;
			return Optional.of(that);
		} else if (this.isLeq(type)) {
			return Optional.of(this);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public final Optional<? extends RatType> join(final Type type) {
		if (type.isLeq(this)) {
			return Optional.of(this);
		} else if (this.isLeq(type)) {
			assert type instanceof RatType;
			final RatType that = (RatType) type;
			return Optional.of(that);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public final <P, R> R accept(final TypeVisitor<? super P, ? extends R> visitor, final P param) {
		return visitor.visit(this, param);
	}

	@Override
	public final int hashCode() {
		return HASH_SEED;
	}

	@Override
	public final boolean equals(final Object obj) {
		return (obj instanceof AbstractIntType);
	}

	@Override
	public final String toString() {
		return TYPE_LABEL;
	}

}