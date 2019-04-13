package net.jqwik.engine.properties.arbitraries;

import java.util.*;
import java.util.stream.*;

import net.jqwik.api.*;
import net.jqwik.api.arbitraries.*;
import net.jqwik.engine.properties.arbitraries.exhaustive.*;
import net.jqwik.engine.properties.arbitraries.randomized.*;

public class DefaultStringArbitrary extends AbstractArbitraryBase implements StringArbitrary {

	private CharacterArbitrary characterArbitrary = new DefaultCharacterArbitrary();

	private int minLength = 0;
	private int maxLength = RandomGenerators.DEFAULT_COLLECTION_SIZE;

	@Override
	public RandomGenerator<String> generator(int genSize) {
		final int cutoffLength = RandomGenerators.defaultCutoffSize(minLength, maxLength, genSize);
		List<Shrinkable<String>> samples = Arrays.stream(new String[] { "" })
												 .filter(s -> s.length() >= minLength && s.length() <= maxLength).map(Shrinkable::unshrinkable)
												 .collect(Collectors.toList());
		return RandomGenerators.strings(randomCharacterGenerator(), minLength, maxLength, cutoffLength).withEdgeCases(genSize, samples);
	}

	@Override
	public Optional<ExhaustiveGenerator<String>> exhaustive() {
		return ExhaustiveGenerators.strings(effectiveCharacterArbitrary(), minLength, maxLength);
	}

	@Override
	public StringArbitrary ofMinLength(int minLength) {
		DefaultStringArbitrary clone = typedClone();
		clone.minLength = minLength;
		return clone;
	}

	@Override
	public StringArbitrary ofMaxLength(int maxLength) {
		DefaultStringArbitrary clone = typedClone();
		clone.maxLength = maxLength;
		return clone;
	}

	@Override
	public StringArbitrary withChars(char... chars) {
		DefaultStringArbitrary clone = typedClone();
		clone.characterArbitrary = clone.characterArbitrary.with(chars);
		return clone;
	}

	@Override
	public StringArbitrary withCharRange(char from, char to) {
		if (from == 0 && to == 0) {
			return this;
		}
		DefaultStringArbitrary clone = typedClone();
		clone.characterArbitrary = clone.characterArbitrary.range(from, to);
		return clone;
	}

	@Override
	public StringArbitrary ascii() {
		DefaultStringArbitrary clone = typedClone();
		clone.characterArbitrary = characterArbitrary.ascii();
		return clone;
	}

	@Override
	public StringArbitrary alpha() {
		DefaultStringArbitrary clone = typedClone();
		clone.characterArbitrary = clone.characterArbitrary.range('A', 'Z');
		clone.characterArbitrary = clone.characterArbitrary.range('a', 'z');
		return clone;
	}

	@Override
	public StringArbitrary numeric() {
		DefaultStringArbitrary clone = typedClone();
		clone.characterArbitrary = clone.characterArbitrary.range('0', '9');
		return clone;
	}

	/**
	 * Extracted unicodes from java 8 with
	 * <pre>
	 * 	for (char c = Character.MIN_VALUE; c &lt; Character.MAX_VALUE; c++) {
	 * 		if (Character.isWhitespace(c)) {
	 * 			System.out.println( "\\u" + Integer.toHexString(c | 0x10000).substring(1) );
	 * 		}
	 * 	}
	 *  </pre>
	 */
	@Override
	public StringArbitrary whitespace() {
		return this.withChars( //
			'\u0009', //
			'\n', //
			'\u000b', //
			'\u000c', //
			'\r', //
			'\u001c', //
			'\u001d', //
			'\u001e', //
			'\u001f', //
			'\u0020', //
			'\u1680', //
			'\u180e', //
			'\u2000', //
			'\u2001', //
			'\u2002', //
			'\u2003', //
			'\u2004', //
			'\u2005', //
			'\u2006', //
			'\u2008', //
			'\u2009', //
			'\u200a', //
			'\u2028', //
			'\u2029', //
			'\u205f', //
			'\u3000' //
		);
	}

	@Override
	public StringArbitrary all() {
		return this.withCharRange(Character.MIN_VALUE, Character.MAX_VALUE);
	}

	private RandomGenerator<Character> randomCharacterGenerator() {
		return effectiveCharacterArbitrary().generator(1);
	}

	private Arbitrary<Character> effectiveCharacterArbitrary() {
		return characterArbitrary;
	}

}
