// (c) 2013 by Roelof Ruis

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.awt.event.*;
import java.lang.*;
import java.util.Random;

public class DisplayWindow extends JFrame implements KeyListener {
  
  DrawingLoop drawingLoop = null;
  GraphicsPanel graphicsPanel = new GraphicsPanel();
  final int WIDTH;
  final int HEIGHT;
  private long sleeptime = 2000;
  private long fadetime = 50;
  
  public DisplayWindow() {
  
    setUndecorated(true);
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSze = toolkit.getScreenSize();
    WIDTH = screenSze.width;
    HEIGHT = screenSze.height;
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    setTitle("Display");
    setMinimumSize(new Dimension(WIDTH, HEIGHT));
    setSize(WIDTH,HEIGHT);
    setLocation(0,0);
    
    // Hide the cursor
    BufferedImage cursorImg = new BufferedImage(16,16, BufferedImage.TYPE_INT_ARGB);
    Cursor blankCursor = toolkit.createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
    getContentPane().setCursor(blankCursor);
    
    addKeyListener(this);
    
    add(graphicsPanel);
    
    setVisible(false);
  }
  
  public void setSleepyTime(long time) {
    sleeptime = time;
    if (drawingLoop != null) {
      drawingLoop.setSleepyTime(time);
    }
  }
  
  public void setFadeTime(long time) {
    fadetime = time;
    if (drawingLoop != null) {
      drawingLoop.setFadeTime(time);
    }
  }
  
  // PUBLIC METHODS
  public void stopDisplay() {
    if (drawingLoop != null) {
      drawingLoop.running = false;
      drawingLoop = null;
      setVisible(false);
    }
  }
  
  public void startDisplay(File rawData) {
    if (rawData != null) {
      stopDisplay();
      drawingLoop = new DrawingLoop(rawData, sleeptime, fadetime);
      drawingLoop.running = true;
      setVisible(true);
    }
  }
  
  
  // KEY LISTENER EVENTS
  public void keyReleased(KeyEvent e) {
  }
  
  public void keyTyped(KeyEvent e) {
  }
    
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
      stopDisplay();
    }
  }
  
  // ------------------------------------------------------
  // ------------ INNER CLASS GRAPHICS PANEL --------------
  // ------------------------------------------------------
  protected class GraphicsPanel extends JPanel{
    
    public volatile String text = "";
    private Font startFont;
    private final int STARTSIZE = 80;
    private volatile float alpha = 0.0f;
    private int curStringWidth = 0;
    private Font curFont;
    
    // Constructor
    public GraphicsPanel() {
      setOpaque(true);
      setBackground(Color.WHITE);
      try {
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(Font.createFont(Font.TRUETYPE_FONT,new File("data/perpetua.ttf")));
      } catch (Exception e) {
        System.out.printf("Font file missing or damaged.\n");
        System.exit(0);
      }
      startFont = new Font("perpetua", Font.PLAIN, STARTSIZE);
    }
    
    // Public Methods
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2d = (Graphics2D) g;
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
      int width = (int) getSize(null).getWidth();
      int height = (int) getSize(null).getHeight();
      g2d.setFont(curFont);
      g2d.drawString(text, (int) ((width - curStringWidth) / 2), (int) (height / 2));
    }
    
    public void setTextAndRepaint(String newText) {
      text = newText;
      alpha = 0.0f;
      findCorrectMeasurement();
    }
    
    public boolean fadeIn() {
      if (alpha < 1.0) {
        alpha += 0.05;
        if (alpha > 1.0) {
          alpha = 1.0f;
        }
        repaint();
        return true;
      } else {
        alpha = 1.0f;
        return false;
      }
    }
    
    public boolean fadeOut() {
      if (alpha > 0.0) {
        alpha -= 0.05;
        if (alpha < 0.0) {
          alpha = 0.0f;
        }
        repaint();
        return true;
      } else {
        alpha = 0.0f;
        return false;
      }
    }
    
    public void findCorrectMeasurement() {
      int width = (int) getSize(null).getWidth();
      int height = (int) getSize(null).getHeight();
      curFont = startFont;
      FontMetrics fm = getFontMetrics(curFont);
      int size = STARTSIZE;
      int fontType = Font.PLAIN;
      if (text.matches("<i>.+</i>")) {
        System.out.printf("\n\t JA!\n");
        fontType = Font.ITALIC;
      }
      text.replaceAll("<i>","").replaceAll("</i>","");
      while (fm.stringWidth(text) > (width - 20)) {
        size = size - 5;
        curFont = new Font("perpetua", Font.PLAIN, size);
        fm = getFontMetrics(curFont);
      }
      curStringWidth = fm.stringWidth(text);
      repaint();
    }
   
  }
  
  // ------------------------------------------------------
  // ------------ INNER CLASS DRAWING LOOP ----------------
  // ------------------------------------------------------
  protected class DrawingLoop extends Thread implements Runnable {
    
    public volatile boolean running = false;
    
    private long sleepyTime;
    private long fadeTime;
    private File rawData;
    private long rawDataLength;
    private Random r = new Random(System.currentTimeMillis());
    
    public DrawingLoop(File rawData, long sleepyTime, long fadeTime) {
      this.sleepyTime = sleepyTime;
      this.rawData = rawData;
      this.fadeTime = fadeTime;
      rawDataLength = countLines(rawData);
      start();
    }
    
    public void run() {
  
      while(running) {

        try {
          
          // Setting the sentence
          if (rawData != null) {
            long numb = (long) Math.floor(r.nextFloat() * (double) rawDataLength);
            String line = selectLine(numb);
            System.out.printf("\t%s\n", line);
            graphicsPanel.setTextAndRepaint(line);
          }
          
          long fadeTimeCounter = 0;
          while (graphicsPanel.fadeIn()) {
            fadeTimeCounter += fadeTime;
            Thread.sleep(fadeTime);
          }
          
          if ((sleepyTime - (fadeTimeCounter*2)) > 0) {
            Thread.sleep(sleepyTime - (fadeTimeCounter*2));
          }
          
          while (graphicsPanel.fadeOut()) {
            Thread.sleep(fadeTime);
          }
          
        } catch (InterruptedException e) {
          running = false;
        }
      }
      System.out.printf("- Thread Drawing Loop stopped!\n");
    }
    
    public void setSleepyTime(long time) {
      sleepyTime = time;
    }
    
    public void setFadeTime(long time) {
      fadeTime = time;
    }
    
    private String selectLine(long numb) {
      try {
        Scanner s = new Scanner(rawData);
        s.useDelimiter("\\n");
        for(int i = 0;i < numb; i++) {
          s.nextLine();
        }
        return s.nextLine();
      } catch (FileNotFoundException e) {
        System.out.printf("Raw data file could not be found.");
        System.exit(0);
        return null;
      }
    }
    
    // count lines
    private long countLines(File f) {
      if (f != null) {
        try {
          long nr = 0;
          LineNumberReader lnr = new LineNumberReader(new FileReader(f));
          lnr.skip(Long.MAX_VALUE);
          nr = lnr.getLineNumber();
          lnr.close();
          return nr;
        } catch (IOException e) {
          System.out.printf("Error when reading file.");
          System.exit(0);
          return 0;
        }
      } else {
        return 0;
      }
    }
    
  } // end of class
  

  
  
  
  
}