package sun.applet;

import java.awt.Button;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

class TextFrame extends Frame
{
  private static AppletMessageHandler amh = new AppletMessageHandler("textframe");

  TextFrame(int paramInt1, int paramInt2, String paramString1, String paramString2)
  {
    setTitle(paramString1);
    TextArea localTextArea = new TextArea(20, 60);
    localTextArea.setText(paramString2);
    localTextArea.setEditable(false);
    add("Center", localTextArea);
    Panel localPanel = new Panel();
    add("South", localPanel);
    Button localButton = new Button(amh.getMessage("button.dismiss", "Dismiss"));
    localPanel.add(localButton);
    localButton.addActionListener(new ActionListener(this)
    {
      public void actionPerformed()
      {
        this.this$0.dispose();
      }
    });
    pack();
    move(paramInt1, paramInt2);
    setVisible(true);
    1 local1 = new WindowAdapter(this)
    {
      public void windowClosing()
      {
        this.this$0.dispose();
      }
    };
    addWindowListener(local1);
  }
}