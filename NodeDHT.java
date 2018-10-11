import java.io.*;
import java.util.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.Naming;
class NodeDHT{
	private Node node,predecessor;
	private Socket connection;
	private HashMap<String,String> words = new HashMap<String,String>();
	private ArrayList<Integer> fingerTable = new ArrayList<Integer>();

	NodeDHT(String arrValue[]){
		node = new Node(arrValue[0],Integer.parseInt(arrValue[1]),Integer.parseInt(arrValue[2]));
		predecessor = new Node(arrValue[3],Integer.parseInt(arrValue[4]),Integer.parseInt(arrValue[5]));
	}

	public static void main(String args[]){
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		Socket s;
		System.out.println("Enter port number for this node between 0 and 65536.");
		int portNum = Integer.parseInt(reader.readLine());
		if(port<0||port>65536){
			System.out.println("Please enter port number from 0 to 65536.");
			System.out.println("System exiting.");
			System.exit(0);
		}
		ServerSocket server = new ServerSocket(portNum);
		InetAddress myIP = InetAddress.getLocalHost();
		CentralNode object = (CentralNode)Naming.lookup("CentralNode");
		String value = object.getInformation(myIP.getHostAddress(),portNum);
		String arrValue[] = value.split("/");
		NodeDHT dht = new NodeDHT(arrvalue);
		while(true){
			s = server.accept();
		}
	}
}