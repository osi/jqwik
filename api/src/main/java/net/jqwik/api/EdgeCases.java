package net.jqwik.api;

import java.math.*;
import java.util.*;
import java.util.function.*;

import org.apiguardian.api.*;

import static org.apiguardian.api.API.Status.*;

@API(status = EXPERIMENTAL, since = "1.3.0")
public interface EdgeCases<T> extends Iterable<Shrinkable<T>> {

	@API(status = INTERNAL)
	abstract class EdgeCasesFacade {
		private static final EdgeCases.EdgeCasesFacade implementation;

		static {
			implementation = FacadeLoader.load(EdgeCases.EdgeCasesFacade.class);
		}

		public abstract <T> EdgeCases<T> fromSuppliers(List<Supplier<Shrinkable<T>>> suppliers);
	}

	@API(status = EXPERIMENTAL, since = "1.3.9")
	interface Config<T> {

		static <T> Consumer<Config<T>> noConfig() {
			return config -> {};
		};

		/**
		 * Don't use any of the default edge cases
		 *
		 * @return same configuration instance
		 */
		Config<T> none();

		/**
		 * Only include edge cases for which {@linkplain #filter(Predicate)}  returns true
		 *
		 * @param filter A predicate
		 *
		 * @return same configuration instance
		 */
		Config<T> filter(Predicate<T> filter);

		/**
		 * Add an unshrinkable additional value as edgeCase.
		 * This may even be outside the value range generated by this arbitrary.
		 *
		 * @param edgeCase The edge case to add to default edge cases.
		 *
		 * @return same configuration instance
		 */
		Config<T> add(T edgeCase);

		/**
		 * Include the values given if they are in the generated edge cases anyway
		 *
		 * @param includedValues The values to be included
		 *
		 * @return same configuration instance
		 */
		@SuppressWarnings("unchecked")
		Config<T> includeOnly(T ... includedValues);
	}

	List<Supplier<Shrinkable<T>>> suppliers();

	default int size() {
		return suppliers().size();
	}

	default boolean isEmpty() {
		return size() == 0;
	}

	default Iterator<Shrinkable<T>> iterator() {
		return suppliers().stream().map(Supplier::get).iterator();
	}

	@API(status = INTERNAL)
	static <T> EdgeCases<T> fromSuppliers(List<Supplier<Shrinkable<T>>> suppliers) {
		return EdgeCasesFacade.implementation.fromSuppliers(suppliers);
	}

	@API(status = INTERNAL)
	static <T> EdgeCases<T> none() {
		return fromSuppliers(Collections.emptyList());
	}

	@API(status = INTERNAL)
	static <T> EdgeCases<T> fromSupplier(Supplier<Shrinkable<T>> supplier) {
		return fromSuppliers(Collections.singletonList(supplier));
	}

}
