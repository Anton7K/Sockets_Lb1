package ua.nure.kaplun.sockets;
import java.io.*;
import java.util.*;

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
                        String printedSenderName;
                        if(senderName.equals(Configuration.SERVER_NAME)){
                            printedSenderName = PrintColors.ANSI_PURPLE +
                                    "SERVER" + ':' + PrintColors.ANSI_RESET + "\n\t";
                        }else {
                            printedSenderName = PrintColors.ANSI_BLUE +
                                    senderName + ':' + PrintColors.ANSI_RESET + "\n\t";
                        }

                        switch (specialCommand){
                            case SpecialCommands.SEND_TEXT_MESSAGE_TO_OTHERSITE_CLIENT :
                                receiveTextMessage(in, bytesToRead, receivedMessage, printedSenderName);
                                break;
                            case SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT :
                                String fileName = receivedMessage.getFileName();
                                boolean isFileVideo = VideoFormats.videoFormatsList.contains(fileName.substring(fileName.lastIndexOf('.')));
                                if(isFileVideo){
                                    FileSender.receiveBinaryFile(in, bytesToRead, receivedMessage,
                                            printedSenderName, fileName, "ReceivedFiles_" + client.getClientName(),
                                            new ConsolePercentagesWriter());
                                }else {
                                    FileSender.receiveBinaryFile(in, bytesToRead, receivedMessage,
                                            printedSenderName, fileName, "ReceivedFiles_" + client.getClientName(),
                                            new NullPercentagesWriter());
                                }
                                break;
                            case SpecialCommands.SEND_SERIALIZABLE_DATA:
                                System.out.print(printedSenderName);
                                Hashtable<String, Date> receivedBirthdays = deserializeBirthdays(receivedMessage.getMessageContent());
                                Hashtable<String, Date> copiedBirthdays = (Hashtable<String, Date>)receivedBirthdays.clone();
                                Collection<Date> dates = receivedBirthdays.values();
                                ArrayList<Date> sortedDates = new ArrayList<>(dates);
                                Collections.sort(sortedDates);
                                 for(int i=0; i<sortedDates.size(); i++) {
                                    Date value = sortedDates.get(i);
                                    for (Map.Entry<String, Date> entry : copiedBirthdays.entrySet()) {
                                        if (entry.getValue() == value) {
                                            String key = entry.getKey();
                                            System.out.print("\n\t" + key + "=>" + value);
                                            copiedBirthdays.remove(key);
                                            break;
                                        }
                                    }
                                }
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

    private Hashtable<String, Date> deserializeBirthdays(byte[] serializableData) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(serializableData);
        ObjectInputStream oin = new ObjectInputStream(bin);
        Hashtable<String, Date> birthdays = new Hashtable<>();
        try {
            birthdays = (Hashtable<String, Date>)oin.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return birthdays;
    }
}
