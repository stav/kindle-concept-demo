
//
//  Source code (C) Copyright 2010 Markus Schmidt (m.schmidt@emtec.com)
//  Published under MIT License (http://en.wikipedia.org/wiki/MIT_License)  
//  Amazon gift cards welcome.
//
//
//  This demo illustrates a few concepts to build a robust Kindlet
//  - handling life cycle correctly (actually this is rather difficult)
//  - logging
//  - aiding garbage collection when destroying 
//  - handling screen sizing to adjust to different kindle models and device orientation
//  - catching five-way controller events and general keyboard handling 
//  - understanding screen redraws
//  - painting a game board either with child components or via direct paint()
//  - loading prescaled images to improve drawing speed 
//

The stuff in this archive is a Kindlet sample application which demonstrates some 
essential concepts of writing a robust Kindlet application.

It the functionality itself is rather limited, the focus is on showing solutions 
to some common Kindle programming problems.  These have been used in our own 
applications and they are the result of publishing four Kindle apps.  


I strongly recommend to print the two java files and read them top to bottom like a
book, focusing on the comments.  Also run the applet in the simulator and watch the 
console output (use the consoles stop/start buttons to simulate a screensaver event too).

Good luck


Markus Schmidt
(m.schmidt@emtec.com) 

-------------------------------------------------------------------------------------------

Build instructions:
 
  Check the build.xml file for correct paths.
  - dir.kdk should point to your KDKv1 folder.
  - the dev libs (Kindlet-1.0.jar, json_simple-1.1.jar, log4j-1.2.15.jar, xml-apis.jar) 
    should in the <dir.kdk>/lib folder.
  - if you are building a device version, have the developer key in the <dir.kdk>/keys folder
  - for a simulator compile, go to the folder with build.xml and type: ant build-unsigned-active-content 
  - for a simulator run, go to the folder with build.xml and type: ant run
  - for a device build, go to the folder with build.xml and type: ant device
	  
-------------------------------------------------------------------------------------------

Versions:

 2011-07-20 v0.80 - first release
 
 2011-07-20 v0.81 - some typos fixed.  
                  - added waitForImage() to GameBoard class. 
 
 -------------------------------------------------------------------------------------------
  