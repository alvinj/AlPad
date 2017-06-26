AlPad
=====

This project is a relatively simple "Scratchpad" text editor.

I originally thought I just wanted a one-window, zero-tabs JFrame
with a JTextArea, but then I decided I wanted tabs so I could have 
multiple text areas, then I wanted to convert tabs in the text to 
spaces, then I wanted to be able to indent text, un-indent text, 
undo/redo changes, and I think that might be it.

I originally named this project "Pasty" because I thought I would
be mostly pasting in-to and out-of the editor, but I now call it
"AlPad".

As one important point, this editor does not write to the disk or
read from the disk. As both names imply, I just want this editor running
on the side so I can use it as a scratchpad thing.


Stranger Things
---------------

There are a couple of unusual things in the code:

* I'm experimenting with variable names, so the global variables in the
  code begin with the letter `g`. 
* I'm also using this project to get more comfortable using ProGuard,
  in particular from within an Ant build script. (ProGuard isn't
  actually helping anything at the moment, but I hope to get back to
  it at some point.)

Compiling/Building
------------------

At the moment, use this script to compile and run the app:

    _buildRun.sh

I've only built this on one system, so I think there may be a build
problem related to the _classes_ directory that the build script uses.
If you run into any problems with the build script, just make the
_classes_ directory manually if necessary, and that may resolve the
problem.

Note: I was trying to get this project working with my 
MacIosComponents library, but some part of that integration
wasn’t working.



Icon
----

The icon comes from http://commons.wikimedia.org/wiki/File:Nuvola_apps_klipper.png


More Information
----------------

You can find more information about AlPad 
[at this url](http://alvinalexander.com/apps/alpad).

You can find more information about me at [alvinalexander.com](http://alvinalexander.com).



