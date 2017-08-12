package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

import startup.Server;

public class ClientThread extends Thread {

	Socket communicationSocket = null;

	//Constructor 
	public ClientThread(Socket clientSocket) {
		communicationSocket = clientSocket;
	}

	//Setting up streams for communication 
	BufferedReader clientInput = null;
	PrintStream clientOutput = null;

	String username="";
	ClientThread opponent = null;

	@Override
	public void run() {

		try {
			//Initializing I/O streams
			clientInput = new BufferedReader(new InputStreamReader(communicationSocket.getInputStream()));
			clientOutput = new PrintStream(communicationSocket.getOutputStream());

			while(true) {
				String input = clientInput.readLine();

				if(input.equals("/EXIT")) {
					if(!Server.onlineUsers.isEmpty()) {
						Server.onlineUsers.remove(this);
						broadcastOnlineList(createOnlineList());
						System.out.println(username+" exited.");
					}
					communicationSocket.close();
					return;
				}

				if(input.startsWith("/USERNAME")) {
					String name = input.split(":")[1];
					String response = checkUsername(name);
					clientOutput.println("/USERNAME:"+response);
					if(response.equals("OK")) {
						this.username=name;
						Server.onlineUsers.add(this);
						broadcastOnlineList(createOnlineList());
						System.out.println(name+" has joined.");
					}		
				}

				if(input.startsWith("/INVITE")) {
					String name = input.split(":")[1];
					forwardInviteTo(name);
				}

				if(input.startsWith("/INVITEDBY")) {
					String name = input.split(":")[1];
					//forward to client
					this.clientOutput.println("/INVITEDBY"+name);
				}
				if(input.startsWith("/RSVPTO")) {
					String name = input.split(":")[1];
					String response = input.split(":")[2];
					forwardResponse(name,response);
				}


			}
			//Closing communication
			//communicationSocket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void forwardResponse(String name, String response) {
		for(ClientThread t : Server.onlineUsers) {
			if(t.username.equals(name)) {
				t.clientOutput.println("/RSVPBY:"+name+":"+response);
			}
		}

	}

	private void forwardInviteTo(String name) {
		for(ClientThread t : Server.onlineUsers) {
			if(t.username.equals(name)) {
				t.clientOutput.println("/INVITEDBY:"+this.username);
				return;
			}
		}		
	}


	private String checkUsername(String name) {
		if (Server.onlineUsers.isEmpty()) {
			return "OK";
		}
		for (ClientThread t : Server.onlineUsers) {
			if (t.username.equals(name)) {
				return "NOT_OK";
			}
		}
		return "OK";
	}

	private void broadcastOnlineList(String list) {
		for (ClientThread t : Server.onlineUsers) {
			t.clientOutput.println(list);
		}
	}
	private String createOnlineList() {
		String usernames="/LIST:";
		/*if(Server.onlineUsers.isEmpty()) {
			clientOutput.println(usernames);
			return;
		}*/
		for(ClientThread t : Server.onlineUsers) {
			usernames+=t.username+";";
		}
		return usernames;
	}

	//	private void forwardInvite(String user) {
	//		for(ClientThread t : Server.onlineUsers) {
	//			if (t.username.equals(user)) {
	//				this.opponent = t;
	//				t.clientOutput.println("\\INVITEDBY "+username);
	//				System.out.println("invite forwarded to "+user);
	//			}
	//		}
	//	}

}
