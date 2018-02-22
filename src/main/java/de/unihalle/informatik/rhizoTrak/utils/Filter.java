package de.unihalle.informatik.rhizoTrak.utils;

import java.io.Serializable;

public interface Filter<T> extends Serializable
{
	public boolean accept(T t);
}