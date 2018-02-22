rhizoTrak
---------

Copyright 2018 Birgit Möller, Stefan Posch, Tino Schmidt, Axel Zieschank

rhizoTrak is a tool for manual and semi-automated annotation of root images. 

The code of this project is substantially based on the [TrakEM2]( https://imagej.net/TrakEM2) plugin for ImageJ/Fiji. 
Please find further information about the original TrakEM2 plugin at the end of this page. 

rhizoTrak extends TrakEM2 towards root-specific functionality, e.g., adds support for importing and exporting annotation data in [RSML format]( https://rootsystemml.github.io/data), extends the connector concept from purely geometric to object-based links, and adds several additional configuration options.

rhizoTrak is released under the General Public License in its latest version, please find the License file included.

Contact: rhizoTrak@informatik.uni-halle.de

More information about rhizoTrak can be found on its webpage: https://github.com/prbio-hub/rhizoTrak/wiki

For installation in ImageJ/Fiji simply activate its update site "rhizoTrak".

---------
##### Original README information about TrakEM2:

Copyright 2005-2014 Albert Cardona and Rodney Douglas.
Copyright 2007-2014 Stephan Saalfeld, Stephan Preibisch, Ignacio Arganda, Verena Kaynig

Released under the General Public License in its latest version.

Contact: acardona at ini phys ethz ch

To compile the source, call mvn (Maven http://maven.apache.org/) in the root source directory (builds everything) or in one of the subprojects (build just the subproject).

It is strongly recommended to use Java 1.6.0 or higher whenever possible, since repainting speed is much higher.

Have fun! Beer and comments to acardona at ini phys ethz ch
