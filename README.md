REPL Mode for Processing 3.0
============================

About
-----
This mode adds in a Read-Evaluate-Print-Loop console to processing in the form of a tab at the bottom. The console enables users to type in processing code and to view the output of this code immediately. Each subsequent line of code shows the output incrementally, much like how an REPL Console for any interpretive language (like Python and the Linux bash terminal) would. The console also provides options to undo commands, to convert the valid commands entered so far into a function, and so on.

Requirements
------------
* Processing 3.10a or later (preferably the latest version built from Github).
* Apache Ant to build.

Building from Source
---------------------
To build REPL Mode from source (recommended), do the following:

1. Setup the correct paths in the `resources/build.properties` file. The following are likely the only paths that you will manually have to set:
  * `processing.source.base` : Represents the path to the processing source directory
  * `processing.classes.core` : Represents the path to the core.jar file
  * `processing.classes.pde` : Represents the path to the pde.jar file
  * `processing.modes` : Represents the path to the folder where the mode is to be installed (usually the `modes` folder inside the sketchbook)
  * `processing.executable` : Represents the path to the processing executable

2. In the command prompt or terminal, simply run `ant full` from the resources directory (where the build.xml and build.properties files are present).

The REPL Mode Commands
----------------------
* The REPL Mode supports pretty much anything that can be entered in the draw() method of processing- basic shapes, variable declarations, control constructs (`if-else`, `for`, `while`, etc.) and so on.
* At present, the REPL Mode does not support calling methods or declaring objects that would require a library to be imported. Support for this feature, however, will be added soon.
* The REPL Mode also has the following **`command words`**, used to issue commands to the REPL Console:
  * **`init`**: Represents the first command issued to the REPL Mode, to initialize the mode. Also used to pass in details about width and height of the sketch the mode has to display. May take the following forms:
    * `init`: The basic form, initializes the REPL Console to display a sketch of size 100x100
    * `init w h`: Initializes the console to display a sketch of width w and height h. Equivalent to calling `size(w,h);` in the `setup()` method
    * `init w h r`: Initializes the console to display a sketch of width w and height h, and to use a renderer of type r, where r is a string that may take values `P2D` or `P3D`. Equivalent to calling `size(w,h,"P2D");` or `size(w,h,"P3D");` in the `setup()` method
  * **`resize`**: Allows the user to resize sketch that the mode displays without losing the contents of the sketch. Can be run in one of the following 3 ways, each similiar to their `init` counterparts:
    * `resize`
    * `resize w h`
    * `resize w h r`
  * **`clear`**: Used to clear the REPL Console without affecting anything else.
  * **`undo`**: Used to undo a (set of) statement(s). Note that command statements cannot be undone. Can be called in one of 2 ways:
    * `undo`: Undoes the last valid statment.
    * `undo x`: Undoes the last x statements.
  * **`redo`**: Used to redo a (set of) statement(s). Note that a redo can only be performed immendiately after an undo. Can be called in one of 2 ways:
    * `redo`: "Redoes" the last undo
    * `redo x`: "Redoes" the last x statements undone by an undo
  * **`print`**: Used to get a method that, if called, displays everything currently visible in the sketch window. More precisely, it adds a method of the void return type to the current tab, the method body consisting of all statements used to dsiplay the output visible at present (i.e., all statements from the last init, excluding those undone). Takes the format `print x`, where x is a string representing the method name.

Use case
---------
The REPL Mode makes for some quick, handy, convenient prototyping. A typical "interaction" with the REPL Console may look like the following:

![image](https://joelmoniz.files.wordpress.com/2015/06/repl_output_usecase.png?w=716)

Bugs and Feature requests
-------------------------
Please file bugs and feature requests [here](https://github.com/joelmoniz/REPLmode/issues).
