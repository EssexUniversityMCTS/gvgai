package tools;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowInput extends WindowAdapter{
	
	/**
	 * Variable to indicate if the window is closed
	 */
	public boolean windowClosed;
	
	/**
	 * Constructor to initilize the class
	 */
	public WindowInput(){
		windowClosed = false;
	}
	
	/**
	 * Mark the window as closed
	 */
	@Override
	public void windowClosed(WindowEvent e) {
		super.windowClosed(e);
		windowClosed = true;
	}
}
