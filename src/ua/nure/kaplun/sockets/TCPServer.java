package ua.nure.kaplun.sockets;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class TCPServer extends Thread{

    private Socket clientSocket;
    private String clientName = "Anonymous";
    private Socket otherSiteClient;
    private ArrayList<Socket> deliveryList = new ArrayList<>();


    private static ArrayList<TCPServer> allConnections;


    public static void main(String[] args) {
        try {
            allConnections = new ArrayList<>();
            ServerSocket serverSocket = new ServerSocket(Configuration.TCP_PORT, 0, InetAddress.getByName(Configuration.SERVER_NAME));
            while (true){
                new TCPServer(serverSocket.accept());
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public TCPServer(Socket clientSocket)
    {
        this.clientSocket = clientSocket;
        allConnections.add(this);
        int clientPort = clientSocket.getPort();
        InetAddress clientAddress = clientSocket.getInetAddress();
        SocketAddress socketAddress = new InetSocketAddress(clientAddress, clientPort);
        System.out.println(PrintColors.ANSI_GREEN + "*****New client with address " + socketAddress + " plug in******" + PrintColors.ANSI_RESET);
        try {
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
//            out.writeUTF(PrintColors.ANSI_GREEN + "Successful connection" + PrintColors.ANSI_RESET);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        setDaemon(true);
        setPriority(NORM_PRIORITY);
        start();
    }

    @Override
    public void run(){
        try {
            InputStream cin = clientSocket.getInputStream();
            OutputStream cout = clientSocket.getOutputStream();
            DataInputStream in = new DataInputStream(cin);
//            DataOutputStream out = new DataOutputStream(cout);
//            String receivedText;
            while (true){
                if (cin.available()>0) {

                    int messageLength = Message.getMessageLength(in);
                    if (messageLength!=-1) {
                        byte[] messageLengthBytes = Message.intToByteArray(messageLength);
                        int firstChunkLength = messageLength <= Configuration.CHUNKS_SIZE ? messageLength : Configuration.CHUNKS_SIZE;

                        byte[] receivedData = new byte[firstChunkLength];
                        int curIndex=0;
                        int bytesToRead=messageLength;

                        System.arraycopy(messageLengthBytes, 0, receivedData, curIndex, messageLengthBytes.length);
                        curIndex+=messageLengthBytes.length;

                        in.readFully(receivedData, curIndex, receivedData.length - curIndex);
                        bytesToRead -= receivedData.length;

                        Message receivedMessage = new Message(receivedData);
                        String specialCommand = receivedMessage.getCommand();


                        switch (specialCommand){
                            case SpecialCommands.SET_OTHER_SITE_CLIENT:
                                checkOtherSiteClientAddress(new String(receivedMessage.getMessageContent()));
                                break;
                            case SpecialCommands.SET_CLIENT_NAME:
                                this.clientName=receivedMessage.getSenderName();
                                break;
                            case SpecialCommands.SEND_TEXT_MESSAGE_TO_OTHERSITE_CLIENT :
                                OutputStream otherOutput = otherSiteClient.getOutputStream();
                                otherOutput.write(receivedMessage.getFullMessage());
                                while (bytesToRead >= Configuration.CHUNKS_SIZE) {
                                    byte[] receivedChunk = new byte[Configuration.CHUNKS_SIZE];
                                    in.read(receivedChunk);
                                    otherOutput.write(receivedChunk);
                                    bytesToRead -= Configuration.CHUNKS_SIZE;
                                }
                                if (bytesToRead != 0) {
                                    byte[] finalChunk = new byte[bytesToRead];
                                    in.read(finalChunk);
                                    otherOutput.write(finalChunk);
                                }
                                otherOutput.flush();
                                break;
                            case SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT :
                                String fileName =receivedMessage.getFileName();
                                String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
                                OutputStream otherOut = otherSiteClient.getOutputStream();
                                if (fileExtension.equals(".bmp")) {
                                    String dirName = "Server_files_" + clientName;
                                    File receivedFile = FileSender.receiveBinaryFile(cin, bytesToRead, receivedMessage,
                                            clientName, fileName, dirName);
                                    BufferedImage image = ImageIO.read(receivedFile);
                                    String convertedFileName = fileName.substring(0, fileName.lastIndexOf('.')) + ".jpg";
                                    File convertedFile = new File(dirName + "\\" + convertedFileName);
                                    ImageIO.write(image, "jpg", convertedFile);

                                    FileSender.sendBinaryFileToOthersiteClient(convertedFile.getPath(),
                                            new DataOutputStream(otherOut), this.clientName);

                                    //delete server folder
                                    receivedFile.delete();
                                    convertedFile.delete();
                                    File serverDir = new File(dirName);
                                    serverDir.delete();
                                } else {
                                    otherOut.write(receivedMessage.getFullMessage());
                                    while (bytesToRead >= Configuration.CHUNKS_SIZE) {
                                        byte[] receivedChunk = new byte[Configuration.CHUNKS_SIZE];
                                        in.read(receivedChunk);
                                        otherOut.write(receivedChunk);
                                        bytesToRead -= Configuration.CHUNKS_SIZE;
                                    }
                                    if (bytesToRead != 0) {
                                        byte[] finalChunk = new byte[bytesToRead];
                                        in.read(finalChunk);
                                        otherOut.write(finalChunk);
                                    }
                                    otherOut.flush();
                                }

//                                System.out.println(new String(receivedMessage.getFullMessage()));
                                break;
                            default:

                        }
                    }
                }
            }
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private void sendTextMessage(String message, String senderName) throws IOException {
        DataOutputStream otherSiteClientOut = new DataOutputStream(otherSiteClient.getOutputStream());
        if (this.clientSocket == null) {
            otherSiteClientOut.writeUTF(PrintColors.ANSI_RED + "OtherSiteClient doesn't selected" + PrintColors.ANSI_RESET);
        }
        else {
            otherSiteClientOut.writeUTF(PrintColors.ANSI_BLUE +
                    senderName + ':' + PrintColors.ANSI_RESET + "\n\t" +  message);
        }
    }

//    private static void filterText(String text){
//        String filterText="";
//        String regExp = "[A-Za-z@]";
//        filterText = text.replaceAll(regExp, "");
//    }

    private void checkOtherSiteClientAddress(String message) throws IOException {
        DataOutputStream out = new DataOutputStream(this.clientSocket.getOutputStream());
        try {
            Socket clientWithEnteredAddress = null;
            for (TCPServer connection : allConnections) {
                InetAddress otherSiteAddress = InetAddress.getByName(message);
                InetAddress connectionAddress = connection.clientSocket.getInetAddress();
                String otherSiteIP = connectionAddress.getHostAddress();
                String connectionIP = otherSiteAddress.getHostAddress();
                if (connectionIP.equals(otherSiteIP)) {
                    clientWithEnteredAddress = connection.clientSocket;
                    if(connection.otherSiteClient==null){
                        connection.otherSiteClient=this.clientSocket;
                    }
                    break;
                }
            }
            if (clientWithEnteredAddress != null) {
                this.otherSiteClient = clientWithEnteredAddress;
            }
//            else {
//                out.writeUTF(PrintColors.ANSI_RED +
//                        "Computer with entered address doesn't connect to the server!" + PrintColors.ANSI_RESET);
//            }

        } catch (UnknownHostException e) {
//            out.writeUTF(PrintColors.ANSI_RED +
//                    "Entered address is incorrect!" + PrintColors.ANSI_RESET);
            e.printStackTrace();
        }
    }

    private void sendBinaryFile(byte[] fileContent){
        try {
            OutputStream out = otherSiteClient.getOutputStream();
            out.write(fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
