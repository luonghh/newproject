package nhom11ltm;

import java.net.*;

public class ServerThread extends Thread {
	
	Server mainServer;
	ServerSocket socket;
	boolean connected = true;
	
	public ServerThread(Server mainServer){
		this.mainServer = mainServer;
		socket = mainServer.socket;
	}
	
	public void run(){
		while(connected){
			try{
				// waits for clients and assign one server agent to each client
				Socket soc = socket.accept();
				AgentServerThread sat = new AgentServerThread(mainServer, soc);
				sat.start();//start the agent thread
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
