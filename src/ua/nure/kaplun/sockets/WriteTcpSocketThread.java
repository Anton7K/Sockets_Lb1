package ua.nure.kaplun.sockets;

import java.io.*;

/**
 * Created by Anton on 23.02.2017.
 */
public class WriteTcpSocketThread extends Thread {
    private TCPClient client;

    public WriteTcpSocketThread(TCPClient client) {
        this.client = client;
    }

    public void run() {
        try {
            OutputStream out = client.getSocket().getOutputStream();
           DataOutputStream dOut = new DataOutputStream(out);
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            String line = null;
            System.out.println("Enter your name:");
            String clientName = keyboard.readLine();
            client.setClientName(clientName);
            Message clientNameMessage = new Message(SpecialCommands.SET_CLIENT_NAME, clientName, "".getBytes());
            out.write(clientNameMessage.getFullMessage());
            System.out.println(PrintColors.ANSI_YELLOW + "To change/choose other site companion by address enter " + SpecialCommands.SET_OTHER_SITE_CLIENT_BY_ADDRESS + "\n" +
                    "To change/choose other site companion by name enter " + SpecialCommands.SET_OTHER_SITE_CLIENT_BY_NAME + "\n" +
                    "To send binary file enter " + SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT + "\n" +
                    "To send text message to your companion just enter your message" + PrintColors.ANSI_RESET);
            while (true) {
                line = keyboard.readLine();
                switch (line) {
                    case SpecialCommands.SET_OTHER_SITE_CLIENT_BY_ADDRESS:
                        System.out.println("Enter internet address (for example '192.168.0.105')");
                        String inetAddress = keyboard.readLine();
                        Message message=new Message(SpecialCommands.SET_OTHER_SITE_CLIENT_BY_ADDRESS, client.getClientName(), inetAddress.getBytes());
                        out.write(message.getFullMessage());
                        break;
                    case SpecialCommands.SET_OTHER_SITE_CLIENT_BY_NAME:
                        System.out.println("Enter recipient name");
                        String name = keyboard.readLine();
                        Message nameMessage=new Message(SpecialCommands.SET_OTHER_SITE_CLIENT_BY_NAME, client.getClientName(), name.getBytes());
                        out.write(nameMessage.getFullMessage());
                        break;
                    case SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT:
                        System.out.println("Enter full path to file");
                        String filePath = keyboard.readLine();
                        FileSender.sendBinaryFileToOthersiteClient(filePath, dOut, client.getClientName());
                        break;
                    default:
                        if(line.trim()!=""){
                            Message textMessage = new Message(SpecialCommands.SEND_TEXT_MESSAGE_TO_OTHERSITE_CLIENT,
                                    client.getClientName(), line.getBytes());
                            out.write(textMessage.getFullMessage());
                        }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
