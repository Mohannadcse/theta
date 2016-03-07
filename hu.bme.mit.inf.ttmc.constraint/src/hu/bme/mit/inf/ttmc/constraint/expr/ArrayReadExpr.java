package hu.bme.mit.inf.ttmc.constraint.expr;

import hu.bme.mit.inf.ttmc.constraint.type.ArrayType;
import hu.bme.mit.inf.ttmc.constraint.type.Type;

public interface ArrayReadExpr<IndexType extends Type, ElemType extends Type> extends Expr<ElemType> {
	
	public Expr<? extends ArrayType<? super IndexType, ? extends ElemType>> getArray();
	public Expr<? extends IndexType> getIndex();

	public ArrayReadExpr<IndexType, ElemType> with(
			final Expr<? extends ArrayType<? super IndexType, ? extends ElemType>> array,
			final Expr<? extends IndexType> index);

	public ArrayReadExpr<IndexType, ElemType> withArray(
			final Expr<? extends ArrayType<? super IndexType, ? extends ElemType>> array);

	public ArrayReadExpr<IndexType, ElemType> withIndex(final Expr<? extends IndexType> index);

}