/* 
 * This file is part of the rhizoTrak project.
 * 
 * Note that rhizoTrak extends TrakEM2, hence, its code base substantially 
 * relies on the source code of the TrakEM2 project and the corresponding Fiji 
 * plugin, initiated by A. Cardona in 2005. Large portions of rhizoTrak's code 
 * are directly derived/copied from the source code of TrakEM2.
 * 
 * For more information on TrakEM2 please visit its websites:
 * 
 *  https://imagej.net/TrakEM2
 * 
 *  https://github.com/trakem2/TrakEM2/wiki
 * 
 * Fore more information on rhizoTrak, visit
 *
 *  https://github.com/prbio-hub/rhizoTrak/wiki
 *
 * Both projects, TrakEM2 and rhizoTrak, are released under GPL. 
 * Please find below first the copyright notice of rhizoTrak, and further on
 * (in case that this file was part of the original TrakEM2 source code base
 * and contained a TrakEM2 file header) the original file header with the 
 * TrakEM2 license note.
 */

/*
 * Copyright (C) 2018 - @YEAR@ by the rhizoTrak development team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Fore more information on rhizoTrak, visit
 *
 *    https://github.com/prbio-hub/rhizoTrak/wiki
 *
 */

/* === original file header below (if any) === */

package de.unihalle.informatik.rhizoTrak.parallel;

import java.util.concurrent.ExecutorService;

/**
 * Allow the source ExecutorServices in TrakEM2 to be configured.
 */
public abstract class ExecutorProvider
{

    private static ExecutorProvider provider = new DefaultExecutorProvider();

    /**
     * Returns an ExecutorService for Callables that use nThreads number of threads.
     * @param nThreads the number of Threads used by a given Callable.
     * @return an ExecutorService that will execute as many Callables as possible for the given
     * number of Threads-per-Callable. For instance, on a machine with 4 cpus (as returned by
     * Runtime.getRuntime().availableProcessors() ), calling getExecutorService(2) will return
     * an ExecutorService that will run 2 ( 4 / 2 ) Callables at a time.
     */
    public static ExecutorService getExecutorService(final int nThreads)
    {
        return provider.getService(nThreads);
    }

    /**
     * Returns an ExecutorService for Callables that use a given fraction of computer resources.
     * @param fractionThreads the fraction of resources that a submitted Callable is expected to
     *                        need.
     * @return an ExecutorService that will execute as many Callables as possible for the given
     * resource fraction. For instance, getExecutorService(1.0f) will return an ExecutorService
     * that will run only one Callable at a time.
     */

    public static ExecutorService getExecutorService(final float fractionThreads)
    {
        return provider.getService(fractionThreads);
    }

    public static void setProvider(final ExecutorProvider ep)
    {
        provider = ep;
    }

    public static ExecutorProvider getProvider()
    {
        return provider;
    }

    public abstract ExecutorService getService(int nThreads);

    public abstract ExecutorService getService(float fractionThreads);

}
