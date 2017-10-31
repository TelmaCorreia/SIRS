package main;

public class Main {
	
	public static void main (String[] args){
		MySocket socket = new MySocket("localhost", 80);
		socket.ping();

	}

}
