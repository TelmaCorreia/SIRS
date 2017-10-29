package main;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class MySocket {
	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;


	public MySocket(String name, int port){
		
		try{

			socket = new Socket(name, port);
			input = new DataInputStream(socket.getInputStream());
			output = new DataOutputStream(socket.getOutputStream());


		}catch(IOException e){
			System.err.println("IOException: "+ e.getMessage());
		}
	}
	
	public Socket getSocket(){
		return this.socket;
	}
	
	public DataInputStream getInputStream(){
		return this.input;
	}
	
	public DataOutputStream getOutputStream(){
		return this.output;
	}
	
	@SuppressWarnings("deprecation")
	public void Connection(){
		if (socket != null && output != null && input != null) {
            try {
            	output.writeBytes("Cenas");
            	
                BufferedReader lines = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                while (true) {
                  String line = lines.readLine();
                  if (line == null)
                    break;
                  System.out.println("Server: " + line);
                  //TODO: protocol
                  if (line.indexOf("Ok") != -1) {
                      break;
                    }
                  
                }
                
                output.close();
                input.close();
                socket.close();
            } catch (IOException e) {
                System.err.println("IOException:  " + e.getMessage());
            }
		}
		
	}
	
}
