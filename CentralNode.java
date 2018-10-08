import java.io.*;
import java.lang.*;
import java.rmi.Naming; 
import java.math.BigInteger;
import java.rmi.registry.*;
import java.rmi.server.*;
import java.util.*;
import java.security.MessageDigest;
public class CentralNode extends UnicastRemoteObject implements CentralNodeInterface{
	int maxNumOfNodes;
	boolean busy;
	ArrayList <Integer> nodeId;
	ArrayList <Node> nodeList;
	int highestId;
	int lowestId;
	final String DEFAULT_IP = "172.16.23.45";
	final String DEFAULT_PORT = "1099";
	int currNumOfNodes;

	CentralNode(int maxNumOfNodes) throws RemoteException{
		this.maxNumOfNodes = maxNumOfNodes;
		this.currNumOfNodes = 0;
		nodeList = new ArrayList<Node>();
		nodeId = new ArrayList<Integer>();
		busy=false;
	}

	//To find the particular node in the network
	Node findNode(int id){
		for(Node ter : nodeList){
			if(ter.id == id){
				return ter;
			}
		}
	}

	// To generate unique Id for each joining node
	public int generateId(String hash){
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.reset();
		md.update(hash.getBytes());
		byte hashBytes[] = md.digest();
		int id;
		BigInteger hashNum = new BigInteger(1,hashBytes);
		id = Math.abs(hashNum.intValue()) % maxNumOfNodes;
		while(nodeId.contains(id)){
			md.reset();
			md.update(hash.getBytes());
			hashBytes = md.digest();
			hashNum = new BigInteger(1,hashBytes);
			id = Math.abs(hashNum.intValue()) % maxNumOfNodes;
		}
		return id;
	}

	// To return the Id of the preceding node 
	public int precedingId(int id){
		int prev=-1;
		for(Integer n : nodeId){
			if(n==id)
				break;
			prev=n;
		}
		if(prev==-1)
			return id;
		else
			return prev;
	}

	// To calculate the lowest and the highest Id generated
	public void calculate(){
		boolean first=true;
		for(Integer num : nodeId){
			if(first){
				this.lowestId=num;
				first=false;
			}
			this.highestId=num;
		}
	}

	// To create new nodes and include them in the network
	public Node joinNetwork(String ip,String port){
		// If the number of node is equal to number of maximum allowed nodes then return an empty node
		System.out.println("New node joining the network");
		if(currNumOfNodes == maxNumOfNodes){
			Node newNode = new Node("Network is full","");
			return newNode;
		}
		if(this.busy){
			Node newNode = new Node("Central Node is busy","");
			return newNode;
		}
		synchronized(this){
			this.busy=true;
		}
		//Genrate Id for the new node
		int id = generateId(ip+port);
		//Add the id to the list of all node ids
		nodeId.add(id);
		Collections.sort(nodeId);
		// Calculate the lowest and the highest node id
		calculate();
		// Get the id of the predecessor node in the loop
		int predId = precedingId(id);
		// Create the new node
		Node newNode = new Node(ip,port);
		// If the new node is the only node or if the node id is the smallest id 
		if(predId==id){
			if(highestId!=lowestId){
				Node temp = findNode(highestId);
				newNode.predecessor = temp;
				newNode.successor = temp.successor;
				temp.successor.predecessor = newNode;
				temp.successor = newNode;
			}
			if(highestId==lowestId){
				newNode.predecessor = newNode;
				newNode.successor = newNode;
			}
		}
		// If there are other nodes in the network
		else{
			Node temp = findNode(predId);
			newNode.predecessor = temp;
			newNode.successor = temp.successor;
			temp.successor.predecessor = newNode;
			temp.successor = newNode;
		}
		newNode.id = id;
		// Add the node to the list of nodes
		nodeList.add(newNode);
		// Increase the count of nodes in the list
		currNumOfNodes++;
		busy = false;
		return newNode;
	}

	public static void main(String args[]) throws Exception{
		Registry registry;
		int maxNumberOfNodes;
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Enter maximum number of nodes allowed in the system");
		try{
			maxNumberOfNodes = Integer.parseInt(reader.readLine());
		}
		catch(NumberFormatException e){
			System.out.println("Please enter a valid integer");
			System.out.println("System exiting");
			System.exit(0);
		}
		try{
			CentralNode superNode = new CentralNode(maxNumberOfNodes);
			superNode.busy=false;
		}catch(RemoteException rmexception){
			System.out.println("Central node could not be created");
			System.out.println("System is exiting");
			System.exit(0);
		}
		try{
			 Naming.rebind("CentralNode", superNode); 
		}catch(Exception e){
			System.out.println("Unable to register central node. System exiting");
			System.exit(0);
		}
		System.out.println("Central Node is waiting for other nodes to join");
	}
}