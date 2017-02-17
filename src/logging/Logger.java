package logging;

import java.util.ArrayList;
import java.util.List;

public class Logger {

	private static final Logger instance = new Logger();
	private List messages;
	
	/**
	 * Private constructor to enforce singleton pattern
	 */
	private Logger() {
		messages = new ArrayList();
	}
	/**
	 * Returns the instance of the singleton Logger
	 * @return the instance
	 */
	public static Logger getInstance() {
		return instance;
	}
	/**
	 * Sends all messages to the console in one batch
	 * Flushes the message log after this is done to prepare for a new game
	 */
	public void printMessages() {
		System.out.println("*** Logged Messages ***");
		for(Object msg : messages) {
			System.out.println(((Message) msg).getContent());
		}
		System.out.println("*** Logged Messages End ***");
//		flushMessages();
	}
	public int getMessageCount() {
		return messages.size();
	}
	/**
	 * Adds a message to the messages list
	 * @param m the message to be added to messages
	 */
	public void addMessage(Message m) {
		messages.add(m);
	}
	/**
	 * Empties the messages list of all messages
	 */
	private void flushMessages() {
		messages.clear();
	}
	
}
