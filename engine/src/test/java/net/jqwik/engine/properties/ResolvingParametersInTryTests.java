package net.jqwik.engine.properties;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;
import net.jqwik.api.lifecycle.*;
import net.jqwik.api.lifecycle.PerProperty.*;
import net.jqwik.engine.*;
import net.jqwik.engine.execution.*;
import net.jqwik.engine.support.*;

import static java.util.Arrays.*;
import static org.assertj.core.api.Assertions.*;

class ResolvingParametersInTryTests {

	@Example
	void nothingToResolve() {
		List<MethodParameter> propertyParameters = TestHelper.getParametersFor(TestContainer.class, "nothingToResolve");
		PropertyLifecycleContext propertyLifecycleContext =
			TestHelper.propertyLifecycleContextFor(TestContainer.class, "nothingToResolve", int.class);
		Iterator<List<Shrinkable<Object>>> forAllGenerator = shrinkablesIterator(asList(1), asList(2));
		ResolvingParametersGenerator generator = new ResolvingParametersGenerator(
			propertyParameters,
			forAllGenerator,
			ResolveParameterHook.DO_NOT_RESOLVE
		);

		assertThat(generator.hasNext()).isTrue();
		assertThat(toValues(generator.next(tryLifecycle(propertyLifecycleContext)))).containsExactly(1);
		assertThat(generator.hasNext()).isTrue();
		assertThat(toValues(generator.next(tryLifecycle(propertyLifecycleContext)))).containsExactly(2);
		assertThat(generator.hasNext()).isFalse();
	}

	private TryLifecycleContext tryLifecycle(PropertyLifecycleContext propertyLifecycleContext) {
		return new DefaultTryLifecycleContext(propertyLifecycleContext);
	}

	@Example
	void resolveLastPosition() {
		List<MethodParameter> propertyParameters = TestHelper.getParametersFor(TestContainer.class, "forAllIntAndString");
		PropertyLifecycleContext propertyLifecycleContext =
			TestHelper.propertyLifecycleContextFor(TestContainer.class, "forAllIntAndString", int.class, String.class);
		Iterator<List<Shrinkable<Object>>> forAllGenerator = shrinkablesIterator(asList(1), asList(2));
		ResolveParameterHook stringInjector = (parameterContext) -> {
			assertThat(parameterContext.index()).isEqualTo(1);
			if (parameterContext.typeUsage().isOfType(String.class)) {
				return Optional.of(lifecycleContext -> {
					TryLifecycleContext tryLifecycleContext = (TryLifecycleContext) lifecycleContext;
					assertThat(tryLifecycleContext.propertyContext()).isSameAs(propertyLifecycleContext);
					return "aString";
				});
			}
			return Optional.empty();
		};
		ResolvingParametersGenerator generator = new ResolvingParametersGenerator(
			propertyParameters,
			forAllGenerator,
			stringInjector
		);

		assertThat(generator.hasNext()).isTrue();
		assertThat(toValues(generator.next(tryLifecycle(propertyLifecycleContext)))).containsExactly(1, "aString");
		assertThat(generator.hasNext()).isTrue();
		assertThat(toValues(generator.next(tryLifecycle(propertyLifecycleContext)))).containsExactly(2, "aString");
		assertThat(generator.hasNext()).isFalse();
	}

	@Example
	void failIfParameterCannotBeResolved() {
		List<MethodParameter> propertyParameters = TestHelper.getParametersFor(TestContainer.class, "forAllIntAndString");
		PropertyLifecycleContext propertyLifecycleContext =
			TestHelper.propertyLifecycleContextFor(TestContainer.class, "forAllIntAndString", int.class, String.class);
		Iterator<List<Shrinkable<Object>>> forAllGenerator = shrinkablesIterator(asList(1), asList(2));
		ResolvingParametersGenerator generator = new ResolvingParametersGenerator(
			propertyParameters,
			forAllGenerator,
			ResolveParameterHook.DO_NOT_RESOLVE
		);

		assertThat(generator.hasNext()).isTrue();
		assertThatThrownBy(() -> {
			generator.next(tryLifecycle(propertyLifecycleContext));
		}).isInstanceOf(CannotResolveParameterException.class);
	}

	@Example
	void resolveSeveralPositions() {
		List<MethodParameter> propertyParameters = TestHelper.getParametersFor(TestContainer.class, "stringIntStringInt");
		PropertyLifecycleContext propertyLifecycleContext =
			TestHelper.propertyLifecycleContextFor(
				TestContainer.class,
				"stringIntStringInt",
				String.class,
				int.class,
				String.class,
				int.class
			);
		Iterator<List<Shrinkable<Object>>> forAllGenerator = shrinkablesIterator(asList(1, 2), asList(3, 4));
		ResolveParameterHook stringInjector = (parameterContext) -> {
			assertThat(parameterContext.index()).isIn(0, 2);
			if (parameterContext.typeUsage().isOfType(String.class)) {
				return Optional.of(lifecycleContext -> "aString");
			}
			return Optional.empty();
		};
		ResolvingParametersGenerator generator = new ResolvingParametersGenerator(
			propertyParameters,
			forAllGenerator,
			stringInjector
		);

		assertThat(generator.hasNext()).isTrue();
		assertThat(toValues(generator.next(tryLifecycle(propertyLifecycleContext))))
			.containsExactly("aString", 1, "aString", 2);
		assertThat(generator.hasNext()).isTrue();
		assertThat(toValues(generator.next(tryLifecycle(propertyLifecycleContext))))
			.containsExactly("aString", 3, "aString", 4);
		assertThat(generator.hasNext()).isFalse();
	}

	@Property(tries = 10)
	@AddLifecycleHook(CreateAString.class)
	@PerProperty(Assert1InjectorCalls.class)
	void resolverIsCalledOnce(@ForAll int ignore, String aString) {
		assertThat(aString).isEqualTo("aString");
	}

	class Assert1InjectorCalls implements Lifecycle {
		@Override
		public void onSuccess() {
			assertThat(CreateAString.countInjectorCalls.get()).isEqualTo(1);
		}
	}

	@Property(tries = 10)
	@AddLifecycleHook(CreateAString.class)
	@PerProperty(Assert10SupplierCalls.class)
	void supplierIsCalledOncePerTry(@ForAll int ignore, String aString) {
		assertThat(aString).isEqualTo("aString");
	}

	class Assert10SupplierCalls implements Lifecycle {
		@Override
		public void onSuccess() {
			assertThat(CreateAString.countSupplierCalls.get()).isEqualTo(10);
		}
	}

	@Property(tries = 10, afterFailure = AfterFailureMode.RANDOM_SEED)
	@AddLifecycleHook(CreateAString.class)
	@ExpectFailure(checkResult = ShrinkTo7.class)
	void shouldShrinkTo7(@ForAll @IntRange(min = 0) int anInt, String aString) {
		assertThat(aString).isEqualTo("aString");
		assertThat(anInt).isLessThan(7);
	}

	private class ShrinkTo7 extends ShrinkToChecker {
		@Override
		public Iterable<?> shrunkValues() {
			return Arrays.asList(7, "aString");
		}
	}

	@Example
	@AddLifecycleHook(CreateAlwaysAString.class)
	@ExpectFailure(checkResult = HasCannotResolveException.class)
	void shouldFailWithWrongParameterValueType(List<Integer> aList) {
	}

	@Example
	@AddLifecycleHook(CreateAString.class)
	@AddLifecycleHook(CreateAlwaysAString.class)
	@ExpectFailure(checkResult = HasCannotResolveException.class)
	void shouldFailWithDuplicateResolution(String aString) {
	}

	private class HasCannotResolveException implements Consumer<PropertyExecutionResult> {
		@Override
		public void accept(PropertyExecutionResult propertyExecutionResult) {
			assertThat(propertyExecutionResult.throwable().isPresent());
			propertyExecutionResult.throwable().ifPresent(throwable -> {
				assertThat(throwable).isInstanceOf(CannotResolveParameterException.class);
			});
		}
	}

	@Property(tries = 5)
	@PerProperty(Insert42Lifecycle.class)
	void check42(int shouldBe42) {
		assertThat(shouldBe42).isEqualTo(42);
	}

	private class Insert42Lifecycle implements Lifecycle {

		Store<LifecycleContext> lifecycleStore = Store.create(this, Lifespan.PROPERTY, () -> null);

		@Override
		public Optional<ResolveParameterHook.ParameterSupplier> resolve(ParameterResolutionContext parameterContext) {
			return Optional.of(lifecycleContext -> {
				assertThat(lifecycleContext).isInstanceOf(TryLifecycleContext.class);
				assertThat(lifecycleStore.get()).isNotSameAs(lifecycleContext);
				lifecycleStore.update(ignore -> lifecycleContext);
				return 42;
			});
		}
	}

	private List<Object> toValues(List<Shrinkable<Object>> shrinkables) {
		return shrinkables.stream().map(Shrinkable::value).collect(Collectors.toList());
	}

	@SafeVarargs
	private final Iterator<List<Shrinkable<Object>>> shrinkablesIterator(List<Object>... lists) {
		Iterator<List<Object>> valuesIterator = Arrays.stream(lists).iterator();

		return new Iterator<List<Shrinkable<Object>>>() {
			@Override
			public boolean hasNext() {
				return valuesIterator.hasNext();
			}

			@Override
			public List<Shrinkable<Object>> next() {
				List<Object> values = valuesIterator.next();
				return values.stream().map(Shrinkable::unshrinkable).collect(Collectors.toList());
			}
		};
	}

	private static class TestContainer {
		@Property
		void nothingToResolve(@ForAll int anInt) {}

		@Property
		void forAllIntAndString(@ForAll int anInt, String aString) {}

		@Property
		void stringIntStringInt(String s1, @ForAll int i1, String s2, @ForAll int i2) {}
	}

}

class CreateAlwaysAString implements ResolveParameterHook {
	@Override
	public Optional<ParameterSupplier> resolve(
		ParameterResolutionContext parameterContext
	) {
		return Optional.of(lifecycleContext -> "a string");
	}
}

class CreateAString implements ResolveParameterHook {

	static Store<Integer> countInjectorCalls;

	static Store<Integer> countSupplierCalls;

	@Override
	public Optional<ParameterSupplier> resolve(ParameterResolutionContext parameterContext) {
		countInjectorCalls = Store.getOrCreate("injectorCalls", Lifespan.PROPERTY, () -> 0);
		countSupplierCalls = Store.getOrCreate("supplierCalls", Lifespan.PROPERTY, () -> 0);

		countInjectorCalls.update(i -> i + 1);

		if (parameterContext.typeUsage().isOfType(String.class)) {
			return Optional.of(lifecycleContext -> {
				assertThat(lifecycleContext).isInstanceOf(TryLifecycleContext.class);
				assertThat(((TryLifecycleContext) lifecycleContext).propertyContext().containerClass())
					.isEqualTo(ResolvingParametersInTryTests.class);
				countSupplierCalls.update(i -> i + 1);
				return "aString";
			});
		}
		return Optional.empty();
	}
}