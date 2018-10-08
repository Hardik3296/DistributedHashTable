import java.io.*;
import java.rmi.*;
import java.util.*;
public interface CentralNodeInterface{
	public int generateId(String hash);
	public Node joinNetwork(String ip,String port);
	public int precedingId(int id);
	public void calculate();
}