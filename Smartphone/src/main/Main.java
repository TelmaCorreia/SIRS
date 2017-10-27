package main;

public class Main {
	
	public static void main (String[] args){
		KeyHelper km = new KeyHelper();
		
		String key = km.getKey(); //generate the key
        String initVector = km.getInitialVector();
        String encypted = km.encrypt(key, initVector, "It works :D");
        String decrypted = km.decrypt(key, initVector, encypted );
        
        
        System.out.println("KEY: "+key+ "\nIV: "+initVector);
        System.out.println("Encrypted message: "+encypted);
        System.out.println("Decrypted message: "+decrypted);
	
	}

}
