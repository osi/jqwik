package net.jqwik.engine.properties.shrinking;

import java.math.*;
import java.util.*;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import static org.assertj.core.api.Assertions.*;

import static net.jqwik.api.ShrinkingTestHelper.*;

class ShrinkTowardsTests {

	@Property(tries = 50)
	void bytes(@ForAll Random random, @ForAll byte target) {
		Arbitrary<Byte> bytes = Arbitraries.bytes().shrinkTowards(target);
		byte shrunkValue = shrinkToMinimal(bytes, random);
		assertThat(shrunkValue).isEqualTo(target);
	}

	@Property(tries = 50)
	void shorts(@ForAll Random random, @ForAll short target) {
		Arbitrary<Short> shorts = Arbitraries.shorts().shrinkTowards(target);
		short shrunkValue = shrinkToMinimal(shorts, random);
		assertThat(shrunkValue).isEqualTo(target);
	}

	@Property(tries = 50)
	void integers(@ForAll Random random, @ForAll int target) {
		Arbitrary<Integer> integers = Arbitraries.integers().shrinkTowards(target);
		int shrunkValue = shrinkToMinimal(integers, random);
		assertThat(shrunkValue).isEqualTo(target);
	}

	@Property(tries = 50)
	void longs(@ForAll Random random, @ForAll long target) {
		Arbitrary<Long> longs = Arbitraries.longs().shrinkTowards(target);
		long shrunkValue = shrinkToMinimal(longs, random);
		assertThat(shrunkValue).isEqualTo(target);
	}

	@Property(tries = 50)
	void bigIntegers(@ForAll Random random, @ForAll BigInteger target) {
		Arbitrary<BigInteger> bigs = Arbitraries.bigIntegers().shrinkTowards(target);
		BigInteger shrunkValue = shrinkToMinimal(bigs, random);
		assertThat(shrunkValue).isEqualTo(target);
	}

	@Property(tries = 10)
	void floats(@ForAll Random random, @ForAll @FloatRange(min = -10000, max = 10000) @Scale(0) float target) {
		Arbitrary<Float> floats = Arbitraries.floats().shrinkTowards(target);
		float shrunkValue = shrinkToMinimal(floats, random);
		assertThat(shrunkValue).isEqualTo(target);
	}

	@Property(tries = 10)
	void doubles(@ForAll Random random, @ForAll @DoubleRange(min = -10000, max = 10000) @Scale(0) double target) {
		Arbitrary<Double> doubles = Arbitraries.doubles().shrinkTowards(target);
		double shrunkValue = shrinkToMinimal(doubles, random);
		assertThat(shrunkValue).isEqualTo(target);
	}

	@Property(tries = 10)
	void bigDecimals(@ForAll Random random, @ForAll @BigRange(min = "-1000", max = "1000") @Scale(0) BigDecimal target) {
		Arbitrary<BigDecimal> bigDecimals = Arbitraries.bigDecimals().shrinkTowards(target);
		BigDecimal shrunkValue = shrinkToMinimal(bigDecimals, random);
		assertThat(shrunkValue).isEqualByComparingTo(target);
	}

}
