package io.github.fike110.jeffect.data;

/**
 * A tuple of two values.
 * 
 * <p>Useful for pairing two related values together, commonly used
 * with {@link io.github.fike110.jeffect.core.Effect#zip}.</p>
 * 
 * <h2>Usage</h2>
 * <pre>{@code
 * Pair<String, Integer> pair = new Pair<>("hello", 42);
 * String first = pair.first();   // "hello"
 * Integer second = pair.second(); // 42
 * }</pre>
 * 
 * @param <A> the type of the first value
 * @param <B> the type of the second value
 */
public final class Pair<A, B> {

    /** The first value */
    private final A first;
    
    /** The second value */
    private final B second;

    /**
     * Creates a Pair with two values.
     * 
     * @param first the first value
     * @param second the second value
     */
    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first value.
     * 
     * @return the first value
     */
    public A first() {
        return first;
    }

    /**
     * Returns the second value.
     * 
     * @return the second value
     */
    public B second() {
        return second;
    }
}