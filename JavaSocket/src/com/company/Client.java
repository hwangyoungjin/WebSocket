package com.company;
import java.net.*;
import java.util.Scanner;
import java.io.*;
public class Client {
    public static void main(String[] args) {
        try {
            Socket socket=new Socket("127.0.0.1", 9999); // -------->accept()
            System.out.println("Client : localPort = "+ socket.getLocalPort());
            System.out.println("Client : getPort = "+ socket.getPort());
            System.out.println("Connection Success!");
            Scanner scanner=new Scanner(System.in);
            String message=scanner.nextLine();
            OutputStream out=socket.getOutputStream();
            DataOutputStream dos=new DataOutputStream(out);
            dos.writeUTF(message);

            InputStream in=socket.getInputStream();
            DataInputStream dis=new DataInputStream(in);
            System.out.println("Receive:"+dis.readUTF());

            dis.close();
            dos.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
