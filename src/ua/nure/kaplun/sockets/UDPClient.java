package ua.nure.kaplun.sockets;

import java.io.IOException;
import java.net.*;
/**
 * Created by Anton on 01.03.2017.
 */
public class UDPClient extends Thread{

    public  static void main(String[] args){
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            connectToServer(clientSocket);
            ReadUdpSocketThread readThread = new ReadUdpSocketThread(clientSocket);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void connectToServer(DatagramSocket clientSocket) throws IOException {
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[1024];
        InetAddress IPAddress = InetAddress.getByName(Configuration.SERVER_NAME);
        sendData=("Hello!").getBytes();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Configuration.UDP_PORT);
        clientSocket.send(sendPacket);
        int port = clientSocket.getPort();
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        System.out.println(PrintColors.ANSI_GREEN + new String(receivePacket.getData()));
    }
}
