REPL Mode for Processing 3.0
============================

About
-----
This mode adds in a Read-Evaluate-Print-Loop console to processing in the form of a tab at the bottom. The console enables users to type in processing code and to view the output of this code immediately. Each subsequent line of code shows the output incrementally, much like how an REPL Console for any interpretive language (like Python and the Linux bash terminal) would. The console also provides options to undo commands, to convert the valid commands entered so far into a function, and so on.  
  
The mode also brings hot swapping to the table. This enables the user to view changes made to the sketch without the need for restarting the sketch.

Using the REPL Mode- a 2 minute guide
-------------------------------------

Using the additional features of the mode is simple:

*   **REPL Console**
    *   First, initialize the console with the init keyword.
    *   Next, type in absolutely anything into the console, and watch the output come alive!
    *   To split a single function call into multiple lines, press the Enter key after a comma.
    *   For more information about the init keyword, and for a list of keywords available, type help.
*   **Hot swapping**
    *   Create a new sketch, type stuff in, and save it. Alternately, open an existing sketch.
    *   Run the sketch, and leave the sketch window open.
    *   Make required changes to the sketch, and voilà! Hot swapped!


Requirements
------------
* Processing 3.11a or later (preferably the latest version built from Github).
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

2. In the command prompt or terminal, simply run `ant run` from the resources directory (where the build.xml and build.properties files are present).

Using the hot-swapper
---------------------

Using the hot-swap feature is super simple- simply save the sketch and run it and leave the sketch window open, and after making the required changes, save the sketch to have the sketch window display the contents of the updated sketch.

The REPL Console Commands
-------------------------
* The REPL Mode Console supports pretty much anything that can be entered in the draw() method of processing- basic shapes, variable declarations, control constructs (`if-else`, `for`, `while`, etc.) and so on.
* In order to import a library, simply type `import awesome.cool.supercool.*;` in a new line in the REPL Console.
* The REPL Mode Console also has the following **`command words`**, used to issue commands to the REPL Console:
  * **`init`**: Represents the first command issued to the REPL Mode Console, to initialize the it. Also used to pass in details about width and height of the sketch the console has to display. May take the following forms:
    * `init`: The basic form, initializes the REPL Console to display a sketch of size 100x100
    * `init w h`: Initializes the console to display a sketch of width w and height h. Equivalent to calling `size(w,h);` in the `setup()` method
    * `init w h r`: Initializes the console to display a sketch of width w and height h, and to use a renderer of type r, where r is a string that may take values `P2D` or `P3D`. Equivalent to calling `size(w,h,"P2D");` or `size(w,h,"P3D");` in the `setup()` method
  * **`resize`**: Allows the user to resize sketch that the REPL console displays without losing the contents of the sketch. Can be run in one of the following 3 ways, each similiar to their `init` counterparts:
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
  * **`codify`**: Used to get a method that, if called, displays everything currently visible in the sketch window. More precisely, it adds a method of the void return type to the current tab, the method body consisting of all statements used to display the output visible at present (i.e., all statements from the last init, excluding those undone). Takes the format `codify x`, where x is a string representing the method name.

Navigating in the REPL Console
------------------------------
* A single statement may be entered in a new line, for example,
  ```
  >> rect(20,20,40,40);
  ```
* A statement block may take 2 forms:
  * A single function call spilt over multiple lines (ending with a comma on the first line, and a semi-colon on the last), for example:
    ```
    >> rect(20, 20, 
    ...    40, 40);
    ```
  * A statement block split over multiple lines (ending with an opening curly brace on the first line, and a closing curly brace on the last), for example:
    ```
    >> if (true) {
    ...    if (true) {
    ...    print("I love processing");
    ...    }
    ...    }
    ```
* Command history may be easily navigated with the up and down arrow keys, much like a typical console
* To copy text, right click in the console, select `Mark`, mark the required text, right-click again, and select `Copy`. To copy everything, simply right-click and select `Copy All`

Use case
---------
The REPL Mode is very similar to the Java Mode, and supports everything that the Java Mode does.  
However, it aims to bring 2 new features to the table:

*   **REPL Console**: The REPL mode contains an REPL Console for easy, quick, handy, convenient prototyping
*   **Hot swapping**: The REPL mode permits restartless hot-swapping of a sketch, saving developer time

A typical "interaction" with the REPL Console may look like the following:

![image](https://joelmoniz.files.wordpress.com/2015/06/repl_output_usecase.png?w=716)

Bugs and Feature requests
-------------------------
Please file bugs and feature requests [here](https://github.com/joelmoniz/REPLmode/issues).
