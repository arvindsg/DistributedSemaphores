package application;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import framework.DisSem;

public class AuctionUser {
	public static void main(String []args)
	{
		if (args.length != 6) {
				System.out
					.println("usage: java AuctionUser multiIP multiPort auctionManagerIp auctionManagerPort username index");
			
				System.exit(1);
			}
		DisSem dissem = null;
		while (true) {

			try {
					if (dissem == null)
						dissem = new DisSem("abc", args[0], Integer.parseInt(args[1]),Integer.parseInt(args[5]));
					BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
					while(true)
					{
						
						System.out.printf("Press Enter to place a bid");
							reader.readLine();
						System.out.println("Requesting bidding rights");
						dissem.P();
						System.out.println("Got bidding rights, requesting current price");
						try{
						Socket socket=new Socket(InetAddress.getByName(args[2]),Integer.parseInt(args[3]));
						DataInputStream dis=new DataInputStream(socket.getInputStream());
						DataOutputStream dos=new DataOutputStream(socket.getOutputStream());
						int currentBid=dis.readInt();
						String currentBidder=dis.readUTF();
						if(currentBid>0)
						{
							if(currentBidder.equals(args[4]))
							{
								System.out.printf("You are the current winning bidder at %d",currentBid);
							}
							else
								System.out.printf("Current winning bid is by %s at value %d\n",currentBidder,currentBid);
						}
						else
						{
							System.out.printf("No bids placed yet\n");
						}
						System.out.printf("Enter your bid(-1 to skip bidding)\n");
						int bid=Integer.parseInt(reader.readLine());
						if(bid>currentBid)
						{
							dos.writeInt(bid);
							dos.writeUTF(args[4]);
							if(dis.readBoolean()) System.out.println("Your bid has been placed, you are now the leading bidder");
							else System.out.println("Your are not the leading bidder");
						}
						else
							{
								System.out.println("Bid not placed");
								dos.writeInt(-1);
								dos.writeUTF(args[4]);
							}
						socket.close();
						}
						catch(Throwable e)
						{
							throw new Error("Communication problem with manger",e);
						}
						finally
						{
							dissem.V();
						}
					}
				}
			catch (Throwable e) {
				System.out
						.println("Failed to initialize dissem, will attempt again");
				e.printStackTrace();

			}

		}
		
		
		
	}
	
}
