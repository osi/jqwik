package net.jqwik.properties.arbitraries;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;

import java.util.*;
import java.util.stream.*;

abstract class DefaultCollectionArbitrary<T, U> extends AbstractArbitraryBase implements SizableArbitrary<U> {

	protected final Arbitrary<T> elementArbitrary;
	protected int minSize = 0;
	protected int maxSize = 0;

	protected DefaultCollectionArbitrary(Class<?> collectionClass, Arbitrary<T> elementArbitrary) {
		this.elementArbitrary = elementArbitrary;
	}

	protected RandomGenerator<List<T>> listGenerator(int tries) {
		int effectiveMaxSize = effectiveMaxSize(tries);
		return createListGenerator(elementArbitrary, tries, effectiveMaxSize);
	}

	protected int effectiveMaxSize(int tries) {
		int effectiveMaxSize = maxSize;
		if (effectiveMaxSize <= 0)
			effectiveMaxSize = Arbitrary.defaultMaxCollectionSizeFromTries(tries);
		return effectiveMaxSize;
	}

	private RandomGenerator<List<T>> createListGenerator(Arbitrary<T> elementArbitrary, int tries, int effectiveMaxSize) {
		RandomGenerator<T> elementGenerator = elementGenerator(elementArbitrary, tries);
		List<Shrinkable<List<T>>> samples = samplesList(new ArrayList<>());
		return RandomGenerators.list(elementGenerator, minSize, effectiveMaxSize).withShrinkableSamples(samples);
	}

	protected <C extends Collection> List<Shrinkable<C>> samplesList(C sample) {
		return Stream.of(sample).filter(l -> l.size() >= minSize).filter(l -> maxSize == 0 || l.size() <= maxSize)
				.map(Shrinkable::unshrinkable).collect(Collectors.toList());
	}

	protected RandomGenerator<T> elementGenerator(Arbitrary<T> elementArbitrary, int tries) {
		return elementArbitrary.generator(tries);
	}

	@Override
	public SizableArbitrary<U> ofMinSize(int minSize) {
		DefaultCollectionArbitrary<T, U> clone = typedClone();
		clone.minSize = minSize;
		return clone;
	}

	@Override
	public SizableArbitrary<U> ofMaxSize(int maxSize) {
		DefaultCollectionArbitrary<T, U> clone = typedClone();
		clone.maxSize = maxSize;
		return clone;
	}
}
