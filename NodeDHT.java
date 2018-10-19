import java.io.*;
import java.util.*;
import java.net.*;
import java.math.BigInteger; 
import java.security.*;
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
	private CentralNode object;

	NodeDHT(String arrValue[], CentralNode object) throws Exception{
		node = new Node(arrValue[0],Integer.parseInt(arrValue[1]),Integer.parseInt(arrValue[2]));
		predecessor = new Node(arrValue[3],Integer.parseInt(arrValue[4]),Integer.parseInt(arrValue[5]));
		words = new HashMap<String,String>();
		message = "";
		type = false;
		fingers = new ArrayList<Node>();
		this.object = object;
	}

	public void initializeFingers(CentralNode object) throws Exception{
		this.fingers = object.getFingerTable(this.node.getID());
		System.out.println("Finger table initialized successfully");
	}

	public int getNodeId(String hash) throws Exception{
		MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        md.update(hash.getBytes());
        byte[] hashBytes = md.digest();
        BigInteger hashNum = new BigInteger(1,hashBytes);
        int id = Math.abs(hashNum.intValue()) % 65536;
        return id;
	}

	public String insertWordInNetwork(String mess) throws Exception{
		String result[] = mess.split("/");
		int wid = Integer.parseInt(result[2]);
		if(wid > this.node.getID() && wid < this.fingers.get(0).getID() || wid == this.node.getID() || this.node.getID() < wid && wid > this.fingers.get(0).getID()){
			words.put(Integer.toString(wid),result[1]);
			String returnMess = Integer.toString(this.node.getID());
			return returnMess;
		}
		else{
			int len = this.fingers.size();
			int i;
			for(i = 0; i < len; i++){
				if(wid > this.fingers.get(i).getID() && wid < this.fingers.get(i+1).getID() || wid == this.fingers.get(i).getID())
					break;
			}
			Socket s = new Socket(this.fingers.get(i).getIP(),this.fingers.get(i).getPort());
			DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
			toServer.writeUTF(mess);
			BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String inputFromServer = fromServer.readLine();
			return inputFromServer;
		}
	}

	public void initializeWords() throws Exception{
		Socket s = new Socket(this.predecessor.getIP(),this.predecessor.getPort());
		DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
		BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
		String mess = "Initialize Finger Table/"+Integer.toString(this.node.getID());
		toServer.writeUTF(mess);
		String inputFromServer = fromServer.readLine();
		String value[] = inputFromServer.split("/");
		for(String pair : value){
			String keyValue[] = pair.split(":");
			this.words.put(keyValue[0],keyValue[1]);
		}
		System.out.println("Key Value pairs initialized successfully");
	}

	public void insertAllWords(String word) throws Exception{
		String res[] = word.split("/");
		for(String eachpair : res){
			String value[] = eachpair.split("/");
			this.words.put(value[0],value[1]);
		}
	}

	public void run(){
		if(this.type){
			try{
				BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
				while(true){
					System.out.println("Press 1 for complete information on this node'");
					System.out.println("Press 2 to input values.");
					System.out.println("Press 3 to leave the network");
					int input;
					input = Integer.parseInt(reader.readLine());
					switch(input){
						case 1:
						Set<String>keys = this.words.keySet();
						System.out.println("Key"+"		"+"Value");
						for(String k : keys){
							System.out.println(k+"		"+this.words.get(k));
						}
						break;
						case 2:
						String word = reader.readLine();
						int wordID = this.getNodeId(word);
						String mess = "Put "+word+" "+Integer.toString(wordID);
						if(wordID > this.node.getID() && wordID < this.fingers.get(0).getID() || wordID == this.node.getID()){
							words.put(Integer.toString(wordID),word);
							System.out.println("Word has been put in the network");
							System.out.println("The word has been put in the node with node id "+this.node.getID());
						}
						else{
							int len = this.fingers.size();
							int i;
							for(i = 0; i < len; i++){
								if(wordID > this.fingers.get(i).getID() && wordID < this.fingers.get(i+1).getID() || wordID == this.fingers.get(i).getID())
									break;
							}
							Socket s = new Socket(this.fingers.get(i).getIP(),this.fingers.get(i).getPort());
							DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
							toServer.writeUTF(mess);
							BufferedReader fromServer = new BufferedReader(new InputStreamReader(s.getInputStream()));
							String inputFromServer = fromServer.readLine();
							System.out.println("Word has been put in the network");
							System.out.println("The word has been put in the node with node id "+inputFromServer);
						}
						break;
						case 3:
						try{
							Socket s = new Socket(this.predecessor.getIP(),this.predecessor.getPort());
							DataOutputStream toServer = new DataOutputStream(s.getOutputStream());
							String result = "";
							Set<String>keys1 = words.keySet();
							boolean first = true;
							for(String key : keys1){
								if(!first)
									result += "/";
								first = false;
								result = result + (key+":"+words.get(key)); 
							}
							String mess1 = "Put All/"+result;
							this.object.leaveNetwork(this.node.getID());
							toServer.writeUTF(mess1);
							toServer.close();
							s.close();
							String mess2 = "Change Predecessor/"+this.fingers.get(0).getIP()+"/"+Integer.toString(this.fingers.get(0).getPort())+"/"+Integer.toString(this.fingers.get(0).getID());
							Socket sec = new Socket(this.fingers.get(0).getIP(),this.fingers.get(0).getPort());
							DataOutputStream toServersec = new DataOutputStream(sec.getOutputStream());
							toServersec.writeUTF(mess2);
							toServersec.close();
							sec.close();
							connection.close();
						}catch(IOException e){}
						break;
						default:
						System.out.println("The input selected is wrong.");
						System.out.println("Please input correct option");
					}
				}
			}catch(Exception e){
				System.out.println("Some Exception."+"\n"+"System exiting.........");
				System.exit(0);
			}
		}
		else{
			try{
				String result[] = message.split("/");
				if(result[0].equals("Put")){
					String result1 = insertWordInNetwork(message);
				}
				else if(result[0].equals("Put All")) {
					insertAllWords(message);
				}
				else if(result[0].equals("Change Predecessor")){
					Node temp = new Node(result[1],Integer.parseInt(result[2]),Integer.parseInt(result[3]));
					this.predecessor = temp;
				}
			}catch(Exception e){}
		}
	}

	public static void main(String args[]) throws Exception{
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
		CentralNode object = (CentralNode)Naming.lookup("CentralNodeDef");
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
		NodeDHT dht = new NodeDHT(arrValue,object);
		dht.initializeWords();
		dht.initializeFingers(object);
		dht.type = true;
		Thread t1 = new Thread(dht);
		t1.start();
		Thread.sleep(1000);
		dht.type = false;
		// Waiting for connections from other nodes
		System.out.println("System has completed initializing. Listening for other nodes at port "+dht.node.getPort()+".");
		while(true){
			dht.connection = server.accept();
			BufferedReader input = new BufferedReader(new InputStreamReader(dht.connection.getInputStream()));
			dht.message = input.readLine();
			Thread t = new Thread(dht);
			t.start();
		}
	}
}