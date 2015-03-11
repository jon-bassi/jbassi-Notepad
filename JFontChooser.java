import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class JFontChooser extends JPanel
{
   JLabel text;
   String[] allFonts;
   String[] allStyles = {"Plain","Italic","Bold","Bold Italic"};
   Integer[] allSizes = {8,10,12,14,18,20};
   JList fontList;
   JComboBox styleList;
   JComboBox sizeList;
   ActionListener listener;
   
   Object[] options = {"Ok","Cancel"};
   
   private String fName;
   private int fStyle;
   private int fSize;
   
   public JFontChooser()
   {
      this.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      this.setPreferredSize(new Dimension(480,300));
      
      // Labels
      JLabel fontLabel = new JLabel("Font:");
      fontLabel.setDisplayedMnemonic('F');
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0.5;
      c.gridx = 0;
      c.gridy = 0;
      c.insets = new Insets(10,10,0,0);
      add(fontLabel,c);
      
      JLabel styleLabel = new JLabel("Font Style:");
      styleLabel.setDisplayedMnemonic('y');
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.5;
      c.gridx = 1;
      c.gridy = 0;
      c.insets = new Insets(10,10,0,0);
      c.anchor = GridBagConstraints.FIRST_LINE_START;
      add(styleLabel,c);
      
      JLabel sizeLabel = new JLabel("Size:");
      sizeLabel.setDisplayedMnemonic('z');
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.5;
      c.gridx = 2;
      c.gridy = 0;
      c.insets = new Insets(10,10,0,0);
      c.anchor = GridBagConstraints.FIRST_LINE_START;
      add(sizeLabel,c);
      
      // List and dropdowns
      allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
      fontList = new JList(allFonts);
      fontLabel.setLabelFor(fontList);
      //sets font of label
      fontList.addListSelectionListener(new ListSelectionListener() {
         public void valueChanged (ListSelectionEvent le) {
             int idx = fontList.getSelectedIndex();
             if (idx != 1)
             {
                fName = allFonts[idx];
                text.setFont(new Font(fName,fStyle,fSize));
             }
         }
     });
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0.5;
      c.weighty = 1;
      c.gridx = 0;
      c.gridy = 1;
      c.ipady = 120;
      c.ipadx = 185;
      c.insets = new Insets(10,10,10,0);
      add(new JScrollPane(fontList),c);
      
      styleList = new JComboBox(allStyles);
      styleLabel.setLabelFor(styleList);
      styleList.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            String style = ((JComboBox)ae.getSource()).getSelectedItem().toString();
            switch (style)
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
            text.setFont(new Font(fName,fStyle,fSize));
         }
      });
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0.5;
      c.weighty = 1;
      c.gridx = 1;
      c.gridy = 1;
      c.ipady = 0;
      c.ipadx = 0;
      c.insets = new Insets(10,10,0,0);
      c.anchor = GridBagConstraints.FIRST_LINE_START;
      add(styleList,c);
      
      sizeList = new JComboBox(allSizes);
      sizeLabel.setLabelFor(sizeList);
      sizeList.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent ae)
         {
            Integer size = (Integer)((JComboBox)ae.getSource()).getSelectedItem();
            fSize = size;
            text.setFont(new Font(fName,fStyle,fSize));
         }
      });
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0.5;
      c.weighty = 1;
      c.gridx = 2;
      c.gridy = 1;
      c.ipadx = 20;
      c.insets = new Insets(10,10,0,75);
      c.anchor = GridBagConstraints.FIRST_LINE_START;
      add(sizeList,c);
      
      // Preview
      JPanel preview = new JPanel(new GridBagLayout());
      text = new JLabel("The quick brown fox jumps over the lazy dog 01234567890");
      text.setFont(new Font(fName,fStyle,fSize));
      TitledBorder prevTitle = new TitledBorder("Preview");
      c.anchor = GridBagConstraints.FIRST_LINE_START;
      c.insets = new Insets(0,20,10,0);
      preview.add(text,c);
      preview.setBorder(prevTitle);
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 0.4;
      c.weighty = .5;
      c.gridx = 0;
      c.gridy = 2;
      c.gridwidth = 3;
      c.ipady = 0;
      c.anchor = GridBagConstraints.CENTER;
      c.insets = new Insets(0,10,0,10);
      add(preview,c);
      
   }
   
   
   public void setDefaultName(String fontName)
   {
      fName = fontName;
      fontList.setSelectedValue(fontName, true);
      text.setFont(new Font(fName,fStyle,fSize));
   }
   
   public void setDefaultStyle(Integer style)
   {
      fStyle = style;
      switch(style)
      {
      case Font.PLAIN : styleList.setSelectedItem("Plain");
         break;
      case Font.BOLD : styleList.setSelectedItem("Bold");
         break;
      case Font.ITALIC : styleList.setSelectedItem("Italic");
         break;
      default : styleList.setSelectedItem("Bold Italic");
      }
      text.setFont(new Font(fName,fStyle,fSize));
   }
   
   public void setDefaultSize(Integer size)
   {
      fSize = size;
      sizeList.setSelectedItem(size);
      text.setFont(new Font(fName,fStyle,fSize));
   }
   
   public String getSelectedFontName()
   {
      return fName;
   }
   
   public int getSelectedFontStyle()
   {
      return fStyle;
   }
   
   public int getSelectedFontSize()
   {
      return fSize;
   }


   public boolean showSelectDialog(JFrame frame)
   {
      int result = JOptionPane.showOptionDialog(frame,this,"Font"
            ,JOptionPane.YES_NO_OPTION,JOptionPane.PLAIN_MESSAGE,null,options,options[0]);
      if (result == 0)
         return true;
      else
         return false;
   }
}
