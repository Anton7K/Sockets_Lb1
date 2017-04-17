package ua.nure.kaplun.sockets;

import ua.nure.kaplun.sockets.exceptions.MessageFormatException;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Anton on 15.03.2017.
 */
public class Message {
    private byte[] fullMessage;
    private byte[] messageContent;

    //keywords
    private int messageLength;
    private String command = null;
    private String senderName = null;
    private String fileName = null;

    private static final int MAX_KEYWORD_COUNT = 4;
    private static String separator = "@";

    public Message(String command, String senderName, byte[] messageContent) {
        String[] keyWords = new String[2];
        this.command = command;
        this.senderName = senderName;
//        messageLength =
        keyWords[0] = command;
        keyWords[1] = senderName;
        buildMessage(messageContent, 0, keyWords);
    }

    public Message(String command, String senderName, String fileName, int fullLength, byte[] fileBytes) {
        String[] keyWords = new String[3];
        this.command = command;
        this.senderName = senderName;
        this.fileName = fileName;
        keyWords[0] = command;
        keyWords[1] = senderName;
        keyWords[2] = fileName;
        buildMessage(fileBytes, fullLength, keyWords);
    }

    // constructor for receiving
    public Message(byte[] fullMessage) {
        try {
            defineKeyWords(fullMessage);
            this.fullMessage = fullMessage;

        } catch (MessageFormatException e) {
            e.printStackTrace();
        }

    }

    static int getMessageLength(DataInputStream is) throws IOException {
        byte[] mesLengthBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            byte readedInfo = is.readByte();

                mesLengthBytes[i] = readedInfo;

        }
//        byte[] separatorBytes = separator.getBytes();
//        for (int i = 0; i < separatorBytes.length; i++) {
//            if(is.read() != separatorBytes[i]){
//                return -1;
//            }
//        }
        return new BigInteger(mesLengthBytes).intValue();
    }

    static int getFullMessageLength(int messageContentLength, String... keyWords) {
        byte[] messageLengthBytes;
        int fullMessageLength = messageContentLength + (MAX_KEYWORD_COUNT) * (separator.getBytes().length);
        for (String keyWord : keyWords) {
            fullMessageLength += keyWord.getBytes().length;
        }
        messageLengthBytes = Message.intToByteArray(fullMessageLength);
        fullMessageLength = fullMessageLength + messageLengthBytes.length;
        return fullMessageLength;
    }

    static  int getHeaderLength(int messageContentLength, String... keyWords){
        int headerLength = 4 + (MAX_KEYWORD_COUNT) * (separator.getBytes().length);
        for (String keyWord : keyWords) {
            headerLength += keyWord.getBytes().length;
        }
        return headerLength;
    }

    public void setCommand(String command) {
        this.command = command;
        byte[] commandBytes = command.getBytes();
        ArrayList<Integer> separatorsIndexes = getSeparatorsIndexes(fullMessage);
        byte[] fullMessageCopy = new byte[fullMessage.length];
        int replaseStart = separatorsIndexes.get(0)+1;
        int replaseEnd = separatorsIndexes.get(1)-1;
        System.arraycopy(fullMessage, 0, fullMessageCopy, 0, replaseStart);
        System.arraycopy(commandBytes, 0, fullMessageCopy, replaseStart, commandBytes.length);
        System.arraycopy(fullMessage, replaseEnd+1, fullMessageCopy, replaseEnd+1, fullMessage.length-(replaseEnd+1));
        fullMessage=fullMessageCopy;
    }

    public int getMessageLength() {
        return messageLength;
    }

    public byte[] getFullMessage() {
        return fullMessage;
    }

    public String getCommand() {
        return command;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getFileName() {
        return fileName;
    }

    public static String getSeparator() {
        return separator;
    }

    public byte[] getMessageContent() {
        ArrayList<Integer> separatorIndexes = getSeparatorsIndexes(fullMessage);
        int lastSeparatorIndex = Collections.max(separatorIndexes);
        int messageContentBeginIndex = lastSeparatorIndex + separator.getBytes().length;
        byte[] messageContent = new byte[fullMessage.length - messageContentBeginIndex];
        System.arraycopy(fullMessage, messageContentBeginIndex, messageContent, 0, messageContent.length);
        this.messageContent = messageContent;
        return this.messageContent;
    }

    private int putKeyWord(int curIndex, byte[] source) {
        byte[] separatorBytes = separator.getBytes();

//        System.arraycopy(separatorBytes, 0, fullMessage, curIndex, separatorBytes.length);
//        curIndex+=separatorBytes.length;
        System.arraycopy(source, 0, fullMessage, curIndex, source.length);
        curIndex += source.length;
        System.arraycopy(separatorBytes, 0, fullMessage, curIndex, separatorBytes.length);
        curIndex += separatorBytes.length;
        return curIndex;
    }

    private void buildMessage(byte[] messageContent, int messageLength, String... keyWords) {
        byte[] messageLengthBytes;
        int firstChunkSize;
        if (messageLength==0) {
            int messageContentLength = messageContent.length;
            this.messageLength = getFullMessageLength(messageContentLength, keyWords);

            firstChunkSize = this.messageLength;
        }else{
            this.messageLength=messageLength;
            firstChunkSize = Configuration.CHUNKS_SIZE;
        }
        fullMessage = new byte[firstChunkSize];
        messageLengthBytes = Message.intToByteArray(this.messageLength);
        int currentIndex = 0;

        //write all key words

        //write message length keyWord
        System.arraycopy(messageLengthBytes, 0, fullMessage, currentIndex, messageLengthBytes.length);
        currentIndex += messageLengthBytes.length;
        System.arraycopy(separator.getBytes(), 0, fullMessage, currentIndex, separator.getBytes().length);
        currentIndex += separator.getBytes().length;

        //write another keywords
        for (String keyWord : keyWords) {
            byte[] keyWordBytes = keyWord.getBytes();
            currentIndex = putKeyWord(currentIndex, keyWordBytes);
        }
        //write remaining separators
        int remainingSeparatorsCount = MAX_KEYWORD_COUNT - keyWords.length - 1;
        for (int i = remainingSeparatorsCount; i > 0; i--) {
            System.arraycopy(separator.getBytes(), 0, fullMessage, currentIndex, separator.getBytes().length);
            currentIndex += separator.getBytes().length;
        }

        //write file content
        System.arraycopy(messageContent, 0, fullMessage, currentIndex, messageContent.length);
    }

    private ArrayList<Integer> getSeparatorsIndexes(byte[] message) {
        ArrayList<Integer> separatorIndexes = new ArrayList<>();
        byte[] separatorBytes = separator.getBytes();
//        int[] separatorIndexes=new int[MAX_KEYWORD_COUNT];
        for (int i = 0; i < message.length; i++) {
            if (message[i] == separatorBytes[0]) {
                int beginIndex = i;
                int currentIndex = beginIndex;
                boolean findSeparator = true;
                for (int j = 1; j < separatorBytes.length; j++, currentIndex++) {
                    if (message[currentIndex] != separatorBytes[j]) {
                        findSeparator = false;
                        break;
                    }
                }
                if (findSeparator) {
                    separatorIndexes.add(beginIndex);
                    if (separatorIndexes.size() >= MAX_KEYWORD_COUNT) {
                        break;
                    }
                }
            }
        }
        return separatorIndexes;
    }


    private void defineKeyWords(byte[] fullMessage) throws MessageFormatException {
        ArrayList<Integer> separatorsIndexes = getSeparatorsIndexes(fullMessage);
        int separatorLength = separator.getBytes().length;

        if (separatorsIndexes.size() == MAX_KEYWORD_COUNT) {
            byte[] lengthBytes = new byte[separatorsIndexes.get(0)];
            byte[] commandBytes = new byte[separatorsIndexes.get(1) - (separatorsIndexes.get(0) + separatorLength)];
            byte[] clientBytes = new byte[separatorsIndexes.get(2) - (separatorsIndexes.get(1) + separatorLength)];
            byte[] fileNameBytes = new byte[separatorsIndexes.get(3) - (separatorsIndexes.get(2) + separatorLength)];

            if (lengthBytes.length > 0) {
                System.arraycopy(fullMessage, 0, lengthBytes, 0, lengthBytes.length);
                this.messageLength = new BigInteger(lengthBytes).intValue();
            }
            if (commandBytes.length > 0) {
                System.arraycopy(fullMessage, separatorsIndexes.get(0) + separatorLength, commandBytes, 0, commandBytes.length);
                this.command = new String(commandBytes);
            }
            if (clientBytes.length > 0) {
                System.arraycopy(fullMessage, separatorsIndexes.get(1) + separatorLength, clientBytes, 0, clientBytes.length);
                this.senderName = new String(clientBytes);
            }
            if (fileNameBytes.length > 0) {
                System.arraycopy(fullMessage, separatorsIndexes.get(2) + separatorLength, fileNameBytes, 0, fileNameBytes.length);
                this.fileName = new String(fileNameBytes);
            }

        } else {
            throw new MessageFormatException("Incorrect message format!");
        }
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }
}
