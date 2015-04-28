package framework;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Comparator;
import java.util.PriorityQueue;


public class DisSemHelper implements Runnable {
	public enum MessageType{REQP,REQV,VOP,POP,ACK,GO};
	protected PriorityQueue<Message> mq;
	protected volatile int lc=0;
	protected volatile int s=1;
	
	String configuratorIP;
	int configuratorPort;
	int helperIndex;
	int multiPort;
	String multiIP;
	int helperNetworkPort;
	Connection coordinatorConnection;
	DataOutputStream coordinatorWrite;
	DataInputStream coordinatorRead;
	private int helperCount=0;
	Thread communicationThread;
	Integer [] acknowledgedTimeStamps;
	public DisSemHelper(String configuratorIP, int configuratorPort,
			 int helperIndex,String multiIP, int multiPort,int helperNetworkPort) {
		super();
		this.configuratorIP = configuratorIP;
		this.configuratorPort = configuratorPort;
		this.helperIndex = helperIndex;
		this.multiIP=multiIP;
		this.multiPort=multiPort;
		this.helperNetworkPort=helperNetworkPort;
		mq=new java.util.PriorityQueue<Message>(10,new QueueSorter());
	}
	public static void main(String[] args) {
		if (args.length != 6) 
			{
				System.out
					.println("usage: java Helper helperIndex coordinator-ip coordinator-port-num multiip multiport helperNetworkPort");
			
				System.exit(1);
			}
			DisSemHelper helper=new DisSemHelper(args[1],Integer.parseInt(args[2]), Integer.parseInt(args[0]), args[3],Integer.parseInt(args[4]),Integer.parseInt(args[5]));
			helper.run();
	}
	public class QueueSorter implements Comparator<Message>
	{
		public int compare_int(int a,int b)
		{
			if(a<b) return -1;
			if (a>b) return 1;
			return 0;
		}
		@Override
		public int compare(Message o1, Message o2) {
			// TODO Auto-generated method stub
			int result=compare_int(o1.lc, o2.lc);
			if(result==0) result=compare_int(o1.owner, o2.owner);
			if(result==0) throw new Error("Can't have two messages from same owner with same lc, check implementation");
			return result;
		}
		
	};
	boolean userConnected;
	MulticastSocket multiCastSocket;
	InetAddress group;
	void attemptHelperConnections() throws IOException
	{
		group = InetAddress.getByName(multiIP);
		multiCastSocket = new MulticastSocket(6789);
		multiCastSocket.joinGroup(group);
		
	}
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
	protected void insert(Message message)
	{
		//insert at apt position-oldest message firsst
		synchronized (mq) {
			mq.add(message);
		}
		
	}
	
	@Override
	public void run() {
		configurate();
		System.out.printf("%d Configurated helper \n",helperIndex);
		try {
			attemptHelperConnections();
			System.out.printf("%d made framework connections\n",helperIndex);
		synchronized (mq) {
			//start listening for connection to program
				communicationThread=new Thread(new MessageParser(this),"parser "+helperIndex);
				communicationThread.start();
		}
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void configurate()
	{
		this.configurate(configuratorIP, configuratorPort);
	}
	void configurate(String coorIP, int coorPort) {
		try {
			System.out.printf("[%d]Configuring Helper \n",helperIndex);
			coordinatorConnection=new Connection(helperNetworkPort);
			DataIO dio = coordinatorConnection.connectIO(coorIP, coorPort);
			coordinatorWrite=dio.getDos();
			coordinatorRead=dio.getDis();
			coordinatorWrite.writeInt(helperIndex);
			coordinatorWrite.writeUTF(InetAddress.getLocalHost().getHostAddress());
			coordinatorWrite.writeInt(helperNetworkPort);
			System.out.printf("[%d]Reading\n",helperIndex);
			helperCount=coordinatorRead.readInt();
			acknowledgedTimeStamps=new Integer[helperCount];
			for(int i=0;i<helperCount;i++)
			{
				acknowledgedTimeStamps[i]=0;
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();
			throw new Error("Error");
		}
	}

	
	

}
