package hu.bme.mit.inf.ttmc.solver.impl;

import java.util.Collection;

import hu.bme.mit.inf.ttmc.common.Stack;
import hu.bme.mit.inf.ttmc.common.StackImpl;
import hu.bme.mit.inf.ttmc.core.expr.Expr;
import hu.bme.mit.inf.ttmc.core.type.BoolType;
import hu.bme.mit.inf.ttmc.solver.ItpMarker;

public class ItpMarkerImpl implements ItpMarker {

	private final Stack<Expr<? extends BoolType>> assertions;
	
	public ItpMarkerImpl() {
		assertions = new StackImpl<>();
	}
	
	public void add(Expr<? extends BoolType> assertion) {
		assertions.add(assertion);
	}
	
	public void push() {
		assertions.push();
	}
	
	public void pop(final int n) {
		assertions.pop(n);
	}
	
	@Override
	public Collection<? extends Expr<? extends BoolType>> getAssertions() {
		return assertions.toCollection();
	}

}