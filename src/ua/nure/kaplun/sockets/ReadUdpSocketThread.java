package ua.nure.kaplun.sockets;

import java.io.IOException;
import java.net.*;

/**
 * Created by Anton on 01.03.2017.
 */
public class ReadUdpSocketThread extends Thread {
    private DatagramSocket clientSocket;

    public ReadUdpSocketThread(DatagramSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run(){
        while (true){
            try {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket =  new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                System.out.println(new String(receivePacket.getData()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
