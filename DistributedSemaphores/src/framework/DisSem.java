package framework;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import framework.DisSemHelper.MessageType;

public class DisSem {
	String semaphoreName;
	String multiIP;
	int multiPort;
	int helperIndex=-1;
	MulticastSocket multiCastSocket;
	InetAddress group;
	public DisSem(String semaphoreName, String multicastIP, int multicastPort,int helperIndex) throws IOException {
		super();
		this.semaphoreName = semaphoreName;
		this.multiIP = multicastIP;
		this.multiPort = multicastPort;
		this.helperIndex=helperIndex;
		group = InetAddress.getByName(multiIP);
		multiCastSocket = new MulticastSocket(multicastPort);
		multiCastSocket.joinGroup(group);
		System.out.println("Helper connected");
	}

	int lc=0;
	int ts;
	protected void sendAll(Message message) throws IOException
	{
		byte [] buf =message.toString().getBytes();
		DatagramPacket packet=new DatagramPacket(buf, buf.length,group,multiPort);
		multiCastSocket.send(packet);
	}
	protected Message readAPacket() throws IOException
	{
		byte [] buf=new byte[1024];
		DatagramPacket packet=new DatagramPacket(buf, buf.length);
		multiCastSocket.receive(packet);
		String ms=new String(buf).trim();
		String [] ma=ms.split(",");
		Message message=new Message(Integer.parseInt(ma[0]),MessageType.valueOf(ma[1]), Integer.parseInt(ma[2]));
		return message;
		
	}
	public synchronized void P() throws IOException
	{
		System.out.printf("%d Attempting P\n",helperIndex);
		sendAll(new Message(helperIndex, MessageType.REQP, lc));
		lc++;
		while(true)
		{
			Message message=readAPacket();
			if(message.messageType!=MessageType.GO)
				continue;
			if(message.owner!=helperIndex)
				continue;
			int ts=message.lc;
			lc=Math.max(lc, ts+1);
			System.out.printf("%d Done P at %d\n",helperIndex,lc);
			lc++;
			return;
		}
		
	}
	public synchronized void V() throws IOException
	{
		System.out.printf("%dAttempting V\n",helperIndex);
		sendAll(new Message(helperIndex, MessageType.REQV, lc));
		lc++;
		System.out.printf("%d Done V\n",helperIndex);
		
	}
}
