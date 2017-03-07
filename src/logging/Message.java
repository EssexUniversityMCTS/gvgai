package logging;

public class Message {
    // 0 is warning, 1 is error
    private int type;
    // the string content of the message to print
    private String content;

    /**
     * Base constructor for a Message. Needs a type (0 or 1) and a content
     * message
     * 
     * @param type
     *            the type of Message this is, either warning or error
     * @param content
     *            the String contained within a Message. This is printed out
     *            after a game runs ( or fails to run )
     */
    public Message(int type, String content) {
	this.type = type;
	this.content = content;
    }

    /**
     * Returns the numerical type of the Message
     * 
     * @return the type of the Message
     */
    public int getType() {
	return type;
    }

    /**
     * Returns the String content of the Message
     * 
     * @return the content of the Message
     */
    public String getContent() {
	return content;
    }
}
