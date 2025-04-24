package i40_messages;

import java.util.UUID;

/**
 * This class is based on the VDI/VDE 2193 part 1 from year 2020. The purpose of the frame is to identify the type or purpose of a message or interaction protocol,
 * identify sender and receiver of a message and name message references such as those usual in business correspondence. 
 * @author Alexis Bernhard
 */
public class Frame {

	/**
	 * The unique identifier of the message to be transmitted as JAVA UUID.
	 */
	private UUID messageID;
	
	/**
	 * Contains the sender of the message, containing a aas_id and a role. 
	 */
	private Participant sender;
	
	/**
	 * Contains the receiver of the message, containing a aas_id and a role. 
	 */
	private Participant receiver;
	
	/**
	 * The message element “type” designates the purpose of intention of a message, for exam-ple “call for proposals” (using a message with the purpose “call for proposal”).
	 */
	private String msg_type;
	
	/**
	 * The conversation identifier. This id stays valid until the own of a conversation.
	 */
	private String conversationID;
	
	/**
	 * Initializes a message frame with a minimal set of required attributes without participant roles and conversation id. 
	 * @param sender the sender of the message
	 * @param receiver the receiver of the message
	 * @param msg_type designates the purpose of intention of a message
	 */
	public Frame(String sender, String receiver, String msg_type) {
		this(sender, receiver, msg_type, null);
	}
	
	/**
	 * 
	 * Initializes a message frame with a set of required attributes without participant roles. 
	 * @param sender the sender of the message
	 * @param receiver the receiver of the message
	 * @param msg_type designates the purpose of intention of a message
	 * @param conversationID The conversation identifier. This id stays valid until the own of a conversation.
	 */
	public Frame(String sender, String receiver, String msg_type, String conversationID) {
		this(sender, null, receiver, null, msg_type, conversationID);
	}
	
	/**
	 * Initializes a message frame with a set of different attributes. 
	 * @param sender the sender of the message
	 * @param senderRole Contains the name and the type (e.g. object) of the sender 
	 * @param receiver Contains the receiver of the message
	 * @param receiverRole Contains the name and the type (e.g. object) of the receiver 
	 * @param msg_type designates the purpose of intention of a message
	 * @param conversationID, the conversation identifier. This id stays valid until the own of a conversation.
	 */
	public Frame(String sender, String senderRole, String receiver, String receiverRole, String msg_type, String conversationID) {
		this.sender = new Participant(sender, senderRole);
		this.receiver = new Participant(receiver, receiverRole);
		this.setMsg_type(msg_type);
		this.setConversationID(conversationID);
	}

	public UUID getMessageID() {
		return messageID;
	}

	public String getSender() {
		return sender.get_id();
	}

	public void setSender(String sender) {
		this.sender.set_id(sender);
	}
	
	public String getSenderRole() {
		return this.sender.getRole();
	}

	public String getReceiver() {
		return receiver.get_id();
	}

	public void setReceiver(String receiver) {
		this.receiver.set_id(receiver);
	}
	
	public String getReceiverRole() {
		return this.receiver.getRole();
	}

	public String getMsg_type() {
		return msg_type;
	}

	public void setMsg_type(String msg_type) {
		this.msg_type = msg_type;
	}

	public String getConversationID() {
		return conversationID;
	}

	public void setConversationID(String conversationID) {
		this.conversationID = conversationID;
	}
	
}
