import java.io.*;
import java.util.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.Naming;
class NodeDHT implements Runnable{
	private Node node,predecessor;
	private Socket connection;
	private String message;
	private HashMap<String,String>;
	private ArrayList<Integer>;

	NodeDHT(String arrValue[]){
		node = new Node(arrValue[0],Integer.parseInt(arrValue[1]),Integer.parseInt(arrValue[2]));
		predecessor = new Node(arrValue[3],Integer.parseInt(arrValue[4]),Integer.parseInt(arrValue[5]));
		words = new HashMap<String,String>();
		message = "";
		fingerTable = new ArrayList<Integer>();
	}

	public void run(){
		
	}

	public static void main(String args[]){
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter port number for this node between 1024 and 65536.");
		// Entering port number the node will use to listen for connections from other nodes
		int portNum = Integer.parseInt(reader.readLine());
		if(portNum<1024||portNum>65536){
			System.out.println("Please enter port number from 1024 to 65536.");
			System.out.println("System exiting.");
			System.exit(0);
		}
		// Server port to which the node will listen to
		ServerSocket server = new ServerSocket(portNum);
		// Getting the IP of the machine
		InetAddress myIP = InetAddress.getLocalHost();
		// Obtaining reference for the Central Node from the registry
		CentralNode object = (CentralNode)Naming.lookup("CentralNode");
		// Joining the network
		String value = object.joinNetwork(myIP.getHostAddress(),portNum);
		if(value.equals("BUSY")){
			System.out.println("Central Node is busy. Please try after sometime.");
			System.out.println("System exiting...");
			System.exit(0);
		}
		else if(value.equals("FULL")){
			System.out.println("No more nodes can join the network at the present moment.");
			System.out.println("System exiting....");
			System.exit(0);
		}
		String arrValue[] = value.split("/");
		// Creating the node from the information obtained
		NodeDHT dht = new NodeDHT(arrvalue);
		// Waiting for connections from other nodes
		System.out.println("System has completed initializing. Listening for other nodes at port "+dht.node.port+".");
		while(true){
			dht.connection = server.accept();
			BufferedReader input = new BufferedReader(new InputStreamReader(dht.connection.getInputStream()));
			dht.message = input.readLine();
			Thread t = new Thread(dht);
			t.start();
		}
	}
}