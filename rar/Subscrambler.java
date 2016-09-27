// (c) 2013 by Roelof Ruis

import java.io.*;
import javax.swing.*;
import javax.swing.UIManager.*;

public class Subscrambler {

  public static void main(String[] args) {
  
    System.out.printf("Starting the subscrambler program...\n");
    
    // initialize program frame in new thread
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        try {
          UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); //getCrossPlatformLookAndFeelClassName
        } catch (Exception e) {
          System.err.printf("Look and Feel could not be loaded...");
          System.exit(1);
        }
        // Initialize the program frame!
        new ProgramWindow();
      }
    });
    
  }

}