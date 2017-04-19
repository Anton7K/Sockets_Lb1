package ua.nure.kaplun.sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/**
 * Created by Anton on 19.04.2017.
 */
public class WriteUDPSocketThread extends Thread{
    private UDPClient client;
    DatagramSocket socket;
    SocketAddress serverAddress;

    public WriteUDPSocketThread(UDPClient client, DatagramSocket socket, SocketAddress serverAddress){
        this.client=client;
        this.socket=socket;
        this.serverAddress=serverAddress;
    }
    @Override
    public void run() {
        String userInput;
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(PrintColors.ANSI_YELLOW +
                "To change/choose other site companion by name enter " + SpecialCommands.SET_OTHER_SITE_CLIENT_BY_NAME + "\n" +
                "To send binary file enter " + SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT + "\n" +
                "To send text message to your companion just enter your message" + PrintColors.ANSI_RESET);
        try {
            while (true){
                userInput = keyboard.readLine();
                Message mes;
                switch (userInput){
                    case SpecialCommands.SET_OTHER_SITE_CLIENT_BY_NAME:
                        System.out.println("Enter name of your companion");
                        userInput = keyboard.readLine();
                        mes = new Message(SpecialCommands.SET_OTHER_SITE_CLIENT_BY_NAME, client.getClientName(), userInput.getBytes());
                        byte[] buf=mes.getFullMessage();
                        DatagramPacket sendPacket = new DatagramPacket(buf,buf.length, serverAddress);
                        socket.send(sendPacket);
                        break;
                    default:
                        mes = new Message(SpecialCommands.SEND_TEXT_MESSAGE_TO_OTHERSITE_CLIENT, client.getClientName(), userInput.getBytes());
                        byte[] textBuf=mes.getFullMessage();
                        DatagramPacket sendTxtPacket = new DatagramPacket(textBuf, textBuf.length, serverAddress);
                        socket.send(sendTxtPacket);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
