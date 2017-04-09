package ua.nure.kaplun.sockets;
import java.io.*;

/**
 * Created by Anton on 23.02.2017.
 */
public class ReadTcpSocketThread extends Thread{
    private TCPClient client;

    public ReadTcpSocketThread(TCPClient client){
        this.client = client;
    }
    public void run() {
        try {
            InputStream in = client.getSocket().getInputStream();
            DataInputStream din = new DataInputStream(in);
            while (true) {
//                if(in.available()>0){
                    int messageLength = Message.getMessageLength(din);
                    if(messageLength != -1){
//                        System.out.println(messageLength);
                        byte[] messageLengthBytes = Message.intToByteArray(messageLength);
                        int firstChunkLength = messageLength <= Configuration.CHUNKS_SIZE ? messageLength : Configuration.CHUNKS_SIZE;

                        byte[] receivedData = new byte[firstChunkLength];
                        int curIndex=0;
                        int bytesToRead=messageLength;

                        System.arraycopy(messageLengthBytes, 0, receivedData, curIndex, messageLengthBytes.length);
                        curIndex+=messageLengthBytes.length;

                        in.read(receivedData, curIndex, receivedData.length - curIndex);
                        bytesToRead -= receivedData.length;

                        Message receivedMessage = new Message(receivedData);
                        String specialCommand = receivedMessage.getCommand();
                        String senderName = receivedMessage.getSenderName();
                        String printedSenderName = PrintColors.ANSI_BLUE +
                                senderName + ':' + PrintColors.ANSI_RESET + "\n\t";


                        switch (specialCommand){
                            case SpecialCommands.SEND_TEXT_MESSAGE_TO_OTHERSITE_CLIENT :
                                receiveTextMessage(in, bytesToRead, receivedMessage, printedSenderName);
                                break;
                            case SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT :
                                FileSender.receiveBinaryFile(in, bytesToRead, receivedMessage, printedSenderName, receivedMessage.getFileName(), "ReceivedFiles_" + client.getClientName());
                                break;
                            default:
                        }
                    }
//                }
//                    String line = din.readUTF();
//                    System.out.println(line);

                    /*int messageLength = Message.getMessageLength(in);
                    byte[] messageLengthBytes = Message.intToByteArray(messageLength);
                    byte[] receivedData = new byte[messageLength];

                    int curIndex = 0;
                    System.arraycopy(messageLengthBytes, 0, receivedData, curIndex, messageLengthBytes.length);
                    curIndex += messageLengthBytes.length;
                    din.readFully(receivedData, curIndex, messageLength - curIndex);

                    System.out.println(Arrays.toString(receivedData));
                    Message receivedMessage = new Message(receivedData);
                    String specialCommand = receivedMessage.getCommand();
                    String senderName = receivedMessage.getSenderName();
                    String printedSenderName = PrintColors.ANSI_BLUE +
                            senderName + ':' + PrintColors.ANSI_RESET + "\n\t";

                    switch (specialCommand) {
                        case SpecialCommands.SEND_TEXT_MESSAGE_TO_OTHERSITE_CLIENT:
                            System.out.println(printedSenderName + receivedMessage.getMessageContent());
                            break;
                        case SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT:
                            String fileName = receivedMessage.getFileName();
//                        StringBuilder fileNameBuilder=new StringBuilder();
//                        int i;
//                        for(i=0; i<message.length(); i++){
//                            if(message.charAt(i)=='@'){
//                                break;
//                            }else{
//                                fileNameBuilder.append(message.charAt(i));
//                            }
//                        }
//                        fileName=fileNameBuilder.toString();
//                        byte[] receivedBytes = message.substring(i++).getBytes();
                            System.out.println(printedSenderName + "Receiving file" + fileName + " .....");
                            receiveBinaryFile(receivedMessage.getMessageContent(), fileName);
                            System.out.println(printedSenderName + fileName + "received successfully");
                            break;
                        default:
                    }*/
                }
//                String line = in.readUTF();
//                System.out.println(line);
        }
            catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void receiveTextMessage(InputStream in, int bytesToRead, Message firstChunkMessage, String printedSenderName) throws IOException {
        System.out.println(printedSenderName + new String(firstChunkMessage.getMessageContent()));
        while(bytesToRead >= Configuration.CHUNKS_SIZE){
            byte[] receivedChunk = new byte[Configuration.CHUNKS_SIZE];
            in.read(receivedChunk);
            System.out.print(new String(receivedChunk));
            bytesToRead-=Configuration.CHUNKS_SIZE;
        }
        if(bytesToRead!=0){
            byte[] finalChunk = new byte[bytesToRead];
            in.read(finalChunk);
            System.out.print(new String(finalChunk));
        }
    }

}
