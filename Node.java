import java.io.*;
import java.lang.*;
import java.rmi.Naming;
import java.net.*;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
class Node{
	Node predecessor;
	Node successor;
	int id;
	String ip;
	String port;
	LinkedHashMap<String,String> words;
	ArrayList<Node>fingerTable;
	Node (String ip,String port){
		this.ip = ip;
		this.port = port;
	}
	public static void main(String args[]) throws Exception{
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter port number for this node");
		String portNumber;
		try{
			portNumber = reader.readLine();
		}catch(NumberFormatException e){
			System.out.println("Enter port number greater that 1024 and less than 63556");
			System.out.println("System exiting");
			System.exit(0);
		}
		InetAddress me = InetAddress.getLocalHost();
      	String myIP = me.getHostAddress();
      	System.setSecurityManager(new RMISecurityManager());
      	try{
			CentralNode object = (CentralNode)Naming.lookup("CentralNode");
		}catch(Exception e){
			System.out.println("Central Node could not be found.")
			System.out.println("System exiting");
			System.exit(0);
		}
		Node node = object.joinNetwork(myIP,portNumber);
		if(node.ip.equals("Network is full")){
			System.out.println("Network capacity is full");
			System.exit(0);
		}
		else if(node.ip.equals("Central Node is busy")){
			System.out.println("Central Node is busy please try again later");
			System.exit(0);
		}
		while(true){
			System.out.println("Press 1 for list of data stored in this node");
			System.out.println("Press 2 to display information about this node");
			System.out.println("Press 3 to leave the network");	
			int option = Integer.parseInt(reader.readLine());
			/*switch(option){
				case 1:
			}*/
		}
	}
}