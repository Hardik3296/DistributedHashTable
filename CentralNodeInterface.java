import java.io.*;
import java.util.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.Naming;

public interface CentralNodeInterface extends Remote{
	public int getNodeId(String hash);
	public int getPredecessor(int id);
	public String getNodeInfo(int id);
	public String joinNetwork(String ip, int port);
	public String getFingerTable(int id);
}