package framework;

import framework.DisSemHelper.MessageType;

public class Message{
	public Message(int owner, MessageType messageType, int lc) {
		super();
		this.owner = owner;
		this.messageType = messageType;
		this.lc = lc;
	}
	int owner;
	MessageType messageType;
	int lc;
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("%d,%s,%d",owner,messageType,lc);
	}
	
}