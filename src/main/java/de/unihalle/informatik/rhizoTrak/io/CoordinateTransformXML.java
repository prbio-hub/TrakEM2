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

package de.unihalle.informatik.rhizoTrak.io;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import mpicbg.models.TransformList;
import mpicbg.trakem2.transform.CoordinateTransform;
import mpicbg.trakem2.transform.CoordinateTransformList;
import mpicbg.trakem2.transform.InvertibleCoordinateTransform;
import mpicbg.trakem2.transform.InvertibleCoordinateTransformList;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import de.unihalle.informatik.rhizoTrak.utils.IJError;
import de.unihalle.informatik.rhizoTrak.utils.Utils;

public class CoordinateTransformXML
{
	static public CoordinateTransform parse(final String xmlPath) throws Exception {
		InputStream istream = null;
		try {
			SAXParserFactory f = SAXParserFactory.newInstance();
			f.setValidating(false);
			SAXParser parser = f.newSAXParser();
			istream = Utils.createStream(xmlPath);
			Parser p = new Parser();
			parser.parse(new InputSource(istream), p);
			return p.ct;
		} finally {
			try {
				if (null != istream) istream.close();
			} catch (Exception e) {}
		}
	}

	static private class Parser extends DefaultHandler {

		/** A stack of potentially nested instances of {@link CoordinateTransformList}. */
		final ArrayList<TransformList<Object>> ct_list_stack = new ArrayList<TransformList<Object>>();
		/** The final result: the top-level {@link CoordinateTransform}. */
		private CoordinateTransform ct = null;
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			final HashMap<String,String> t = new HashMap<String,String>();
			for (int i=attributes.getLength() -1; i>-1; i--) {
				t.put(attributes.getQName(i).toLowerCase(), attributes.getValue(i));
			}
			makeCoordinateTransform(qName.toLowerCase(), t);
		}
		
		public void endElement(String namespace_URI, String local_name, String qualified_name) {
			if (qualified_name.endsWith("ict_transform_list")) {
				ct_list_stack.remove( ct_list_stack.size() - 1 );
			}
		}

		private final void makeCoordinateTransform(
			final String type,
			final HashMap<String,String> ht_attributes)
		{
			try
			{	
				if ( type.equals( "ict_transform" ) )
				{
					final CoordinateTransform ct = ( CoordinateTransform )Class.forName( ht_attributes.get( "class" ) ).newInstance();
					ct.init( ht_attributes.get( "data" ) );
					if ( ct_list_stack.isEmpty() )
					{
						this.ct = ct;
					}
					else
					{
						ct_list_stack.get( ct_list_stack.size() - 1 ).add( ct );
					}
				}
				else if ( type.equals( "iict_transform" ) )
				{
					final InvertibleCoordinateTransform ict = ( InvertibleCoordinateTransform )Class.forName( ht_attributes.get( "class" ) ).newInstance();
					ict.init( ht_attributes.get( "data" ) );
					if ( ct_list_stack.isEmpty() )
					{
						this.ct = ict;
					}
					else
					{
						ct_list_stack.get( ct_list_stack.size() - 1 ).add( ict );
					}
				}
				else if ( type.equals( "ict_transform_list" ) )
				{
					final CoordinateTransformList< CoordinateTransform > ctl = new CoordinateTransformList< CoordinateTransform >();
					if ( ct_list_stack.isEmpty() )
					{
						this.ct = ctl;
					}
					else
					{
						ct_list_stack.get( ct_list_stack.size() - 1 ).add( ctl );
					}
					ct_list_stack.add( ( TransformList )ctl );
				}
				else if ( type.equals( "iict_transform_list" ) )
				{
					final InvertibleCoordinateTransformList< InvertibleCoordinateTransform > ictl = new InvertibleCoordinateTransformList< InvertibleCoordinateTransform >();
					if ( ct_list_stack.isEmpty() )
					{
						this.ct = ictl;
					}
					else
					{
						ct_list_stack.get( ct_list_stack.size() - 1 ).add( ictl );
					}
					ct_list_stack.add( ( TransformList )ictl );
				}
			}
			catch ( Exception e ) { IJError.print(e); }
		}
	}
}
