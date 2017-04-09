package ua.nure.kaplun.sockets;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by Anton on 23.02.2017.
 */
public class TCPClient extends Thread{

    public String getClientName() {
        return name;
    }

    public void setClientName(String name) {
        this.name = name;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    private String name;
    private Socket socket;
    TCPClient(Socket socket){
        this.socket=socket;
    }
    public static void main(String[] args){
        try
        {
            Socket socket= new Socket(InetAddress.getByName(Configuration.SERVER_NAME), Configuration.TCP_PORT);
            TCPClient client = new TCPClient(socket);
            ReadTcpSocketThread readThread = new ReadTcpSocketThread(client);
            readThread.start();
            WriteTcpSocketThread writeThread = new WriteTcpSocketThread(client);
            writeThread.start();
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
    }
}
