package hu.bme.mit.theta.formalism.sts.aiger.elements;

import static hu.bme.mit.theta.core.type.booltype.BoolExprs.Not;

import java.util.List;

import hu.bme.mit.theta.core.type.Expr;
import hu.bme.mit.theta.core.type.booltype.BoolType;

public final class OutVar extends HwElement {
	private final int literal;

	public OutVar(final String token) {
		this(Integer.parseInt(token));
	}

	public OutVar(final int literal) {
		super(-1);
		this.literal = literal;
	}

	@Override
	public Expr<BoolType> getExpr(final List<HwElement> elements) {
		Expr<BoolType> expr = elements.get(literal / 2).getExpr(elements);
		if (literal % 2 != 0)
			expr = Not(expr);
		return expr;
	}

	@Override
	public int getVarId() {
		throw new UnsupportedOperationException("OutVars do not have corresponding ID.");
	}

}