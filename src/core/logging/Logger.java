package core.logging;

import java.util.ArrayList;

public class Logger {

    private static final Logger instance = new Logger();
    private ArrayList<Message> messages;

    /**
     * Private constructor to enforce singleton pattern
     */
    private Logger() {
	messages = new ArrayList<Message>();
    }

    /**
     * Returns the instance of the singleton Logger
     * 
     * @return the instance
     */
    public static Logger getInstance() {
	return instance;
    }

    /**
     * Returns the list of errors and warnings
     * 
     * @return list of errors and warnings
     */
    public ArrayList<Message> getMessages(){
	return this.messages;
    }
    
    /**
     * Sends all messages to the console in one batch Flushes the message log
     * after this is done to prepare for a new game
     */
    public void printMessages() {
	System.out.println("*** Logged Messages ***");
	for (Object msg : messages) {
	    System.out.println(((Message) msg).getContent());
	}
	System.out.println("*** Logged Messages End ***");
    }

    /**
     * Get the number of error and warning messages combined
     * @return 	number of error and warning messages
     */
    public int getMessageCount() {
	return messages.size();
    }
    
    /**
     * Get either error or warning messages
     * @param type 0 warnings, 1 errors
     * @return	number of errors or warnings
     */
    public int getMessageCount(int type) {
	int result = 0;
	for (int i=0; i<messages.size(); i++){
	    if(messages.get(i).getType() == type){
		result += 1;
	    }
	}
	return result;
    }

    /**
     * Adds a message to the messages list
     * 
     * @param m
     *            the message to be added to messages
     */
    public void addMessage(Message m) {
	messages.add(m);
    }

    /**
     * Empties the messages list of all messages
     */
    public void flushMessages() {
	messages.clear();
    }

}
