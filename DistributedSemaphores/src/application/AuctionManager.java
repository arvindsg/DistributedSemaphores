package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class AuctionManager {
	public static String currentAuctionId="";
	public static int currentAuctionPrice=0;
	
	public static void main(String []args) throws IOException
	{
		if(args.length!=1) {
			System.out
			.println("usage: java AuctionManager auctionPort");
	
			System.exit(1);
		}
		int auctionPort=Integer.parseInt(args[0]);
		ServerSocket serverSocket=new ServerSocket(auctionPort);
		while(true)
		{
			try{
			System.out.println("Waiting for more bidders");
			Socket socket=serverSocket.accept();
			System.out.println("Bidder connected");
			DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
			DataInputStream dis=new DataInputStream(socket.getInputStream());
			dos.writeInt(currentAuctionPrice);
			dos.writeUTF(currentAuctionId);
			int requestedAuction=dis.readInt();
			String requestingBidder=dis.readUTF();
			if(requestedAuction>currentAuctionPrice)
			{
				currentAuctionPrice=requestedAuction;
				currentAuctionId=requestingBidder;
				dos.writeBoolean(true);
			}
			else
			{
				dos.writeBoolean(false);
				
			}
			System.out.printf("Current winning bidder is %s at value %d",currentAuctionId,currentAuctionPrice);
			socket.close();
			}catch(Throwable e)
			{
				e.printStackTrace();
			}
			
		}
		
		
	}
}
