package ua.nure.kaplun.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
/**
 * Created by Anton on 01.03.2017.
 */
public class UDPClient{
    private SocketAddress address;
    private String name;
    private SocketAddress companionAddress;

    public UDPClient(SocketAddress address, String name) {
        this.address = address;
        this.name = name;
    }

    public UDPClient(SocketAddress address, String name, SocketAddress companionAddress) {
        this.address = address;
        this.name = name;
        this.companionAddress = companionAddress;

    }

    public SocketAddress getAddress() {
        return address;
    }

    public void setAddress(SocketAddress address) {
        this.address = address;
    }


    public String getClientName() {
        return name;
    }

    public void setClientName(String name) {
        this.name = name;
    }

    public SocketAddress getCompanionAddress() {
        return companionAddress;
    }

    public void setCompanionAddress(SocketAddress companionAddress) {
        this.companionAddress = companionAddress;
    }

    public  static void main(String[] args){
        try {
            DatagramSocket clientSocket = new DatagramSocket();
            UDPClient client = connectToServer(clientSocket);
            ReadUdpSocketThread readThread = new ReadUdpSocketThread(clientSocket);
            readThread.start();
            SocketAddress serverAddress = new InetSocketAddress(InetAddress.getByName(Configuration.SERVER_NAME), Configuration.UDP_PORT);
            WriteUDPSocketThread writeThread = new WriteUDPSocketThread(client, clientSocket, serverAddress);
            writeThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static UDPClient connectToServer(DatagramSocket clientSocket) throws IOException {
        byte[] sendData;
        byte[] receiveData = new byte[Configuration.UDP_CHUNK_SIZE];
        InetAddress IPAddress = InetAddress.getByName(Configuration.SERVER_NAME);
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter your name:");
        String name = keyboard.readLine();
        Message nameMes = new Message(SpecialCommands.SET_CLIENT_NAME, name,"".getBytes());
        sendData=nameMes.getFullMessage();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, Configuration.UDP_PORT);
        clientSocket.send(sendPacket);
        int port = clientSocket.getPort();
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        clientSocket.receive(receivePacket);
        System.out.println(PrintColors.ANSI_GREEN + new String(receivePacket.getData()));

//        SocketAddress socAddr=new InetSocketAddress(clientSocket.getInetAddress(), clientSocket.getPort());
        return new UDPClient(receivePacket.getSocketAddress(), name);
    }
}
