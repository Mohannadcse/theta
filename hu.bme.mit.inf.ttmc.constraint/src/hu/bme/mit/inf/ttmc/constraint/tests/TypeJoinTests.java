package hu.bme.mit.inf.ttmc.constraint.tests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import hu.bme.mit.inf.ttmc.constraint.ConstraintManager;
import hu.bme.mit.inf.ttmc.constraint.ConstraintManagerImpl;
import hu.bme.mit.inf.ttmc.constraint.factory.TypeFactory;
import hu.bme.mit.inf.ttmc.constraint.type.BoolType;
import hu.bme.mit.inf.ttmc.constraint.type.FuncType;
import hu.bme.mit.inf.ttmc.constraint.type.IntType;
import hu.bme.mit.inf.ttmc.constraint.type.RatType;
import hu.bme.mit.inf.ttmc.constraint.type.Type;

@RunWith(Parameterized.class)
public class TypeJoinTests {

	private static final Type TOP;
	private static final BoolType BOOL;
	private static final IntType INT;
	private static final RatType RAT;
	private static final FuncType<IntType, RatType> INT_TO_RAT;
	private static final FuncType<IntType, IntType> INT_TO_INT;
	private static final FuncType<RatType, RatType> RAT_TO_RAT;
	private static final FuncType<RatType, IntType> RAT_TO_INT;

	@Parameter(value = 0)
	public Type type1;

	@Parameter(value = 1)
	public Type type2;

	@Parameter(value = 2)
	public Type join;

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {

				{ BOOL, BOOL, BOOL },

				{ BOOL, INT, TOP },

				{ BOOL, RAT, TOP },

				{ BOOL, INT_TO_RAT, TOP },

				{ INT, INT, INT },

				{ INT, RAT, RAT },

				{ INT, INT_TO_RAT, TOP },

				{ RAT, RAT, RAT },

				{ RAT, INT_TO_RAT, TOP },

				{ RAT_TO_INT, RAT_TO_INT, RAT_TO_INT },

				{ RAT_TO_INT, INT_TO_INT, INT_TO_INT },

				{ RAT_TO_INT, RAT_TO_RAT, RAT_TO_RAT },

				{ RAT_TO_INT, INT_TO_RAT, INT_TO_RAT },

				{ INT_TO_INT, INT_TO_INT, INT_TO_INT },

				{ INT_TO_INT, RAT_TO_RAT, INT_TO_RAT },

				{ INT_TO_INT, INT_TO_RAT, INT_TO_RAT },

				{ RAT_TO_RAT, RAT_TO_RAT, RAT_TO_RAT },

				{ RAT_TO_RAT, INT_TO_RAT, INT_TO_RAT },

				{ INT_TO_RAT, INT_TO_RAT, INT_TO_RAT }

		});
	}

	static {
		final ConstraintManager manager = new ConstraintManagerImpl();
		final TypeFactory tf = manager.getTypeFactory();

		TOP = null;
		BOOL = tf.Bool();
		INT = tf.Int();
		RAT = tf.Rat();
		INT_TO_RAT = tf.Func(INT, RAT);
		INT_TO_INT = tf.Func(INT, INT);
		RAT_TO_RAT = tf.Func(RAT, RAT);
		RAT_TO_INT = tf.Func(RAT, INT);
	}

	@Test
	public void testJoin() {
		final Optional<Type> expected = Optional.ofNullable(join);
		assertEquals(type1.join(type2), expected);
		assertEquals(type2.join(type1), expected);
	}

}