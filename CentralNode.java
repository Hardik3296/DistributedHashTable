import java.io.*;
import java.util.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.Naming;
class CentralNode extends UnicastRemoteObject implements CentralNodeInterface{
	private ArrayList<Integer>nodeID;
	private boolean busy;
	private Socket connection;
	private int maxNum;
	private int currNum;
	private String message;
	private ArrayList<Node> nodeList;
	static final int DEFAULT_PORT = 1083;

	CentralNode(int maxNum){
		this.nodeID = new ArrayList<Integer>();
		this.busy=false;
		this.currNum = 0;
		this.maxNum = maxNum;
	}

	// Function to generate unoque key for the node
	public int getNodeId(String hash){
		MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        md.update(hash.getBytes());
        byte[] hashBytes = md.digest();
        BigInteger hashNum = new BigInteger(1,hashBytes);
        id = Math.abs(hashNum.intValue()) % this.maxNum;
        // Checking with previously generated IDs
        while(nodeID.contains(id)) {
            md.reset();
            md.update(hashBytes);
            hashBytes = md.digest();
            hashNum = new BigInteger(1,hashBytes);
            id = Math.abs(hashNum.intValue()) % this.maxNum;
        }
        return id;
	}

	// Function to get id of preceding node
	public int getPredecessor(int id){
		int pred = -1;
		for(Integer n : nodeID){
			if(id == n)
				break;
			pred = n;
		}
		if(pred == -1)
			return id;
		return pred;
	}

	// Function to initialize finger table for the new node
	public String getFingerTable(int id) throws RemoteException{
		Collections.sort(nodeID);
		int len = nodeID.size();
		for(int i = 0; i < len; i++){
			if(nodeID[i] == id)
				break;
		}
		int increment = 1;
		String result = "";
		boolean first = true;
		for(i = i + 1; i < len; i = i + increment;){
			if(!first){
				result += "/";
				first = false;
			}
			increment = increment * 2;
			result += Integer.toString(nodeID[i]);
		}
		return result;
	}

	// Function to provide information about the node
	public String getNodeInfo(int id){
		String result="";
		for(Node temp : nodeList){
			if(temp.id == id){
				result=temp.ip+"/"+Integer.toString(port)+"/"+Integer.toString(id);
				return result;
			}
		}
		return "NACK";
	}

	// Function to allow other nodes to join the network
	public String joinNetwork(String ip, int port) throws RemoteException{
		if(busy){
			return "BUSY";
		}
		if(this.maxNum == this.currNum){
			return "FULL";
		}
		this.currNum++;
		synchronized(this){
			busy = true;
		}
		// Generating key for hashing
		String hash = ip+Integer.toString(port);
		// Getting unique id for the node
		int id = getNodeId(hash);
		// Adding node id to the id list
		nodeID.add(id);
		// Creating new node
		Node node = new Node(ip,port,id);
		// Adding the node information to the node list
		nodeList.add(node);
		Collections.sort(nodeID);
		// Getting predecessor for the given node
		int pred = getPredecessor(id);
		String nodeInfo = ip+"/"+Integer.toString(port)+"/"+Integer.toString(id);
		if(pred == id){
			return nodeInfo+"/"+nodeInfo;
		}
		// Getting information about the predecessor
		String predInfo = getNodeInfo(pred);
		synchronized(this){
			busy = false;
		}
		return nodeInfo+"/"+predInfo;
	}

	public static void main(String args[]) throws Exception{
		// Entering maximum number of nodes to be allowed simultaneous connection in the network
		BufferedReader reader = new BufferedReader(new IputStreamReader(System.in));
		System.out.println("Enter maximum number of nodes allowed");
		int maxNum;
		try{
			maxNum = Integer.parseInt(reader.readLine());
		}catch(Exception e){
			System.out.println("Error in value of maximum number of nodes");
			System.out.println("System exiting....")
			System.exit(0);
		}
		// Creating object for central node
		CentralNode superNode = new CentralNode(maxNum);
		// Binding the object with the registry
		try{
			Naming.rebind("CentralNode",superNode);
		}catch(Exception e){
			System.out.println("Central node could not be bound to registry");
			System.out.println("System exiting");
			System.exit(0);
		}
		System.out.println("Central Node is waiting for connection......");
	}
}