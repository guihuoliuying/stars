package test;

import com.stars.core.expr.ExprConfig;
import com.stars.core.expr.ExprLexer;
import com.stars.core.expr.ExprParser;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.experimental.theories.Theory;
import org.junit.experimental.theories.suppliers.TestedOn;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;
import static test.TestExprArithmetic.LessThan.lessThan;

public class TestExprArithmetic {

    @Test
    public void testDigits0() {
        assertEquals(0L, eval("0"));
    }

    @Test
    public void testDigits1() {
        assertEquals(1L, eval("1"));
    }

    @Test
    public void testDigitsMinus1() {
        assertEquals(-1L, eval("0 - 1"));
    }

    @Test
    public void testDigits9223372036854775807() {
        assertEquals(9223372036854775807L, eval("9223372036854775807"));
    }

    @Test
    public void testDigits9223372036854775808() {
        assertEquals(-9223372036854775808L, eval("9223372036854775807 + 1"));
    }

    @Test
    public void testDigitsMinus9223372036854775808() {
        assertEquals(0x8000_0000_0000_0000L, eval("0 - 9223372036854775807 - 1"));
        assertEquals(-9223372036854775808L, eval("0 - 9223372036854775807 - 1"));
    }

    @Test
    public void testDigitsMinus9223372036854775809() {
        assertEquals(0x7FFF_FFFF_FFFF_FFFFL, eval("0 - 9223372036854775807 - 1 - 1"));
        assertEquals(9223372036854775807L, eval("0 - 9223372036854775807 - 1 - 1"));
    }

    @Test
    public void testAdd0() {
        assertEquals(1L, eval("1 + 0"));
    }

    @Test
    public void testAdd1() {
        assertEquals(2L, eval("1 + 1"));
    }

    @Theory
    public void testAdd(
            @TestedOn(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}) int l,
            @TestedOn(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}) int r) {

        assumeThat((long) l, lessThan(Long.MAX_VALUE));
        assumeThat((long) r, lessThan(Long.MAX_VALUE));

        assumeThat(eval(l + " + " + r), is((long) l + (long) r));
    }

    @Test
    public void testSub0() {
        assertEquals(1L, eval("1 - 0"));
    }

    @Test
    public void testSub1() {
        assertEquals(0L, eval("1 - 1"));
    }

    @Test
    public void testSub2() {
        assertEquals(-1L, eval("1 - 2"));
    }

    @Test
    public void testDiv0() {
        try {
            eval("1 / 0");
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            assertEquals("/ by zero", e.getMessage());
        }
    }

    private long eval(String expr) {
        return (long) new ExprParser(new ExprLexer(expr), new ExprConfig()).parse().eval();
    }

    protected static class LessThan extends BaseMatcher<Long> {

        private Long value;

        public LessThan(Long value) {
            this.value = value;
        }

        @Override
        public boolean matches(Object item) {
            return (Long) item < value;
        }

        @Override
        public void describeTo(Description description) {

        }

        public static Matcher<Long> lessThan(Long value) {
            return new LessThan(value);
        }
    }

}
