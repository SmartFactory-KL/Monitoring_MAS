package i40_messages;

import java.util.UUID;

/**
 * This class is based on the VDI/VDE 2193 part 1 from year 2020
 * @author Alexis Bernhard
 */
public class Frame {

	private UUID messageID;
	
	private Participant sender;
	
	private Participant receiver;
	
	private String msg_type;
	
	private String conversationID;
	
	public Frame(String sender, String receiver, String msg_type) {
		this(sender, receiver, msg_type, null);
	}
	
	public Frame(String sender, String receiver, String msg_type, String conversationID) {
		this(sender, null, receiver, null, msg_type, conversationID);
	}
	
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
