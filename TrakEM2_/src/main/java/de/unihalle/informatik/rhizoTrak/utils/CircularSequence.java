package de.unihalle.informatik.rhizoTrak.utils;

public final class CircularSequence
{
	int i;
	final private int size;

	/** A sequence of integers from 0 to {@code size}
	 * that cycle back to zero when reaching the end;
	 * the starting point is the last point in the sequence,
	 * so that a call to {@link #next()} delivers the first value, 0.
	 * 
	 * @param size The length of the range to cycle over.
	 */
	public CircularSequence(final int size) {
		this.size = size;
		this.i = size -1;
	}
	final public int next() {
		++i;
		i = i % size;
		return i;
	}
	final public int previous() {
		--i;
		i = i % size;
		return i;
	}
	/** Will wrap around if {@code k<0} or {@code k>size}. */
	final public int setPosition(final int k) {
		i = k;
		if (i < 0) i = size - ((-i) % size);
		else i = i % size;
		return i;
	}
	/** Will wrap around. */
	final public int move(final int inc) {
		return setPosition(i + inc);
	}
}
