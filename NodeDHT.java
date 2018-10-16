import java.io.*;
import java.util.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.Naming;
class NodeDHT implements Runnable{
	private Node node,predecessor;
	private boolean type;
	private Socket connection;
	private String message;
	private HashMap<String,String> words;
	private ArrayList<Node> fingers;

	NodeDHT(String arrValue[]){
		node = new Node(arrValue[0],Integer.parseInt(arrValue[1]),Integer.parseInt(arrValue[2]));
		predecessor = new Node(arrValue[3],Integer.parseInt(arrValue[4]),Integer.parseInt(arrValue[5]));
		words = new HashMap<String,String>();
		message = "";
		type = false;
		fingers = new ArrayList<INode>();
	}

	public void initializeFingers(CentralNode object){
		this.fingers = object.getFingerTable(this.node.id);
		System.out.println("Finger table initialized successfully");
	}

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

	public void initializeWords(){
		Socket s = new Socket(this.predecessor.ip,this.predecessor.port);
		DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
		BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
		String mess = "Initialize Finger Table/"+Integer.toString(this.node.id);
		out.writeUTF(mess);
		String inputFromServer = fromServer.readLine();
		String value[] = inputFromServer.split("/");
		for(String pair : value){
			String keyValue[] = pair.split(":");
			dht.words.put(keyValue[0],keyValue[1]);
		}
		System.out.println("Key Value pairs initialized successfully");
	}

	public void run(){
		if(dht.type){
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			while(true){
				System.out.println("Press 1 for complete information on this node'");
				System.out.println("Press 2 to input values.");
				System.out.println("Press 3 to leave the network");
				System.out.println("Press 4 to obtain information for given key");
				int input;
				input = Integer.parseInt(reader.readLine());
				switch(input){
					case 1:
					Set<String>keys = dht.words.keySet();
					System.out.println("Key"+"		"+"Value");
					for(String k : keys){
						System.out.println(k+"		"+dht.words.get(k));
					}
					break;
					case 2:
					String word = reader.reaLine();
					int wordID = this.getNodeId(word);
					String mess = "Put "+word+" "+Integer.toString(wordID);
					if(wordID > this.node.id && wordID < this.fingers[0].id || wordID == this.node.id){
						words.put(Integer.toString(wordID),word);
						System.out.println("Word has been put in the network");
					}
					else{
						int len = this.fingers.size();
						for(int i = 0; i < len; i++){
							if(wordID > this.fingers[i].id && wordID < this.fingers[i+1].id || wordID == this.fingers[i].id)
								break;
						}
						Socket s = new Socket(this.fingers[i].ip,this.fingers[i].port);
						DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
						out.writeUTF(mess);
						BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
						String inputFromServer = fromServer.readLine();
						System.out.println("Word has been put in the network");
					}
					break;
					case 3:
					break;
					case 4:
					break;
					default:
				}
			}
		}
		else{

		}
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
		dht.initializeWords();
		dht.initializeFingers(object);
		dht.type = true;
		Thread t1 = new Thread(dht);
		t1.start();
		sleep(1000);
		dht.type = false;
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