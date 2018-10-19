import java.io.*;
import java.security.*;
import java.util.*;
import java.math.BigInteger; 
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.Naming;
class Node{
	private String ip;
	private int port;
	private int id;

	Node(String ip,int port,int id){
		this.ip=ip;
		this.port=port;
		this.id=id;
	}

	public void setIP(String ip){
		this.ip = ip;
	}

	public void setPort(int port){
		this.port = port;
	}

	public void setID(int id){
		this.id = id;
	}

	public String getIP(){
		return this.ip;
	}

	public int getPort(){
		return this.port;
	}

	public int getID(){
		return this.id;
	}
}