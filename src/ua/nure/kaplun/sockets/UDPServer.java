package ua.nure.kaplun.sockets;

import java.io.IOException;
import java.net.*;

/**
 * Created by Anton on 24.02.2017.
 */
public class UDPServer extends Thread{
    private DatagramPacket receivePacket;
    private static DatagramSocket serverSocket;

    public UDPServer(DatagramPacket clientPacket) {

        try {
            this.receivePacket = clientPacket;
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            byte[] sendData = new byte[1024];
            SocketAddress socAdr = new InetSocketAddress(IPAddress, port);
            System.out.println(PrintColors.ANSI_GREEN + "****New client with socket address:" + socAdr
                    + " plug in");
            sendData=("Successful connection").getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, socAdr);
            serverSocket.send(sendPacket);
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        while (true){
            try {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket =  new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                System.out.println(new String(receivePacket.getData()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args){
        try {
            serverSocket = new DatagramSocket(Configuration.UDP_PORT);
            while (true){
                try {
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket =  new DatagramPacket(receiveData, receiveData.length);
                    serverSocket.receive(receivePacket);
                    new UDPServer(receivePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

}
