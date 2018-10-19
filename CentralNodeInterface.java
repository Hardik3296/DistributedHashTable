import java.io.*;
import java.util.*;
import java.net.*;
import java.math.BigInteger; 
import java.rmi.*;
import java.security.*;
import java.rmi.server.*;
import java.rmi.Naming;

public interface CentralNodeInterface extends Remote{
	public int getNodeId(String hash) throws RemoteException,NoSuchAlgorithmException;
	public int getPredecessor(int id);
	public String getNodeInfo(int id);
	public String joinNetwork(String ip, int port) throws RemoteException,NoSuchAlgorithmException;
	public void leaveNetwork(int id);
	public ArrayList<Node> getFingerTable(int id) throws RemoteException;
}