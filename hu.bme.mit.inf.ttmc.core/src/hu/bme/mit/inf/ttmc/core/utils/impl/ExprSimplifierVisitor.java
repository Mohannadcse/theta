package hu.bme.mit.inf.ttmc.core.utils.impl;

import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.Add;
import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.Bool;
import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.False;
import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.Int;
import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.Ite;
import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.Mul;
import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.Neg;
import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.Not;
import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.Rat;
import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.Sub;
import static hu.bme.mit.inf.ttmc.core.expr.impl.Exprs.True;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import hu.bme.mit.inf.ttmc.core.expr.AddExpr;
import hu.bme.mit.inf.ttmc.core.expr.AndExpr;
import hu.bme.mit.inf.ttmc.core.expr.ArrayReadExpr;
import hu.bme.mit.inf.ttmc.core.expr.ArrayWriteExpr;
import hu.bme.mit.inf.ttmc.core.expr.BoolLitExpr;
import hu.bme.mit.inf.ttmc.core.expr.ConstRefExpr;
import hu.bme.mit.inf.ttmc.core.expr.EqExpr;
import hu.bme.mit.inf.ttmc.core.expr.ExistsExpr;
import hu.bme.mit.inf.ttmc.core.expr.Expr;
import hu.bme.mit.inf.ttmc.core.expr.FalseExpr;
import hu.bme.mit.inf.ttmc.core.expr.ForallExpr;
import hu.bme.mit.inf.ttmc.core.expr.FuncAppExpr;
import hu.bme.mit.inf.ttmc.core.expr.FuncLitExpr;
import hu.bme.mit.inf.ttmc.core.expr.GeqExpr;
import hu.bme.mit.inf.ttmc.core.expr.GtExpr;
import hu.bme.mit.inf.ttmc.core.expr.IffExpr;
import hu.bme.mit.inf.ttmc.core.expr.ImplyExpr;
import hu.bme.mit.inf.ttmc.core.expr.IntDivExpr;
import hu.bme.mit.inf.ttmc.core.expr.IntLitExpr;
import hu.bme.mit.inf.ttmc.core.expr.IteExpr;
import hu.bme.mit.inf.ttmc.core.expr.LeqExpr;
import hu.bme.mit.inf.ttmc.core.expr.LitExpr;
import hu.bme.mit.inf.ttmc.core.expr.LtExpr;
import hu.bme.mit.inf.ttmc.core.expr.ModExpr;
import hu.bme.mit.inf.ttmc.core.expr.MulExpr;
import hu.bme.mit.inf.ttmc.core.expr.NegExpr;
import hu.bme.mit.inf.ttmc.core.expr.NeqExpr;
import hu.bme.mit.inf.ttmc.core.expr.NotExpr;
import hu.bme.mit.inf.ttmc.core.expr.OrExpr;
import hu.bme.mit.inf.ttmc.core.expr.ParamRefExpr;
import hu.bme.mit.inf.ttmc.core.expr.RatDivExpr;
import hu.bme.mit.inf.ttmc.core.expr.RatLitExpr;
import hu.bme.mit.inf.ttmc.core.expr.RemExpr;
import hu.bme.mit.inf.ttmc.core.expr.SubExpr;
import hu.bme.mit.inf.ttmc.core.expr.TrueExpr;
import hu.bme.mit.inf.ttmc.core.model.Assignment;
import hu.bme.mit.inf.ttmc.core.type.BoolType;
import hu.bme.mit.inf.ttmc.core.type.IntType;
import hu.bme.mit.inf.ttmc.core.type.RatType;
import hu.bme.mit.inf.ttmc.core.type.Type;
import hu.bme.mit.inf.ttmc.core.type.closure.ClosedUnderAdd;
import hu.bme.mit.inf.ttmc.core.type.closure.ClosedUnderMul;
import hu.bme.mit.inf.ttmc.core.type.closure.ClosedUnderNeg;
import hu.bme.mit.inf.ttmc.core.type.closure.ClosedUnderSub;
import hu.bme.mit.inf.ttmc.core.utils.ExprVisitor;

public class ExprSimplifierVisitor implements ExprVisitor<Assignment, Expr<? extends Type>> {

	@Override
	public <DeclType extends Type> Expr<? extends DeclType> visit(final ConstRefExpr<DeclType> expr, final Assignment param) {
		final Optional<LitExpr<DeclType>> eval = param.eval(expr.getDecl());
		if (eval.isPresent()) {
			return eval.get();
		}

		return expr;
	}

	@Override
	public <DeclType extends Type> Expr<? extends Type> visit(final ParamRefExpr<DeclType> expr, final Assignment param) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Expr<? extends BoolType> visit(final FalseExpr expr, final Assignment param) {
		return expr;
	}

	@Override
	public Expr<? extends BoolType> visit(final TrueExpr expr, final Assignment param) {
		return expr;
	}

	@Override
	public Expr<? extends BoolType> visit(final NotExpr expr, final Assignment param) {
		final Expr<? extends BoolType> op = ExprUtils.cast(expr.getOp().accept(this, param), BoolType.class);
		if (op instanceof NotExpr) {
			return ((NotExpr) op).getOp();
		} else if (op instanceof TrueExpr) {
			return False();
		} else if (op instanceof FalseExpr) {
			return True();
		}

		return expr.withOp(op);
	}

	@Override
	public Expr<? extends BoolType> visit(final ImplyExpr expr, final Assignment param) {
		final Expr<? extends BoolType> leftOp = ExprUtils.cast(expr.getLeftOp().accept(this, param), BoolType.class);
		final Expr<? extends BoolType> rightOp = ExprUtils.cast(expr.getRightOp().accept(this, param), BoolType.class);

		if (leftOp instanceof BoolLitExpr && rightOp instanceof BoolLitExpr) {
			final boolean leftValue = ((BoolLitExpr) leftOp).getValue();
			final boolean rightValue = ((BoolLitExpr) rightOp).getValue();
			return Bool(!leftValue || rightValue);
		}

		if (leftOp instanceof FalseExpr || rightOp instanceof TrueExpr) {
			return True();
		} else if (leftOp instanceof TrueExpr) {
			return rightOp;
		} else if (rightOp instanceof FalseExpr) {
			return ExprUtils.cast(Not(leftOp).accept(this, param), BoolType.class);
		}

		return expr.withOps(leftOp, rightOp);
	}

	@Override
	public Expr<? extends BoolType> visit(final IffExpr expr, final Assignment param) {
		final Expr<? extends BoolType> leftOp = ExprUtils.cast(expr.getLeftOp().accept(this, param), BoolType.class);
		final Expr<? extends BoolType> rightOp = ExprUtils.cast(expr.getRightOp().accept(this, param), BoolType.class);

		if (leftOp instanceof BoolLitExpr && rightOp instanceof BoolLitExpr) {
			final boolean leftValue = ((BoolLitExpr) leftOp).getValue();
			final boolean rightValue = ((BoolLitExpr) rightOp).getValue();
			return Bool(leftValue == rightValue);
		}

		if (leftOp instanceof TrueExpr) {
			return rightOp;
		} else if (rightOp instanceof TrueExpr) {
			return leftOp;
		} else if (leftOp instanceof FalseExpr) {
			return ExprUtils.cast(Not(rightOp).accept(this, param), BoolType.class);
		} else if (rightOp instanceof FalseExpr) {
			return ExprUtils.cast(Not(leftOp).accept(this, param), BoolType.class);
		}

		return expr.withOps(leftOp, rightOp);
	}

	@Override
	public Expr<? extends BoolType> visit(final AndExpr expr, final Assignment param) {
		final List<Expr<? extends BoolType>> ops = new ArrayList<>();

		if (expr.getOps().size() == 0)
			return True();

		for (final Expr<? extends BoolType> op : expr.getOps()) {
			final Expr<? extends BoolType> opVisited = ExprUtils.cast(op.accept(this, param), BoolType.class);
			if (opVisited instanceof TrueExpr) {
				continue;
			} else if (opVisited instanceof FalseExpr) {
				return False();
			} else if (opVisited instanceof AndExpr) {
				ops.addAll(((AndExpr) opVisited).getOps());
			} else {
				ops.add(opVisited);
			}
		}

		if (ops.size() == 0) {
			return True();
		} else if (ops.size() == 1) {
			return ops.iterator().next();
		}

		return expr.withOps(ops);
	}

	@Override
	public Expr<? extends BoolType> visit(final OrExpr expr, final Assignment param) {
		final List<Expr<? extends BoolType>> ops = new ArrayList<>();

		if (expr.getOps().size() == 0)
			return True();

		for (final Expr<? extends BoolType> op : expr.getOps()) {
			final Expr<? extends BoolType> opVisited = ExprUtils.cast(op.accept(this, param), BoolType.class);
			if (opVisited instanceof FalseExpr) {
				continue;
			} else if (opVisited instanceof TrueExpr) {
				return True();
			} else if (opVisited instanceof OrExpr) {
				ops.addAll(((OrExpr) opVisited).getOps());
			} else {
				ops.add(opVisited);
			}
		}

		if (ops.size() == 0) {
			return False();
		} else if (ops.size() == 1) {
			return ops.iterator().next();
		}

		return expr.withOps(ops);
	}

	@Override
	public Expr<? extends Type> visit(final ExistsExpr expr, final Assignment param) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Expr<? extends Type> visit(final ForallExpr expr, final Assignment param) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public Expr<? extends BoolType> visit(final EqExpr expr, final Assignment param) {
		final Expr<? extends Type> leftOp = expr.getLeftOp().accept(this, param);
		final Expr<? extends Type> rightOp = expr.getRightOp().accept(this, param);

		if ((leftOp instanceof BoolLitExpr && rightOp instanceof BoolLitExpr) || (leftOp instanceof IntLitExpr && rightOp instanceof IntLitExpr)
				|| (leftOp instanceof RatLitExpr && rightOp instanceof RatLitExpr)) {
			return Bool(leftOp.equals(rightOp));
		} else if (leftOp instanceof ConstRefExpr && rightOp instanceof ConstRefExpr) {
			if (leftOp.equals(rightOp))
				return True();
		}

		return expr.withOps(leftOp, rightOp);
	}

	@Override
	public Expr<? extends BoolType> visit(final NeqExpr expr, final Assignment param) {
		final Expr<? extends Type> leftOp = expr.getLeftOp().accept(this, param);
		final Expr<? extends Type> rightOp = expr.getRightOp().accept(this, param);

		if ((leftOp instanceof BoolLitExpr && rightOp instanceof BoolLitExpr) || (leftOp instanceof IntLitExpr && rightOp instanceof IntLitExpr)
				|| (leftOp instanceof RatLitExpr && rightOp instanceof RatLitExpr)) {
			return Bool(!leftOp.equals(rightOp));
		} else if (leftOp instanceof ConstRefExpr && rightOp instanceof ConstRefExpr) {
			if (leftOp.equals(rightOp))
				return False();
		}

		return expr.withOps(leftOp, rightOp);
	}

	@Override
	public Expr<? extends BoolType> visit(final GeqExpr expr, final Assignment param) {
		final Expr<? extends RatType> leftOp = ExprUtils.cast(expr.getLeftOp().accept(this, param), RatType.class);
		final Expr<? extends RatType> rightOp = ExprUtils.cast(expr.getRightOp().accept(this, param), RatType.class);

		if (leftOp instanceof ConstRefExpr && rightOp instanceof ConstRefExpr) {
			if (leftOp.equals(rightOp))
				return True();
		}

		if ((leftOp instanceof RatLitExpr || leftOp instanceof IntLitExpr) && (rightOp instanceof RatLitExpr || rightOp instanceof IntLitExpr)) {
			final long leftNum = num(leftOp);
			final long leftDenom = denom(leftOp);
			final long rightNum = num(rightOp);
			final long rightDenom = denom(rightOp);

			assert (leftDenom > 0);
			assert (rightDenom > 0);

			return Bool(leftNum * rightDenom >= rightNum * leftDenom);
		}

		return expr.withOps(leftOp, rightOp);
	}

	@Override
	public Expr<? extends BoolType> visit(final GtExpr expr, final Assignment param) {
		final Expr<? extends RatType> leftOp = ExprUtils.cast(expr.getLeftOp().accept(this, param), RatType.class);
		final Expr<? extends RatType> rightOp = ExprUtils.cast(expr.getRightOp().accept(this, param), RatType.class);

		if (leftOp instanceof ConstRefExpr && rightOp instanceof ConstRefExpr) {
			if (leftOp.equals(rightOp))
				return False();
		}

		if ((leftOp instanceof RatLitExpr || leftOp instanceof IntLitExpr) && (rightOp instanceof RatLitExpr || rightOp instanceof IntLitExpr)) {
			final long leftNum = num(leftOp);
			final long leftDenom = denom(leftOp);
			final long rightNum = num(rightOp);
			final long rightDenom = denom(rightOp);

			assert (leftDenom > 0);
			assert (rightDenom > 0);

			return Bool(leftNum * rightDenom > rightNum * leftDenom);
		}

		return expr.withOps(leftOp, rightOp);
	}

	@Override
	public Expr<? extends BoolType> visit(final LeqExpr expr, final Assignment param) {
		final Expr<? extends RatType> leftOp = ExprUtils.cast(expr.getLeftOp().accept(this, param), RatType.class);
		final Expr<? extends RatType> rightOp = ExprUtils.cast(expr.getRightOp().accept(this, param), RatType.class);

		if (leftOp instanceof ConstRefExpr && rightOp instanceof ConstRefExpr) {
			if (leftOp.equals(rightOp))
				return True();
		}

		if ((leftOp instanceof RatLitExpr || leftOp instanceof IntLitExpr) && (rightOp instanceof RatLitExpr || rightOp instanceof IntLitExpr)) {
			final long leftNum = num(leftOp);
			final long leftDenom = denom(leftOp);
			final long rightNum = num(rightOp);
			final long rightDenom = denom(rightOp);

			assert (leftDenom > 0);
			assert (rightDenom > 0);

			return Bool(leftNum * rightDenom <= rightNum * leftDenom);
		}

		return expr.withOps(leftOp, rightOp);
	}

	@Override
	public Expr<? extends BoolType> visit(final LtExpr expr, final Assignment param) {
		final Expr<? extends RatType> leftOp = ExprUtils.cast(expr.getLeftOp().accept(this, param), RatType.class);
		final Expr<? extends RatType> rightOp = ExprUtils.cast(expr.getRightOp().accept(this, param), RatType.class);

		if (leftOp instanceof ConstRefExpr && rightOp instanceof ConstRefExpr) {
			if (leftOp.equals(rightOp))
				return False();
		}

		if ((leftOp instanceof RatLitExpr || leftOp instanceof IntLitExpr) && (rightOp instanceof RatLitExpr || rightOp instanceof IntLitExpr)) {
			final long leftNum = num(leftOp);
			final long leftDenom = denom(leftOp);
			final long rightNum = num(rightOp);
			final long rightDenom = denom(rightOp);

			assert (leftDenom > 0);
			assert (rightDenom > 0);

			return Bool(leftNum * rightDenom < rightNum * leftDenom);
		}

		return expr.withOps(leftOp, rightOp);
	}

	@Override
	public Expr<? extends IntType> visit(final IntLitExpr expr, final Assignment param) {
		return expr;
	}

	@Override
	public Expr<? extends IntType> visit(final IntDivExpr expr, final Assignment param) {
		final Expr<? extends IntType> leftOp = ExprUtils.cast(expr.getLeftOp().accept(this, param), IntType.class);
		final Expr<? extends IntType> rightOp = ExprUtils.cast(expr.getRightOp().accept(this, param), IntType.class);

		if (rightOp instanceof IntLitExpr && ((IntLitExpr) rightOp).getValue() == 0) {
			throw new ArithmeticException("Division by zero");
		}

		if (leftOp instanceof IntLitExpr && rightOp instanceof IntLitExpr) {
			final long leftInt = ((IntLitExpr) leftOp).getValue();
			final long rightInt = ((IntLitExpr) rightOp).getValue();
			return Int(leftInt / rightInt);
		}

		return expr.withOps(leftOp, rightOp);
	}

	@Override
	public Expr<? extends IntType> visit(final RemExpr expr, final Assignment param) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
		/*
		 * final Expr<? extends IntType> leftOp =
		 * ExprUtils.cast(expr.getLeftOp().accept(this, param), IntType.class);
		 * final Expr<? extends IntType> rightOp =
		 * ExprUtils.cast(expr.getRightOp().accept(this, param), IntType.class);
		 *
		 * if (leftOp instanceof IntLitExpr && rightOp instanceof IntLitExpr) {
		 * final long leftInt = ((IntLitExpr) leftOp).getValue(); final long
		 * rightInt = ((IntLitExpr) rightOp).getValue(); return Int(leftInt %
		 * rightInt); }
		 *
		 * return Rem(leftOp, rightOp);
		 */
	}

	@Override
	public Expr<? extends IntType> visit(final ModExpr expr, final Assignment param) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
		/*
		 * final Expr<? extends IntType> leftOp =
		 * ExprUtils.cast(expr.getLeftOp().accept(this, param), IntType.class);
		 * final Expr<? extends IntType> rightOp =
		 * ExprUtils.cast(expr.getRightOp().accept(this, param), IntType.class);
		 *
		 * if (leftOp instanceof IntLitExpr && rightOp instanceof IntLitExpr) {
		 * final long leftInt = ((IntLitExpr) leftOp).getValue(); final long
		 * rightInt = ((IntLitExpr) rightOp).getValue(); return
		 * Int(Math.floorMod(leftInt, rightInt)); }
		 *
		 * return IntDiv(leftOp, rightOp);
		 */
	}

	@Override
	public Expr<? extends RatType> visit(final RatLitExpr expr, final Assignment param) {

		long denom = expr.getDenom();
		long num = expr.getNum();

		if (denom < 0) {
			denom *= -1;
			num *= -1;
		} else if (denom == 0) {
			throw new ArithmeticException("Division by zero");
		}

		if (num == 0) {
			return Int(0);
		} else if (num % denom == 0) {
			return Int(num / denom);
		}

		if (num == expr.getNum() && denom == expr.getDenom()) {
			return expr;
		} else {
			return Rat(num, denom);
		}
	}

	@Override
	public Expr<? extends RatType> visit(final RatDivExpr expr, final Assignment param) {
		final Expr<? extends RatType> leftOp = ExprUtils.cast(expr.getLeftOp().accept(this, param), RatType.class);
		final Expr<? extends RatType> rightOp = ExprUtils.cast(expr.getRightOp().accept(this, param), RatType.class);

		if (rightOp instanceof IntLitExpr && ((IntLitExpr) rightOp).getValue() == 0) {
			throw new ArithmeticException("Division by zero");
		}

		if ((leftOp instanceof RatLitExpr || leftOp instanceof IntLitExpr) && (rightOp instanceof RatLitExpr || rightOp instanceof IntLitExpr)) {
			final long leftNum = num(leftOp);
			final long leftDenom = denom(leftOp);
			final long rightNum = num(rightOp);
			final long rightDenom = denom(rightOp);

			return ExprUtils.cast(Rat(leftNum * rightDenom, leftDenom * rightNum).accept(this, param), RatType.class);
		}

		return expr.withOps(leftOp, rightOp);
	}

	@Override
	public <ExprType extends ClosedUnderNeg> Expr<? extends ClosedUnderNeg> visit(final NegExpr<ExprType> expr, final Assignment param) {
		final Expr<? extends ClosedUnderNeg> op = ExprUtils.cast(expr.getOp().accept(this, param), ClosedUnderNeg.class);

		if (op instanceof IntLitExpr) {
			return Int(((IntLitExpr) op).getValue() * -1);
		} else if (op instanceof RatLitExpr) {
			final RatLitExpr opLit = ((RatLitExpr) op);
			return Rat(opLit.getNum() * -1, opLit.getDenom());
		} else if (op instanceof NegExpr) {
			return ((NegExpr<? extends ClosedUnderNeg>) op).getOp();
		}

		if (op == expr.getOp()) {
			return expr;
		} else {
			return Neg(op);
		}
	}

	@Override
	public <ExprType extends ClosedUnderSub> Expr<? extends ClosedUnderSub> visit(final SubExpr<ExprType> expr, final Assignment param) {
		final Expr<? extends ClosedUnderSub> leftOp = ExprUtils.cast(expr.getLeftOp().accept(this, param), ClosedUnderSub.class);
		final Expr<? extends ClosedUnderSub> rightOp = ExprUtils.cast(expr.getRightOp().accept(this, param), ClosedUnderSub.class);

		if (leftOp instanceof ConstRefExpr && rightOp instanceof ConstRefExpr) {
			if (leftOp.equals(rightOp))
				return Int(0);
		}

		if ((leftOp instanceof RatLitExpr || leftOp instanceof IntLitExpr) && (rightOp instanceof RatLitExpr || rightOp instanceof IntLitExpr)) {
			final long leftNum = num(leftOp);
			final long leftDenom = denom(leftOp);
			final long rightNum = num(rightOp);
			final long rightDenom = denom(rightOp);

			return ExprUtils.cast(Rat(leftNum * rightDenom - rightNum * leftDenom, leftDenom * rightDenom).accept(this, param), RatType.class);
		}

		if (leftOp == expr.getLeftOp() && rightOp == expr.getRightOp()) {
			return expr;
		} else {
			return Sub(leftOp, rightOp);
		}
	}

	@Override
	public <ExprType extends ClosedUnderAdd> Expr<? extends ClosedUnderAdd> visit(final AddExpr<ExprType> expr, final Assignment param) {
		final List<Expr<? extends ClosedUnderAdd>> ops = new ArrayList<>();
		long num = 0;
		long denom = 1;

		for (final Expr<? extends ExprType> op : expr.getOps()) {
			final Expr<? extends ClosedUnderAdd> opVisited = ExprUtils.cast(op.accept(this, param), ClosedUnderAdd.class);
			if (opVisited instanceof IntLitExpr) {
				num += denom * ((IntLitExpr) opVisited).getValue();
			} else if (opVisited instanceof RatLitExpr) {
				final RatLitExpr opRatLit = (RatLitExpr) opVisited;
				num = num * opRatLit.getDenom() + denom * opRatLit.getNum();
				denom *= opRatLit.getDenom();
			} else if (opVisited instanceof AddExpr<?>) {
				ops.addAll(((AddExpr<?>) opVisited).getOps());
			} else {
				ops.add(opVisited);
			}
		}

		final Expr<? extends ClosedUnderAdd> sum = ExprUtils.cast(Rat(num, denom).accept(this, param), ClosedUnderAdd.class);

		if (!sum.equals(Int(0))) {
			ops.add(sum);
		}

		if (ops.size() == 0) {
			return Int(0);
		} else if (ops.size() == 1) {
			return ops.iterator().next();
		}

		return Add(ops);
	}

	@Override
	public <ExprType extends ClosedUnderMul> Expr<? extends ClosedUnderMul> visit(final MulExpr<ExprType> expr, final Assignment param) {
		final List<Expr<? extends ClosedUnderMul>> ops = new ArrayList<>();
		long num = 1;
		long denom = 1;

		for (final Expr<? extends ExprType> op : expr.getOps()) {
			final Expr<? extends ClosedUnderMul> opVisited = ExprUtils.cast(op.accept(this, param), ClosedUnderMul.class);
			if (opVisited instanceof IntLitExpr) {
				num *= ((IntLitExpr) opVisited).getValue();
				if (num == 0)
					return Int(0);
			} else if (opVisited instanceof RatLitExpr) {
				final RatLitExpr opRatLit = (RatLitExpr) opVisited;
				num *= opRatLit.getNum();
				denom *= opRatLit.getDenom();
			} else if (opVisited instanceof MulExpr<?>) {
				ops.addAll(((MulExpr<?>) opVisited).getOps());
			} else {
				ops.add(opVisited);
			}
		}

		final Expr<? extends ClosedUnderMul> prod = ExprUtils.cast(Rat(num, denom).accept(this, param), ClosedUnderMul.class);

		if (!prod.equals(Int(1))) {
			ops.add(prod);
		}

		if (ops.size() == 0) {
			return Int(1);
		} else if (ops.size() == 1) {
			return ops.iterator().next();
		}

		return Mul(ops);
	}

	@Override
	public <IndexType extends Type, ElemType extends Type> Expr<? extends Type> visit(final ArrayReadExpr<IndexType, ElemType> expr, final Assignment param) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public <IndexType extends Type, ElemType extends Type> Expr<? extends Type> visit(final ArrayWriteExpr<IndexType, ElemType> expr, final Assignment param) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public <ParamType extends Type, ResultType extends Type> Expr<? extends Type> visit(final FuncLitExpr<ParamType, ResultType> expr, final Assignment param) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public <ParamType extends Type, ResultType extends Type> Expr<? extends Type> visit(final FuncAppExpr<ParamType, ResultType> expr, final Assignment param) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public <ExprType extends Type> Expr<? extends ExprType> visit(final IteExpr<ExprType> expr, final Assignment param) {
		final Expr<? extends BoolType> cond = ExprUtils.cast(expr.getCond().accept(this, param), BoolType.class);
		@SuppressWarnings("unchecked")
		final Expr<? extends ExprType> then = (Expr<? extends ExprType>) expr.getThen().accept(this, param);
		@SuppressWarnings("unchecked")
		final Expr<? extends ExprType> elze = (Expr<? extends ExprType>) expr.getElse().accept(this, param);

		if (cond instanceof TrueExpr) {
			return then;
		} else if (cond instanceof FalseExpr) {
			return elze;
		}

		if (cond == expr.getCond() && then == expr.getThen() && elze == expr.getElse()) {
			return expr;
		} else {
			return Ite(cond, then, elze);
		}
	}

	// TODO: refactor these helper methods as soon as IntLit and RatLit has some
	// common interface
	private long num(final Expr<? extends Type> expr) {
		assert (expr instanceof IntLitExpr || expr instanceof RatLitExpr);
		if (expr instanceof IntLitExpr)
			return ((IntLitExpr) expr).getValue();
		else
			return ((RatLitExpr) expr).getNum();
	}

	// TODO: refactor these helper methods as soon as IntLit and RatLit has some
	// common interface
	private long denom(final Expr<? extends Type> expr) {
		assert (expr instanceof IntLitExpr || expr instanceof RatLitExpr);
		if (expr instanceof IntLitExpr)
			return 1;
		else
			return ((RatLitExpr) expr).getDenom();
	}
}
