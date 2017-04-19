package ua.nure.kaplun.sockets;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Anton on 24.02.2017.
 */
public class UDPServer extends Thread{
//    private DatagramPacket receivePacket;

    private static DatagramSocket serverSocket;
    private static ArrayList<UDPClient> clients = new ArrayList<>();

    public UDPServer(DatagramPacket clientPacket) {
        try {
            InetAddress IPAddress = clientPacket.getAddress();
            int port = clientPacket.getPort();
            byte[] sendData;
            byte[] receivedChunk = clientPacket.getData();
            Message receivedMessage=new Message(receivedChunk);
            String senderName = receivedMessage.getSenderName();
            SocketAddress socAdr = new InetSocketAddress(IPAddress, port);
            boolean isClientNew=true;
            SocketAddress companionAddress=socAdr;
            for(UDPClient client : clients){
                if(client.getAddress().equals(socAdr)){
                    isClientNew=false;
                    companionAddress = client.getCompanionAddress();
                    break;
                }
            }

            if(isClientNew) {
                clients.add(new UDPClient(socAdr, senderName));
                System.out.println(PrintColors.ANSI_GREEN + "****New client with socket address:" + socAdr
                        + " plug in");
                sendData = ("Successful connection").getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, socAdr);
                serverSocket.send(sendPacket);
            }else {
                String specCommand = receivedMessage.getCommand();
                switch (specCommand){
                    case SpecialCommands.SET_OTHER_SITE_CLIENT_BY_NAME:
                        int mesLength = receivedMessage.getMessageLength();
                        byte[]realData = new byte[mesLength];
                        System.arraycopy(receivedChunk, 0, realData, 0, mesLength);
                        receivedMessage = new Message(realData);
                        String companionName = new String(receivedMessage.getMessageContent());
                        SocketAddress compAddress = null;
                        for(UDPClient client: clients){
                            if(client.getClientName().equals(companionName)){
                                compAddress=client.getAddress();
                                client.setCompanionAddress(socAdr);
                                break;
                            }
                        }
                        if (compAddress!=null){
                            for(UDPClient client: clients){
                                if(client.getAddress().equals(socAdr)){
                                    client.setCompanionAddress(compAddress);
                                }
                            }
                        }
                        break;
                    case SpecialCommands.SEND_TEXT_MESSAGE_TO_OTHERSITE_CLIENT:
                    case SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT:
                        DatagramPacket sendPacket = new DatagramPacket(receivedChunk, receivedChunk.length, companionAddress);
                        serverSocket.send(sendPacket);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        while (true){
            try {
                byte[] receiveData = new byte[Configuration.UDP_CHUNK_SIZE];
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
                    byte[] receiveData = new byte[Configuration.UDP_CHUNK_SIZE];
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
