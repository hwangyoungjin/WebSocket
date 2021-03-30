package com.company.chat;

import java.io.*;
import java.net.*;
import java.util.*;
public class MultiChatServer {
    HashMap clients;
    MultiChatServer() {
        clients = new HashMap();
        Collections.synchronizedMap(clients);
    }
    public void start() {
        ServerSocket serverSocket = null;
        Socket socket = null;
        try{
            serverSocket = new ServerSocket(9999);
            System.out.println("start server...");
            while(true) {
                socket = serverSocket.accept();
                System.out.println(socket.getInetAddress()+":"+
                        socket.getPort()+" connect!");
                ServerReceiver thread = new ServerReceiver(socket);
                thread.start(); // run()
            }
        }catch(Exception e) {e.printStackTrace();}
    }
    void sendToAll(String msg) {//브로드캐스팅 기능
        Iterator iterator = clients.keySet().iterator();
        while(iterator.hasNext()) {
            try {
                DataOutputStream out =
                        (DataOutputStream)clients.get(iterator.next());
                out.writeUTF(msg);
            }catch(IOException e) {e.printStackTrace();}
        }
    }
    public static void main(String[] args) {
        new MultiChatServer().start();
    }
    //inner class
    class ServerReceiver extends Thread {
        Socket socket; DataInputStream in; DataOutputStream out;
        ServerReceiver(Socket socket) {
            this.socket = socket;
            try{
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            }catch(Exception e) {e.printStackTrace();}
        }
        public void run() {
            String name = "";
            try{
                name = in.readUTF(); // park, lee
                if (clients.get(name) != null) {//같은 이름사용자 존재
                    out.writeUTF("#Aleady exist name : "+name);
                    out.writeUTF("#Please reconnect by other name");
                    System.out.println(socket.getInetAddress()+":"+
                            socket.getPort()+" disconnect!");
                    in.close();
                    out.close();
                    socket.close();
                    socket = null;
                } else {//같은 이름 존재하지 않는 경우
                    sendToAll("#"+name+" join!");
                    clients.put(name, out);
                    while(in != null) { sendToAll(in.readUTF()); }
                }
            }catch(IOException e) { e.printStackTrace();
            }finally{
                if (socket != null) {
                    sendToAll("#"+name+" exit!");
                    clients.remove(name);
                    System.out.println(socket.getInetAddress()+":"+
                            socket.getPort()+" disconnect!");
                }
            }
        }
    }
}
