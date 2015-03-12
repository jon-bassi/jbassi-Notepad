import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.*;
import javax.swing.undo.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.util.*;

public class JNotepad implements ActionListener
{
   String filename = "Untitled";
   String filepath = "";
   String oldfile = "";
   boolean edited = false; // check if there's any text before closing
   JFrame frame;
   JTextArea pad;
   JCheckBoxMenuItem wordWrap;
   JCheckBoxMenuItem statusBar;
   int statusRow = 1;
   int statusCol = 1;
   String clipboard = "";
   UndoManager undoManager = new UndoManager();
   GridBagConstraints c = new GridBagConstraints();
   JPanel statusPane;
   JLabel status;
   JFileChooser jfc;
   FileFilter filter = new FileNameExtensionFilter("*.txt or *.java", "txt","java");
   
   public JNotepad(int height, int width, int wrap, Font fontType) 
   {
      frame = new JFrame(filename + " - JNotepad");
      frame.setLayout(new GridBagLayout());
      frame.setSize(width,height);
      frame.setLocationRelativeTo(null);
      frame.setIconImage(new ImageIcon("JNotepad.png").getImage());
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      frame.addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent we)
         {
            if(!jNotepadExit())
            {
               int wordWrap = 0;
               if (pad.getLineWrap())
                  wordWrap = 1;
               String iniData = "j. bassi JNotepad (c) 2015\nDefaultHeight:"
                     + frame.getHeight() + "\nDefaultWidth:" + frame.getWidth()
                     + "\nDefaultFontName:" + pad.getFont().getFontName()
                     + "\nDefaultFontStyle:" + pad.getFont().getStyle()
                     + "\nDefaultFontSize:" + pad.getFont().getSize()
                     + "\nWordWrap:" + wordWrap;
               try
               {
                  FileWriter writeToFile = new FileWriter(new File("JNotepad.ini"));
                  writeToFile.write(iniData);
                  writeToFile.close();
               }
               catch (IOException e)
               {
                  e.printStackTrace();
                  System.exit(0);
               }
               frame.dispose();
            }
            else
               return;
         }
      });
      
      try
      {
         clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
         .getData(DataFlavor.stringFlavor).toString();
      } catch (HeadlessException e)
      {
         e.printStackTrace();
      } catch (UnsupportedFlavorException e)
      {
         // happens when something other than text has been copied - ignore
      } catch (IOException e)
      {
         e.printStackTrace();
      }
      
      pad = new JTextArea();
      pad.setAlignmentX(JTextField.TOP_ALIGNMENT);
      if (wrap == 1)
         pad.setLineWrap(true);
      pad.setFont(fontType);
      pad.addMouseListener(new MouseListener());
      pad.getDocument().addUndoableEditListener(undoManager);
      
      // Menu
      JMenuBar menu = new JMenuBar();
      
      // file
      JMenu file = new JMenu("File");
      file.setMnemonic(KeyEvent.VK_F);
      
      JMenuItem newFile = new JMenuItem("New");
      newFile.addActionListener(this);
      newFile.setMnemonic(KeyEvent.VK_N);
      newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,Event.CTRL_MASK));
      
      JMenuItem open = new JMenuItem("Open...");
      open.addActionListener(this);
      open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,Event.CTRL_MASK));
      
      JMenuItem save = new JMenuItem("Save");
      save.addActionListener(this);
      save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,Event.CTRL_MASK));
      
      JMenuItem saveAs = new JMenuItem("Save As...");
      saveAs.addActionListener(this);
      
      JMenuItem pageSetup = new JMenuItem("Page Setup");
      pageSetup.addActionListener(this);
      pageSetup.setMnemonic(KeyEvent.VK_U);
      
      JMenuItem print = new JMenuItem("Print...");
      print.addActionListener(this);
      print.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,Event.CTRL_MASK));
      
      JMenuItem exit = new JMenuItem("Exit");
      exit.addActionListener(this);
      exit.setMnemonic(KeyEvent.VK_X);
      
      file.add(newFile);
      file.add(open);
      file.add(save);
      file.add(saveAs);
      file.addSeparator();
      file.add(pageSetup);
      file.add(print);
      file.addSeparator();
      file.add(exit);
      menu.add(file);
      
      // edit
      JMenu edit = new JMenu("Edit");
      edit.setMnemonic(KeyEvent.VK_E);
      
      JMenuItem undo = new JMenuItem("Undo");
      undo.addActionListener(this);
      undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,Event.CTRL_MASK));
      
      JMenuItem redo = new JMenuItem("Redo");
      redo.addActionListener(this);
      redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,Event.CTRL_MASK));
      
      JMenuItem cut = new JMenuItem("Cut");
      cut.addActionListener(this);
      cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,Event.CTRL_MASK));
      cut.setEnabled(false);
      
      JMenuItem copy = new JMenuItem("Copy");
      copy.addActionListener(this);
      copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,Event.CTRL_MASK));
      copy.setEnabled(false);
      
      JMenuItem paste = new JMenuItem("Paste");
      paste.addActionListener(this);
      paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,Event.CTRL_MASK));
      
      JMenuItem delete = new JMenuItem("Delete");
      delete.addActionListener(this);
      delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0));
      delete.setEnabled(false);
      
      JMenuItem find = new JMenuItem("Find...");
      find.addActionListener(this);
      find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,Event.CTRL_MASK));
      
      JMenuItem findNext = new JMenuItem("Find Next");
      findNext.addActionListener(this);
      
      JMenuItem replace = new JMenuItem("Replace...");
      replace.addActionListener(this);
      replace.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,Event.CTRL_MASK));
      
      JMenuItem goTo = new JMenuItem("Go To...");
      goTo.addActionListener(this);
      goTo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,Event.CTRL_MASK));
      
      JMenuItem selectAll = new JMenuItem("Select All");
      selectAll.addActionListener(this);
      selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,Event.CTRL_MASK));
      
      JMenuItem timeDate = new JMenuItem("Time/Date");
      timeDate.addActionListener(this);
      timeDate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
      
      edit.add(undo);
      edit.add(redo);
      edit.addSeparator();
      edit.add(cut);
      edit.add(copy);
      edit.add(paste);
      edit.add(delete);
      edit.addSeparator();
      edit.add(find);
      edit.add(findNext);
      edit.add(replace);
      edit.add(goTo);
      edit.addSeparator();
      edit.add(selectAll);
      edit.add(timeDate);
      menu.add(edit);
      
      pad.addCaretListener(new CaretListener()
      {
         public void caretUpdate(CaretEvent ce)
         {
            int caretPos = pad.getCaretPosition();
            
            try
            {
               statusRow = pad.getLineOfOffset(caretPos);
               statusCol = caretPos - pad.getLineStartOffset(statusRow);
               statusCol++;
               statusRow++;
               status.setText("Ln " + statusRow + ", " + "Col " + statusCol);
            } catch (BadLocationException e)
            {
            }
            
            int length = pad.getSelectionEnd() - pad.getSelectionStart();
            if (length < 1)
            {
               cut.setEnabled(false);
               copy.setEnabled(false);
               delete.setEnabled(false);
            }
            else
            {
               cut.setEnabled(true);
               copy.setEnabled(true);
               delete.setEnabled(true);
            }
         }
      });
      
      
      // format
      JMenu format = new JMenu("Format");
      format.setMnemonic(KeyEvent.VK_O);
      if (wrap == 1)
         wordWrap = new JCheckBoxMenuItem("Word Wrap",true);
      else
         wordWrap = new JCheckBoxMenuItem("Word Wrap",false);
      wordWrap.addActionListener(this);
      wordWrap.setMnemonic(KeyEvent.VK_W);
      JMenuItem font = new JMenuItem("Font...");
      font.addActionListener(this);
      font.setMnemonic(KeyEvent.VK_F);
      format.add(wordWrap);
      format.add(font);
      menu.add(format);
      
      // view
      JMenu view = new JMenu("View");
      view.setMnemonic(KeyEvent.VK_V);
      statusBar = new JCheckBoxMenuItem("Status Bar",false);
      if (!wordWrap.getState())
         statusBar.setEnabled(true);
      else
         statusBar.setEnabled(false);
      statusBar.setMnemonic(KeyEvent.VK_S);
      statusBar.addActionListener(this);
      view.add(statusBar);
      menu.add(view);
      
      // help
      JMenu help = new JMenu("Help");
      help.setMnemonic(KeyEvent.VK_H);
      JMenuItem viewHelp = new JMenuItem("View Help");
      viewHelp.setMnemonic(KeyEvent.VK_H);
      viewHelp.setEnabled(false);                                    //enable
      JMenuItem about = new JMenuItem("About JNotepad");
      about.addActionListener(this);
      help.add(viewHelp);
      help.addSeparator();
      help.add(about);
      menu.add(help);
      
      frame.setJMenuBar(menu);
      JScrollPane pane = new JScrollPane(pad);
      pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 1;
      c.weighty = 0.995;
      c.fill = GridBagConstraints.BOTH;
      frame.add(pane,c);
      
      status = new JLabel("Ln " + statusRow + ", " + "Col " + statusCol);
      statusPane = new JPanel(new BorderLayout());
      statusPane.add(status,BorderLayout.EAST);
      frame.setVisible(true);
   }
   
   /*******************
    *  TODO: Actions  *
    *******************/
   public void actionPerformed(ActionEvent ae)
   {
      String command = ae.getActionCommand();
      PrinterJob pj;
      PageFormat pf;
      String s;
      StringSelection ss;
      Clipboard cb;
      int returnval;
      
      switch (command)
      {
      // file
      case "New" :
         try
         {
            if (!jNotepadExit())
            {
               this.frame.dispose();
               JNotepad.main(null);
            } 
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      break;
      case "Open..." :
         edited = false;
         if (!pad.getText().equals(""))
            edited = true;
         if (oldfile.equals(pad.getText()))
            edited = false;
         if (edited)
         {
            jNotepadExit();
         }
         jfc = new JFileChooser();
         jfc.setFileFilter(filter);
         returnval = jfc.showOpenDialog(frame);
         if (returnval == JFileChooser.APPROVE_OPTION)
         {
            File file = jfc.getSelectedFile();
            filename = file.getName();
            filepath = file.getAbsolutePath();
            frame.setTitle(filename + " - JNotepad");
            oldfile = "";
            try
            {
               Scanner read = new Scanner(file);
               while (read.hasNextLine())
               {
                  oldfile += read.nextLine() + "\n";
               }
               read.close();
            } catch (FileNotFoundException e)
            {
            }
            pad.setText(oldfile);
            frame.revalidate();
         }
         break;
      case "Save" :
         if (filename.equals("Untitled"))
         {
            jfc = new JFileChooser();
            returnval = jfc.showSaveDialog(frame);
            if (returnval == JFileChooser.APPROVE_OPTION)
            {
               File file = jfc.getSelectedFile();
               try
               {
                  FileWriter write = new FileWriter(file);
                  write.write(pad.getText());
                  write.close();
                  edited = false;
                  filename = file.getName();
                  filepath = file.getAbsolutePath();
                  frame.setTitle(filename + " - JNotepad");
               } catch (IOException e)
               {
               }
            }
         }
         else
         {
            File file = new File(filepath);
            try
            {
               FileWriter write = new FileWriter(file);
               write.write(pad.getText());
               write.close();
               edited = false;
            } catch (IOException e)
            {
            }
            
         }
         break;
      case "Save As..." :
         jfc = new JFileChooser();
         returnval = jfc.showSaveDialog(frame);
         if (returnval == JFileChooser.APPROVE_OPTION)
         {
            File file = jfc.getSelectedFile();
            try
            {
               FileWriter write = new FileWriter(file);
               write.write(pad.getText());
               write.close();
               edited = false;
               filename = file.getName();
               filepath = file.getAbsolutePath();
               frame.setTitle(filename + " - JNotepad");
            } catch (IOException e)
            {
            }
         }
         break;
      case "Page Setup" :
         pj = PrinterJob.getPrinterJob();
         pf = pj.pageDialog(pj.defaultPage());
         break;
      case "Print..." :
         pj = PrinterJob.getPrinterJob();
         if (pj.printDialog())
         {
            try
            {
               pj.print();
            }
            catch (PrinterException e)
            {
               e.printStackTrace();
               System.out.println(e);
            }
         }
         break;
      case "Exit" :
         if(!jNotepadExit())
            this.frame.dispose();
         break;
      // edit
      case "Undo" :
         if (undoManager.canUndo())
            undoManager.undo();
         break;
      case "Redo" :
         if (undoManager.canRedo())
            undoManager.redo();
         break;
      case "Cut" :
         s = pad.getSelectedText();
         ss = new StringSelection(s);
         cb = Toolkit.getDefaultToolkit().getSystemClipboard();
         cb.setContents(ss, ss);
         pad.replaceSelection("");
         break;
      case "Copy" :
         s = pad.getSelectedText();
         ss = new StringSelection(s);
         cb = Toolkit.getDefaultToolkit().getSystemClipboard();
         cb.setContents(ss, ss);
         break;
      case "Paste" :
         cb = Toolkit.getDefaultToolkit().getSystemClipboard();
         try
         {
            pad.replaceSelection(cb.getData(DataFlavor.stringFlavor).toString());
         } catch (UnsupportedFlavorException e)
         {
            e.printStackTrace();
         } catch (IOException e)
         {
            e.printStackTrace();
         }
         break;
      case "Delete" :
         pad.replaceSelection("");
         break;
         
         // todo find and find next
         
      case "Select All" :
         pad.selectAll();
         break;
      case "Time/Date" :
         Calendar cal = Calendar.getInstance();
         String ampm = "AM";
         if (cal.get(Calendar.AM_PM) == 1)
            ampm = "PM";
         pad.setText(pad.getText() + cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE)
               + " " + ampm + " " + (cal.get(Calendar.MONTH) + 1) + "/"
               + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR));
         break;
      // format
      case "Word Wrap" :
         if (pad.getLineWrap())
         {
            wordWrap.setSelected(false);
            pad.setLineWrap(false);
            statusBar.setEnabled(true);
            if (statusBar.isSelected())
            {
               c.gridx = 0;
               c.gridy = 1;
               c.weightx = 1;
               c.weighty = 0.001;
               c.insets = new Insets(0,0,0,5);
               frame.add(statusPane,c);
               frame.revalidate();
            }
         }
         else
         {
            wordWrap.setSelected(true);
            pad.setLineWrap(true);
            statusBar.setEnabled(false);
            if (statusBar.isSelected())
            {
               frame.remove(statusPane);
               frame.revalidate();
            }
         }
         break;
      case "Font..." :
         JFontChooser jfc = new JFontChooser();
         Font font = pad.getFont();
         jfc.setDefaultName(font.getFontName());
         jfc.setDefaultStyle(font.getStyle());
         jfc.setDefaultSize(font.getSize());
         boolean result = jfc.showSelectDialog(frame);
         if (result)
         {
            pad.setFont(new Font(jfc.getSelectedFontName(),jfc.getSelectedFontStyle()
                     ,jfc.getSelectedFontSize()));
         }
         break;
      // view
      case "Status Bar" :
         if (!statusBar.isSelected())
         {
            statusBar.setSelected(false);
            frame.remove(statusPane);
            frame.revalidate();
         }
         else
         {
            statusBar.setSelected(true);
            c.gridx = 0;
            c.gridy = 1;
            c.weightx = 1;
            c.weighty = 0.001;
            c.insets = new Insets(0,0,0,5);
            frame.add(statusPane,c);
            frame.revalidate();
         }
         break;
      // help
      case "About JNotepad" :
         JOptionPane.showMessageDialog(frame,
         "JNotepad version 0.1\n(c) 2015 j. bassi");
         break;
      }
   }
   
   /**
    * brings up save dialog
    * @return true if cancel is pressed
    */
   private boolean jNotepadExit()
   {
      // check if there's text
      edited = false;
      if (!pad.getText().equals(""))
         edited = true;
      if (oldfile.equals(pad.getText()))
         edited = false;
      int choice = 1;
      if(edited)
      {
         Object[] options = {"Save","Don't Save","Cancel"};
         choice = JOptionPane.showOptionDialog(frame, "Do you want to save changes to " + filename + "?"
               , "JNotepad",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE
               ,null,options, options[0]);
      }
      if (choice == 0)
      {
         jfc = new JFileChooser();
         int returnval = jfc.showSaveDialog(frame);
         if (returnval == JFileChooser.APPROVE_OPTION)
         {
            File file = jfc.getSelectedFile();
            try
            {
               FileWriter write = new FileWriter(file);
               write.write(pad.getText());
               write.close();
               edited = false;
               filename = file.getName();
               filepath = file.getAbsolutePath();
               frame.setTitle(filename + " - JNotepad");
            } catch (IOException e)
            {
            }
         }
         return false;
      }
      if (choice == 1)
         return false;
      return true;
   }
   
   private static void createFile(File iniFile) throws IOException
   {
      iniFile.createNewFile();
      String iniData = "j. bassi JNotepad (c) 2015\nDefaultHeight:600"
            + "\nDefaultWidth:600\nDefaultFontName:Consolas"
            + "\nDefaultFontStyle:Plain\nDefaultFontSize:12\nWordWrap:0";
      FileWriter writeToFile = new FileWriter(iniFile);
      writeToFile.write(iniData);
      writeToFile.close();
   }
   
   
   
   
   public static void main(String[] args) throws IOException
   {
      if (args != null && args.length > 0)
      {
         
      }
      
      
      
      
      File iniFile = new File("JNotepad.ini");
      if (!iniFile.exists())
         createFile(iniFile);
      try
      {
         Scanner readFromFile = new Scanner(iniFile);
         readFromFile.nextLine();
         int height = Integer.parseInt(readFromFile.nextLine().substring(14));
         int width = Integer.parseInt(readFromFile.nextLine().substring(13));
         String fontName = readFromFile.nextLine().substring(16);
         String fontStyle = readFromFile.nextLine().substring(17);
         int fontSize = Integer.parseInt(readFromFile.nextLine().substring(16));
         int wrap = Integer.parseInt(readFromFile.nextLine().substring(9));
         readFromFile.close();
         int fStyle = 0;
         switch (fontStyle)
         {
         case "Plain" : fStyle = Font.PLAIN;
            break;
         case "Italic" : fStyle = Font.ITALIC;
            break;
         case "Bold" : fStyle = Font.BOLD;
            break;
         case "Bold Italic" : fStyle = (Font.BOLD | Font.ITALIC);
            break;
         }
         Font font = new Font(fontName,fStyle,fontSize);
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
                  new JNotepad(height,width,wrap,font);
            }
         });
      }
      catch (Exception e)
      {
         // .ini file corrupted - remake
         createFile(iniFile);
         SwingUtilities.invokeLater(new Runnable()
         {
            public void run()
            {
               new JNotepad(600,600,0,new Font("Consolas",Font.PLAIN,12));
            }
         });
      }
      
   }
   
   class RightClickMenu extends JPopupMenu
   {
      JMenuItem anItem;
      public RightClickMenu()
      {
         JMenuItem undo = new JMenuItem("Undo");
         undo.addActionListener(JNotepad.this);
         
         JMenuItem cut = new JMenuItem("Cut");
         cut.addActionListener(JNotepad.this);
         if (pad.getSelectedText() == null)
            cut.setEnabled(false);
         
         JMenuItem copy = new JMenuItem("Copy");
         copy.addActionListener(JNotepad.this);
         if (pad.getSelectedText() == null)
            copy.setEnabled(false);
         
         JMenuItem paste = new JMenuItem("Paste");
         paste.addActionListener(JNotepad.this);
         if (clipboard.equals(""))
            paste.setEnabled(false);
         
         JMenuItem delete = new JMenuItem("Delete");
         delete.addActionListener(JNotepad.this);
         if (pad.getSelectedText() == null)
            delete.setEnabled(false);
         
         JMenuItem selectAll = new JMenuItem("Select All");
         selectAll.addActionListener(JNotepad.this);
         
         add(undo);
         addSeparator();
         add(cut);
         add(copy);
         add(paste);
         add(delete);
         addSeparator();
         add(selectAll);
      }
  }
   class MouseListener extends MouseAdapter
   {
      public void mousePressed(MouseEvent me)
      {
          if (me.isPopupTrigger())
              mouseAction(me);
      }
      
      public void mouseReleased(MouseEvent me)
      {
          if (me.isPopupTrigger())
              mouseAction(me);
      }
      
      private void mouseAction(MouseEvent me)
      {
          RightClickMenu menu = new RightClickMenu();
          menu.show(me.getComponent(), me.getX(), me.getY());
      }
   }
}
