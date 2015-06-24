package jm.mode.replmode;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.Connector.Argument;

import processing.app.Base;
import processing.app.RunnerListener;
import processing.app.SketchException;
import processing.core.PApplet;
import processing.mode.java.JavaBuild;
import processing.mode.java.runner.Runner;

public class REPLRunner extends Runner {
  String portStr;
  boolean isWindowVisible;
  boolean hasFailedLoad;

  public REPLRunner(JavaBuild build, RunnerListener listener)
      throws SketchException {
    super(build, listener);
    
    portStr = "";
    isWindowVisible = false;
  }

  public void launchREPL(boolean refresh) {
    // I <3 short circuiting
    if ((!isWindowVisible || refresh) && launchREPLVirtualMachine()) {
      isWindowVisible = true;
      generateTrace();
      isWindowVisible = false;
    }
  }
  
  public boolean launchREPLVirtualMachine() {
    if (process != null)
      System.out.println(process.isAlive());
    else
      System.out.println("Null process");
    
    int port = 8000 + (int) (Math.random() * 1000);
    portStr = String.valueOf(port);
    String[] vmParams = getMachineParams();
    String[] sketchParams = getSketchParams(false);    

    String hotSwapArg = "";
    URL url = REPLRunner.class.getProtectionDomain().getCodeSource().getLocation();
    File currentDirectory = null;
    try {
      currentDirectory = new File(url.toURI());
      hotSwapArg = "-javaagent:" + currentDirectory.getParentFile().getAbsolutePath() + "/hotswap-agent.jar=autoHotswap=true";
      System.out.println(currentDirectory.getParentFile().getAbsolutePath());
      hasFailedLoad = false;
    } catch (URISyntaxException e2) {
//      e2.printStackTrace();
      System.err.println("The hot swapper is feeling a little sleepy right now. Don't worry- REPL Mode will try to wake it up");
      hasFailedLoad = true;
    }
    // Newer (Java 1.5+) version that uses JVMTI
    String jdwpArg = "-agentlib:jdwp=transport=dt_socket,address=" + portStr + ",server=y,suspend=y";
    // Everyone works the same under Java 7 (also on OS X)
    String[] commandArgs = new String[] { Base.getJavaPath(), jdwpArg,hotSwapArg };
    System.out.println(Base.getJavaPath());
    commandArgs = PApplet.concat(commandArgs, vmParams);
    commandArgs = PApplet.concat(commandArgs, sketchParams);
    launchJava(commandArgs);

    /*
     * This part seems to be used to get the vm, that is in turn used not only
     * for the debugger, but to close the sketch frame as well...
     */
    AttachingConnector connector = (AttachingConnector)
      findConnector("com.sun.jdi.SocketAttach");

    Map<String, Argument> arguments = connector.defaultArguments();

    Connector.Argument portArg = arguments.get("port");
    portArg.setValue(portStr);

    try {
      while (true) {
        try {
          vm = connector.attach(arguments);
          if (vm != null) {
            return true;
          }
        } catch (IOException e) {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e1) {
            e1.printStackTrace(sketchErr);
          }
        }
      }
    } catch (IllegalConnectorArgumentsException exc) {
      throw new Error("Internal error: " + exc);
    }
  }
  
  boolean isFailedLoad() {
    return hasFailedLoad;
  }
}
