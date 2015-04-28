package framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import framework.DisSemHelper.MessageType;

class MessageParser implements Runnable
	{
		/**
		 * 
		 */
		private final DisSemHelper disSemHelper;
		public MessageParser(DisSemHelper disSemHelper) {
			super();
			this.disSemHelper = disSemHelper;
		}
		@Override
		public void run() {
			Message recievedMessage;
			while(true)
			{
				//If i am user connection maker, make the connection
				
				//now just start reading and responding to messages
				while(true)
				{
					//read a message from  network
					try{
						recievedMessage=this.disSemHelper.readAPacket();
						int owner=recievedMessage.owner;
						int ts=recievedMessage.lc;
						System.out.printf("%d read message %s\n",this.disSemHelper.helperIndex,recievedMessage.toString());
						MessageType messageType=recievedMessage.messageType;
						
						
						synchronized (this.disSemHelper.mq) 
						{
							this.disSemHelper.lc=Math.max(this.disSemHelper.lc, ts+1);
							this.disSemHelper.lc++;
							if(messageType==MessageType.REQP)
							{
								if(owner!=this.disSemHelper.helperIndex) continue;
								Message message=new Message(this.disSemHelper.helperIndex, MessageType.POP, this.disSemHelper.lc);
								this.disSemHelper.sendAll(message);
								this.disSemHelper.lc++;
//								insert(message);//since own pop are not recieved
//								sendAll(new Message(helperIndex, MessageType.ACK, lc));
//								//own vop recieved and acked by self
//								lc++;
							}
							else if(messageType==MessageType.REQV)
							{
								if(owner!=this.disSemHelper.helperIndex) continue;
								Message message=new Message(this.disSemHelper.helperIndex, MessageType.VOP, this.disSemHelper.lc);
								this.disSemHelper.sendAll(message);
								this.disSemHelper.lc++;
//								insert(message); //since own vop are not recieved
//								sendAll(new Message(helperIndex, MessageType.ACK, lc));
//								lc++;
							}
							else if(messageType==MessageType.POP||messageType==MessageType.VOP)
							{
								this.disSemHelper.insert(recievedMessage);
								this.disSemHelper.sendAll(new Message(this.disSemHelper.helperIndex, MessageType.ACK, this.disSemHelper.lc));
								this.disSemHelper.lc++;
							}
							else if(messageType==MessageType.ACK)
							{
								this.disSemHelper.acknowledgedTimeStamps[owner]=Math.max(this.disSemHelper.acknowledgedTimeStamps[owner], ts);
//								//since own acks are never recieved
//								acknowledgedTimeStamps[helperIndex]=lc;
								//find min acknowledge time stamp
								int minACK=Collections.min(Arrays.asList(this.disSemHelper.acknowledgedTimeStamps));
								
								//any vop messages older than that can be acknowledged simaltaneously reducing s
								ArrayList<Message> m=new ArrayList<Message>(this.disSemHelper.mq);
								for(Message message:m)
								{
									if(message.messageType==MessageType.VOP)
									{
										if(message.lc<=minACK)
										{
											System.out.printf("%d Acknowledged  new message %s\n",this.disSemHelper.helperIndex,message.toString());
											this.disSemHelper.mq.remove(message);
											this.disSemHelper.s++;
										}
										else break;//other messages have to be older than this
									}
									
								}
								m=new ArrayList<Message>(this.disSemHelper.mq);
								for(Message mess:m)
								{
									if(this.disSemHelper.s<=0) {
										break;
									} // no use continuing we need atleast another vop before can issue go ahead
									if(mess.messageType==MessageType.POP)
									{
										if(mess.lc<=minACK)
										{
											System.out.printf("%d Fully acknowledged  %s\n",this.disSemHelper.helperIndex,mess.toString());
											this.disSemHelper.mq.remove(mess);
											this.disSemHelper.s--;
											if(mess.owner==this.disSemHelper.helperIndex)
											{
												this.disSemHelper.sendAll(new Message(this.disSemHelper.helperIndex, MessageType.GO, this.disSemHelper.lc));
												this.disSemHelper.lc++;
											}
										}
											
									}
									
								}
										
							}
							
						}
						
					}
					catch(Throwable e)
					{
							System.out.println(this.disSemHelper.helperIndex+"helper disconnected");
						e.printStackTrace();
						return;
						//System.exit(1);
					}
				}
				
				
			}
			
		}
		
	}