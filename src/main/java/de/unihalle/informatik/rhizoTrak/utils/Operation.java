package de.unihalle.informatik.rhizoTrak.utils;

public interface Operation<I,O>
{
	public I apply(O o);
}