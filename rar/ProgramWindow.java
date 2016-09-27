// (c) 2013 by Roelof Ruis

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import java.awt.event.*;

public class ProgramWindow extends JFrame {
  
  MainPanel mp = new MainPanel();
  DisplayWindow dw = new DisplayWindow();
  
  public ProgramWindow() {
  
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSze = toolkit.getScreenSize();
    final int WIDTH = screenSze.width;
    final int HEIGHT = screenSze.height;
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setTitle("SubScrambler");
    setMinimumSize(new Dimension(400, 300));
    setSize(400,300);
    setLocation(50,50);
    
    add(mp);
    
    setVisible(true);
    
  }
  
      // Main panel
  protected class MainPanel extends JPanel {
  
    FileList fl = new FileList();
    SettingsPane sp = new SettingsPane();
    
    // Constructor
    public MainPanel() {
      
      setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
      JScrollPane fileScrollPane = new JScrollPane(fl);
      fileScrollPane.setBorder(BorderFactory.createTitledBorder("Available Files"));
      sp.setBorder(BorderFactory.createTitledBorder("Settings"));
      
      add(fileScrollPane);
      add(sp);
      
    }
    
    // File List shows the files
    protected class FileList extends JList<File> {
  
      public FileList() {
        
        setBorder(BorderFactory.createLineBorder(Color.black));
        setSize(100,200);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setLayoutOrientation(JList.VERTICAL);
        setVisibleRowCount(-1);
        setListData(getFiles());
        
      }
      
      private Vector<File> getFiles() {
        System.out.printf("Checking available files...\n");
        
        Vector<File> fileVec = new Vector<File>();
        File dataDir = new File("data");
        
        File[] fileArray = dataDir.listFiles();
        for (File f : fileArray) {
          if (f.isFile() && (f.getName().endsWith(".sub") || f.getName().endsWith(".srt"))) {
            fileVec.add(f);
          }
        }
        
        return fileVec;
        
      }
      
    }
    
    // Buttons 
    protected class SettingsPane extends JPanel implements ActionListener {
    
      private final int SPEED_MIN = 100;
      private final int SPEED_MAX = 3000;
      private final int SPEED_INIT = 200;
      
      private final int FADE_MIN = 5;
      private final int FADE_MAX = 100;
      private final int FADE_INIT = 10;
      
      public SettingsPane() {
        
        TitledBorder b;
        
        setLayout(new BorderLayout());
                
        JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, SPEED_MIN, SPEED_MAX, SPEED_INIT);
        speedSlider.addChangeListener(new SentenceTimeChangeListener());
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setMinorTickSpacing(5);
        speedSlider.setPaintTicks(false);
        speedSlider.setPaintLabels(false);
        b = BorderFactory.createTitledBorder("Time between sentences");
        b.setTitleJustification(TitledBorder.CENTER);
        speedSlider.setBorder(b);
                
        JSlider fadeSlider = new JSlider(JSlider.HORIZONTAL, FADE_MIN, FADE_MAX, FADE_INIT);
        fadeSlider.addChangeListener(new FadeTimeChangeListener());
        fadeSlider.setMajorTickSpacing(10);
        fadeSlider.setMinorTickSpacing(1);
        fadeSlider.setPaintTicks(false);
        fadeSlider.setPaintLabels(false);
        b = BorderFactory.createTitledBorder("Fade time");
        b.setTitleJustification(TitledBorder.CENTER);
        fadeSlider.setBorder(b);
        
        
        // Start Button
        JButton buttonStart = new JButton("Start Display");
        buttonStart.addActionListener(this);
        buttonStart.setActionCommand("start");
        
        //add(sliderLabel, BorderLayout.PAGE_START);
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
        centerPanel.add(speedSlider);
        centerPanel.add(fadeSlider);
        
        add(centerPanel, BorderLayout.CENTER);
        add(buttonStart, BorderLayout.PAGE_END);
        
      }
    
      // Button press listener
      public void actionPerformed(ActionEvent e) {
        if ("start".equals(e.getActionCommand())) {
          java.util.List<File> fileList = fl.getSelectedValuesList();
          if (!fileList.isEmpty()) {
            File rawData = extractLines((ArrayList<File>) fileList);
            dw.startDisplay(rawData);
          }
        }
      }
      
      // Listen to the sentence display time change slider
      protected class SentenceTimeChangeListener implements ChangeListener {
         public void stateChanged(ChangeEvent e) {
           JSlider source = (JSlider) e.getSource();
           if (!source.getValueIsAdjusting()) {
            long newVal = (source.getValue() * 10);
            System.out.printf("New time between sentences set to: %.2f seconds\n", ((float) newVal)/1000);
            dw.setSleepyTime(newVal);
          }
        }
      }
      
      // Listen to the fade time change slider
      protected class FadeTimeChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
          JSlider source = (JSlider) e.getSource();
          if (!source.getValueIsAdjusting()) {
            long newVal = source.getValue();
            System.out.printf("New fade time set to: %d milliseconds\n", newVal);
            dw.setFadeTime(newVal);
          }
        }
      }
      
      //extract Lines from files
      private File extractLines(ArrayList<File> fileArray) {
      
        System.out.printf("Creating raw data from selected files...\n");
        try {
          File writeF = new File("data/rawdata.txt");
          FileWriter w = new FileWriter(writeF);
          
          for (File f : fileArray) {
            Scanner s = new Scanner(f);
            s.useDelimiter("\\r\\n");
            String nextStr;
            String fullStr = "";
            
            while (s.hasNext()) {
              nextStr = s.next();
              if (nextStr.length() == 0 && fullStr != "") {
                fullStr = fullStr + "\n";
                w.write(fullStr);
                fullStr = "";
              } else {
                if (!nextStr.matches("[\\d\\[\\]](.*?)")) {
                  if (fullStr == "") {
                    fullStr = nextStr;
                  } else {
                    fullStr = fullStr + " " + nextStr;
                  }
                }
              }
            }
            s.close();
          }
          
          w.close();
          
          return writeF;
        } catch (FileNotFoundException e) {
          System.err.printf("File not found.");
          System.exit(0);
          return null;
        } catch (IOException e) {
          System.err.printf("Unable to read file.");
          System.exit(0);
          return null;
        }
      }
      
    }
    
  }

}