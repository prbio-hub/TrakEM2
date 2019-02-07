---
layout: page
title: Development
description: Development for rhizoTrak
---

# Table of Contents
1. [How to write your own segmentation operator in rhizoTrak](#write)
2. [Template project for developing operators](#project)
3. [Install your new operator in rhizoTrak](#install)

## How to write your own segmentation operator in rhizoTrak<a name="write"></a>

In rhizoTrak you can aid the manual annotation process through an automatic
segmentation. This automatic segmentation is performed by a [MiToBo](https://mitobo.informatik.uni-halle.de) operator.
[MiToBo](https://mitobo.informatik.uni-halle.de) is a Java toolbox and programming environment for image processing and
analysis algorithms deeply linked to [ImageJ](https://imagej.net/Welcome) and [Fiji](http://fiji.sc/).
To learn more about MiToBo's basic concepts of operators and how to implement your own operators, take a look at
MiToBo's [User Guide](http://www.informatik.uni-halle.de/mitobo/downloads/manual/MiToBoManual.pdf).
In addition, MiToBo's [Javadoc API](http://www.informatik.uni-halle.de/mitobo/api/index.html)
gives you an overview of its functionality
and more details about the programming interface.

For adding segmentation functionality to rhizoTrak you can
either use an existing segmentation operator or implement your own. Every 
segmentation operator takes the image of the current layer as an input and gives
the calculated segmentation in the expected data structure back.

To get a good, i.e. useful, segmentation the way of calculating the segmentation
has to fit the data and their specific characteristic. In some cases the 
existing operators do not perform well on your data. Then it may be a good idea
to write your own implementation of a segmentation operator. The following
guide will help you to do this by explaining the parameters of the interface.
 
The class [`RootSegmentationOperator`](http://mitobo.informatik.uni-halle.de/api/de/unihalle/informatik/MiToBo/apps/minirhizotron/segmentation/RootSegmentationOperator.html) (in package [de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation](http://mitobo.informatik.uni-halle.de/api/de/unihalle/informatik/MiToBo/apps/minirhizotron/segmentation/package-summary.html))
is the interface for your own operator. Therefore you have to write an operator class which extends this
[`RootSegmentationOperator`](http://mitobo.informatik.uni-halle.de/api/de/unihalle/informatik/MiToBo/apps/minirhizotron/segmentation/RootSegmentationOperator.html) class.

#### Input parameter
The input is the image you want to segment.<br><br>The class [`RootSegmentationOperator`](http://mitobo.informatik.uni-halle.de/api/de/unihalle/informatik/MiToBo/apps/minirhizotron/segmentation/RootSegmentationOperator.html)
has a protected variable `image` of type
[`ij.ImagePlus`](https://imagej.nih.gov/ij/developer/api/ij/ImagePlus.html).
You can access this variable in your own class (through `this.image`).
Alternatively, there is a getter method `getImage()` which returns the image.
When using your own segmentation operator in rhizoTrak, the current layer image is set automatically by rhizoTrak
as this image via `setImage(ImagePlus img)`.<br><br>
Of course, internally you can also use the MiToBo internal image data structures in your calculations, i.e.
the data type
[`MTBImage`](http://mitobo.informatik.uni-halle.de/api/de/unihalle/informatik/MiToBo/core/datatypes/images/MTBImage.html)
or one of its derived data types, e.g.,
[`MTBImageByte`](http://mitobo.informatik.uni-halle.de/api/de/unihalle/informatik/MiToBo/core/datatypes/images/MTBImageByte.html).
To convert your image named `img` from type [`ij.ImagePlus`](https://imagej.nih.gov/ij/developer/api/ij/ImagePlus.html) to
[`MTBImageByte`](http://mitobo.informatik.uni-halle.de/api/de/unihalle/informatik/MiToBo/core/datatypes/images/MTBImageByte.html) you have to add the 
following line in your code:<br><br>
```MTBImageByte mtbImage = (MTBImageByte) MTBImage.createMTBImage(img).convertType(MTBImageType.MTB_BYTE, true)```

#### Output parameter
The operator should return the segmentation results in the expected data 
structure. Then, if used within rhizoTrak, the results will be imported as
treelines. The output 
parameter `resultLineMap` of the operator is protected and can therefore be accessed directly in 
your own class (through `this.resultLineMap`). It is of type `Map<Integer, Map<Integer, Node>>`.

* The integer key in the outer map identifies the treeline. It is an unique
		ID for every treeline.<br><br>
* In the inner map the integer key identifies the node. Every node has an
	     	ID which is unique within its respective treeline. 
* The nodes are of type `Node` (see package 
		de.unihalle.informatik.MiToBo.apps.minirhizotron.segmentation.Node):

  * A node can have a predecessor node in the treeline. In the variable 
			`predecessor` of type `int` you store
			the ID (see above) of the predecessor node. By this rhizoTrak can then 
			display the nodes in the right order in the treeline, including
			branching. So two nodes can have the same predecessor ID. For the
			root node, which obviously has no predecessor, rhizoTrak is expecting
			the ID "-1".<br><br>
  * `x` indicates the x position of the node in pixels in the image. <br>This
			variable is of type `double`, so positions can be specified with sub-pixel accuracy.<br><br>
  * `y` indicates the y position of the node in pixels in the image. As
			 	for `x`, `y` is of type `double`.<br><br>
  * The diameter of the node is stored in `diameter` of type `double`. <br>
			It can be set to 0.0, then the node has no thickness.<br><br>
  * Additionally there are two more parameters `nx` and `ny` of type `double`
			which are only used internally and can safely be ignored.<br><br>
  * There are three different constructors available for the class `Node`:<br><br>
		  	- `Node(double x, double y)`, with predecessor = 0 and diameter = 0.0 <br>
		 	- `Node(int predecessor, double x, double y)`, with diameter = 0.0<br>
		 	- `Node(int predecessor, double x, double y, double diameter)`


## Template project for developing operators <a name="project"></a>

For a quick start in operator development we provide you with a pre-configured Maven project.<br>
You can download the project from our [Github repository](https://github.com/prbio-hub/rhizoTrak-segmentationOperatorDemo).

To setup the project, just clone the repository to a folder of your choice.

In the sub-folder `src/main/java/rhizoTrak/segmentation` of the project you can find the dummy segmentation operator class
[RhizoTrakDummySegmentationDemo.java](https://github.com/prbio-hub/rhizoTrak-segmentationOperatorDemo/blob/master/src/main/java/rhizoTrak/segmentation/RhizoTrakDummySegmentationDemo.java) which you can use as template for your own implementation.<br>
Note that the operator just creates some arbitrary treelines, however, completely ignores the actual input image - it's really just a dummy.

Some important notes regarding the demo operator:
* lines 70-72:<br> your new operator class needs to extend [`RootSegmentationOperator`](http://mitobo.informatik.uni-halle.de/api/de/unihalle/informatik/MiToBo/apps/minirhizotron/segmentation/RootSegmentationOperator.html) and you need to add the annotation `@ALDDerivedClass` to your class definition, otherwise rhizoTrak will not be able to find your new operator
* lines 83-85:<br> your operator must have a public constructor without any arguments, otherwise rhizoTrak cannot invoke an instance of your operator class
* lines 94ff:<br> the `operate()` method is the method called from rhizoTrak to invoke your operator; at the time of invocation you can assume that the member variable `image` inherited from the parent class contains the current image to be processed
* line 101:<br> here we instantiate the result data structure which is subsequently filled with some segment data (see above)
* lines 130-133:<br> every operator class is required to implement the method `getUniqueClassIdentifier()` to allow rhizoTrak to index objects of this class; choose a unique and comprehensive string as identifier

## Install your new operator in rhizoTrak <a name="install"></a>
To install your new operator in rhizoTrak you need to build a jar archive containing the class file of your new operator class.<br>

The easiest way to build such a jar is to run `mvn package` on the command line in your project.<br>

Afterwards you can find the jar file in the sub-folder `target` of your project. Just copy the jar file to the `jars` folder of your local Fiji installation and restart Fiji. When you run rhizoTrak then your new operator should appear in the list of available detection operators in rhizoTrak.
