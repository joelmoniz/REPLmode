package jm.mode.replmode;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.text.Utilities;

public class CommandPromptPane extends NavigationFilter {
  private int prefixLength;

  private Action deletePrevious;

  private Action shiftLine;

  JTextArea consoleArea;

  String prompt;

  int rowStartPosition;

  public CommandPromptPane(String prompt, JTextArea component) {
    consoleArea = component;
    this.prompt = prompt;
    this.prefixLength = prompt.length();
    rowStartPosition = 0;

    deletePrevious = component.getActionMap().get("delete-previous");
    shiftLine = component.getActionMap().get("insert-break");
    component.getActionMap().put("delete-previous", new BackspaceAction());
    component.getActionMap().put("insert-break", new EnterAction());
    component.addKeyListener(new CommandKeyListener());
    component.setCaretPosition(prefixLength);
//    component.setWrapStyleWord(true);
    component.setLineWrap(true);
  }

  public void setDot(NavigationFilter.FilterBypass fb, int dot,
                     Position.Bias bias) {
    fb.setDot(Math.max(dot, rowStartPosition + prefixLength), bias);
  }

  public void moveDot(NavigationFilter.FilterBypass fb, int dot,
                      Position.Bias bias) {
    fb.moveDot(Math.max(dot, rowStartPosition + prefixLength), bias);
  }

  class BackspaceAction extends AbstractAction {
    private static final long serialVersionUID = -116059028248053840L;

    public void actionPerformed(ActionEvent e) {
      JTextArea component = (JTextArea) e.getSource();

      if (getColumn(component) > prefixLength + 1) {
        deletePrevious.actionPerformed(null);
      }
    }
  }

  class EnterAction extends AbstractAction {
    private static final long serialVersionUID = 2813908067205522536L;

    public void actionPerformed(ActionEvent e) {
      JTextArea component = (JTextArea) e.getSource();
      shiftLine.actionPerformed(null);
      component.replaceSelection(prompt);
//      System.out.println("Position: "+component.getCaretPosition());
      try {
        rowStartPosition = Math.max(rowStartPosition, Utilities
            .getRowStart(consoleArea, consoleArea.getCaretPosition()));
      } catch (BadLocationException e1) {
        e1.printStackTrace();
      }
    }
  }

  class CommandKeyListener implements KeyListener {

    @Override
    public void keyTyped(KeyEvent e) {
      switch (e.getKeyCode()) {
      case KeyEvent.VK_UP:
        ; // TODO: Bring previous line's code here
      }
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

  }

  // Refer : http://stackoverflow.com/a/2750099/2427542
  // Refer : http://stackoverflow.com/a/13375811/2427542

  /**
   * Use to get line number at which caret is placed.
   * 
   * Code adapted from http://java-sl.com/tip_row_column.html
   * 
   * @param console
   *          : The JTextArea console
   * @return Row number
   */
  public static int getRow(JTextArea console) {
    int pos = console.getCaretPosition();
    int rn = (pos == 0) ? 1 : 0;
    try {
      int offs = pos;
      while (offs > 0) {
        offs = Utilities.getRowStart(console, offs) - 1;
        rn++;
      }
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    return rn;
  }

  /**
   * Use to get location of column at which caret is placed. The column number
   * starts with 1.
   * 
   * Code adapted from http://java-sl.com/tip_row_column.html
   * 
   * @param console
   *          : The JTextArea console
   * @return Column number
   */
  public static int getColumn(JTextArea console) {
    int pos = console.getCaretPosition();
    try {
      return pos - Utilities.getRowStart(console, pos) + 1;
    } catch (BadLocationException e) {
      e.printStackTrace();
    }
    return -1;
  }

  /*
   * Convenience main() function for easy debugging.
   */
  public static void main(String args[]) throws Exception {

    JTextArea textField = new JTextArea(">> ", 20, 40);
    textField.setNavigationFilter(new CommandPromptPane(">> ", textField));

    JFrame frame = new JFrame("Navigation Filter Example");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(textField);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}