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
                byte[] receiveData = new byte[Configuration.UDP_CHUNK_SIZE];
                DatagramPacket receivePacket =  new DatagramPacket(receiveData, receiveData.length);
                clientSocket.receive(receivePacket);
                Message receivedMessage = new Message(receivePacket.getData());
                String specCommand = receivedMessage.getCommand();
                String senderName = receivedMessage.getSenderName();
                String printedSenderName = PrintColors.ANSI_BLUE +
                        senderName + ':' + PrintColors.ANSI_RESET + "\n\t";
                switch (specCommand){
                    case SpecialCommands.SEND_TEXT_MESSAGE_TO_OTHERSITE_CLIENT:
                        System.out.println(printedSenderName + new String(receivedMessage.getMessageContent()));
                        break;
                    case SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT:
                        break;
                }
//                System.out.println(new String(receivePacket.getData()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
