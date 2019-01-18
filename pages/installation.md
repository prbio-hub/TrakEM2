---
layout: page
title: Installation 
description: Installation of rhizoTrak
---

To install there are two option

1. Using Fiji and the MiToBo update site
	* Download [Fiji](https://fiji.sc/) and follow the installation instructions of Fiji.
	* Run Fiji by opening the application file provided in the Fiji directory (on Windows/MacOS)
	or by executing the available shell scripts (Linux).
	* Navigate to the update function via <code>Help → Update...</code> from the Fiji menu
	and click the button <code>Manage update sites</code> in the ImageJ Updater.
	* Find the update site MiToBo and check it active and click the button <code>Close</code>.
	* Fiji will now display a message that there are some new files to install:  
	click the button <code>Apply changes</code> to install the files, 
	* restart Fiji and rhizoTrak will be available as a new plugin

2. From source code

	This is mainly of interest for those who want to modify the code
	* Clone the rhizoTrak git repository from [github](https://github.com/prbio-hub/rhizoTrak).     
	  (Note: this repository is a fork of the 
		[TrakEM2 repository](https://github.com/trakem2/TrakEM2/))
	* Check out the branch <code>rhizoTrak</code>.
	* Execute the maven goal <code>package</code> which creates the
		rhizoTrak jar in the directory <code>target</code>.    
	  The name of the jar will be <code>rhizoTrak_???.jar</code>, where <code>???</code>
		depends on the version of the artefact.
	* This jar needs to be located in the <code>plugins</code> directory of
		a [ImageJ](https://imagej.nih.gov/ij/) 
		or [Fiji](https://fiji.sc/) installation, and all dependencies of rhizoTrak in
		the corresponding <code>jars</code>directory.     
	  	The easiest way to achieve this is to install rhizoTrak using Fiji and 
		the MiToBo update site (see above) and copy the rhizoTrak jar to its <code>plugins</code> directory.
	
