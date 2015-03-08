import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.util.Calendar;
import java.util.Scanner;

public class Notepad implements ActionListener
{
   String filename = "Untitled";
   boolean edited = false; // check if there's any text before closing
   JFrame frame;
   JTextArea pad;
   JCheckBoxMenuItem wordWrap;
   JMenuItem statusBar;
   String clipboard = "";
   
   public Notepad(int height, int width, int wrap, Font fontType) 
   {
      frame = new JFrame(filename + " - Notepad");
      frame.setSize(width,height);
      frame.setLocationRelativeTo(null);
      frame.setIconImage(new ImageIcon("Notepad.png").getImage());
      frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
      frame.addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent we)
         {
            if(!NotepadExit())
            {
               int wordWrap = 0;
               if (pad.getLineWrap())
                  wordWrap = 1;
               String iniData = "j. bassi Notepad (c) 2015\nDefaultHeight:"
                     + frame.getHeight() + "\nDefaultWidth:" + frame.getWidth()
                     + "\nDefaultFontName:" + pad.getFont().getFontName()
                     + "\nDefaultFontStyle:" + pad.getFont().getStyle()
                     + "\nDefaultFontSize:" + pad.getFont().getSize()
                     + "\nWordWrap:" + wordWrap;
               try
               {
                  FileWriter writeToFile = new FileWriter(new File("Notepad.ini"));
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
      
      // caret listener for edit events (need for status bar?)
      pad.addCaretListener(new CaretListener()
      {
         public void caretUpdate(CaretEvent ce)
         {
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
      statusBar = new JMenuItem("Status Bar");
      if (!wordWrap.getState())
         statusBar.setEnabled(true);
      else
         statusBar.setEnabled(false);                                //enable
      statusBar.setMnemonic(KeyEvent.VK_S);
      view.add(statusBar);
      menu.add(view);
      
      // help
      JMenu help = new JMenu("Help");
      help.setMnemonic(KeyEvent.VK_H);
      JMenuItem viewHelp = new JMenuItem("View Help");
      viewHelp.setMnemonic(KeyEvent.VK_H);
      viewHelp.setEnabled(false);                                    //enable
      JMenuItem about = new JMenuItem("About Notepad");
      about.addActionListener(this);
      help.add(viewHelp);
      help.addSeparator();
      help.add(about);
      menu.add(help);
      
      
      frame.setJMenuBar(menu);
      JScrollPane pane = new JScrollPane(pad);
      pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      frame.add(pane);
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
      
      switch (command)
      {
      // file
      case "New" :
         try
         {
            if (!NotepadExit())
            {
               this.frame.dispose();
               Notepad.main(null);
            } 
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
      break;
      
      case "Page Setup" :
         pj = PrinterJob.getPrinterJob();
         pf = pj.pageDialog(pj.defaultPage());
         String s;
         StringSelection ss;
         Clipboard cb;
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
         if(!NotepadExit())
            this.frame.dispose();
         break;
         
         
      // edit
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
         
         
         
      case "Select All" :
         pad.selectAll();
         break;
      case "Time/Date" :
         Calendar c = Calendar.getInstance();
         String ampm = "AM";
         if (c.get(Calendar.AM_PM) == 1)
            ampm = "PM";
         pad.setText(pad.getText() + c.get(Calendar.HOUR) + ":" + c.get(Calendar.MINUTE)
               + " " + ampm + " " + (c.get(Calendar.MONTH) + 1) + "/"
               + c.get(Calendar.DAY_OF_MONTH) + "/" + c.get(Calendar.YEAR));
         break;
      // format
      case "Word Wrap" :
         if (pad.getLineWrap())
         {
            wordWrap.setSelected(false);
            pad.setLineWrap(false);
            statusBar.setEnabled(true);
         }
         else
         {
            wordWrap.setSelected(true);
            pad.setLineWrap(true);
            statusBar.setEnabled(false);
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
      // help
      case "About Notepad" :
         JOptionPane.showMessageDialog(frame,
         "Notepad version 0.1\n(c) 2015 j. bassi");
         break;
      }
   }
   
   /**
    * brings up save dialog
    * @return true if cancel is pressed
    */
   private boolean NotepadExit()
   {
      // check if there's text
      edited = false;
      if (!pad.getText().equals(""))
         edited = true;
      int choice = 0;
      if(edited)
      {
         Object[] options = {"Save","Don't Save","Cancel"};
         choice = JOptionPane.showOptionDialog(frame, "Do you want to save changes to " + filename + "?"
               , "Notepad",JOptionPane.YES_NO_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE
               ,null,options, options[0]);
      }
      if (choice == 0)
      {
         // TODO: JFileChooser here
         return false;
      }
      if (choice == 1)
         return false;
      return true;
   }
   
   private static void createFile(File iniFile) throws IOException
   {
      iniFile.createNewFile();
      String iniData = "j. bassi Notepad (c) 2015\nDefaultHeight:600"
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
      
      
      
      
      File iniFile = new File("Notepad.ini");
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
                  new Notepad(height,width,wrap,font);
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
               new Notepad(600,600,0,new Font("Consolas",Font.PLAIN,12));
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
         undo.addActionListener(Notepad.this);
         
         JMenuItem cut = new JMenuItem("Cut");
         cut.addActionListener(Notepad.this);
         if (pad.getSelectedText() == null)
            cut.setEnabled(false);
         
         JMenuItem copy = new JMenuItem("Copy");
         copy.addActionListener(Notepad.this);
         if (pad.getSelectedText() == null)
            copy.setEnabled(false);
         
         JMenuItem paste = new JMenuItem("Paste");
         paste.addActionListener(Notepad.this);
         if (clipboard.equals(""))
            paste.setEnabled(false);
         
         JMenuItem delete = new JMenuItem("Delete");
         delete.addActionListener(Notepad.this);
         if (pad.getSelectedText() == null)
            delete.setEnabled(false);
         
         JMenuItem selectAll = new JMenuItem("Select All");
         selectAll.addActionListener(Notepad.this);
         
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
