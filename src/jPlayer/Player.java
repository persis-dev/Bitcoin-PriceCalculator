package jPlayer;
import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.swing.SwingTerminal;

import java.awt.HeadlessException;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class Player
{
  static SwingTerminal term=new SwingTerminal();
  static Screen screen = TerminalFacade.createScreen(term);
  
  public static void main(String[] args)
    throws InterruptedException, NumberFormatException, IOException, HeadlessException, UnsupportedFlavorException
  {
	Primary.start();
    GUIScreen gui = new GUIScreen(screen);
    MainWindow mainWindow = new MainWindow();
    screen.startScreen();
	term.getJFrame().addWindowListener(new java.awt.event.WindowAdapter() {
	    public void windowClosing(java.awt.event.WindowEvent evt) {
	        screen.stopScreen();
	      }
	});
    Thread updater = new Thread(mainWindow);
    updater.start();
    gui.showWindow(mainWindow, GUIScreen.Position.FULL_SCREEN);
  }
  
  public static void print(String s, int delay)
    throws InterruptedException
  {
    char[] arrayOfChar;
    int j = (arrayOfChar = s.toCharArray()).length;
    for (int i = 0; i < j; i++)
    {
      char c = arrayOfChar[i];
      screen.getTerminal().putCharacter(c);
      Thread.sleep(delay);
    }
  }
}
