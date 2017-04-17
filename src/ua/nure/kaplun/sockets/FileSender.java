package ua.nure.kaplun.sockets;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Anton on 11.03.2017.
 */
public class FileSender {
    public static void sendBinaryFileToOthersiteClient(String path, DataOutputStream out, String senderName, SendingPercentagesWriter writer, String sendingCommand){
        try{
            File sendingFile = new File(path);
            long fileLength = sendingFile.length();
            if(fileLength == (int)fileLength) {
                FileInputStream fileInput = new FileInputStream(sendingFile);
                int fullMessageLength = Message.getFullMessageLength((int)fileLength,
                        sendingCommand, senderName, sendingFile.getName());
                int messageHeaderLength = Message.getHeaderLength((int)fileLength,
                        sendingCommand, senderName, sendingFile.getName());
                if (fullMessageLength <= Configuration.CHUNKS_SIZE) {
                    byte[] fileContent = new byte[(int) fileLength];
                    fileInput.read(fileContent);
                    Message message = new Message(sendingCommand,
                            senderName, sendingFile.getName(), 0 ,fileContent);
                    out.write(message.getFullMessage());
                } else {
                    int bytesToRead = (int)fileLength;

                    //send first chunk with header
                    byte[] firstFileChunk = new byte[Configuration.CHUNKS_SIZE - messageHeaderLength];
                    fileInput.read(firstFileChunk);
                    bytesToRead -= firstFileChunk.length;
                    int postedPart=0;
                    int currentPercentages=0;
                    BufferedWriter consoleOut = new BufferedWriter(new OutputStreamWriter(new
                            FileOutputStream(java.io.FileDescriptor.out), "ASCII"), 512);

                    writer.writePercentages(currentPercentages, consoleOut);
                    Message message = new Message(sendingCommand,
                            senderName, sendingFile.getName(), fullMessageLength, firstFileChunk);
                    out.write(message.getFullMessage());
                    postedPart+=message.getMessageContent().length;
                    out.flush();

                    //send remaining chunks
                    byte[] chunk;
                    while(bytesToRead > Configuration.CHUNKS_SIZE){
                        chunk=new byte[Configuration.CHUNKS_SIZE];
                        fileInput.read(chunk);
                        bytesToRead -= Configuration.CHUNKS_SIZE;
                        out.write(chunk);
                        postedPart+=chunk.length;

                        if(currentPercentages < getPercentage(postedPart, (int)fileLength)){
                            currentPercentages = getPercentage(postedPart, (int)fileLength);
                            writer.writePercentages(currentPercentages, consoleOut);
                        }
                    }
                    if(bytesToRead > 0){
                        chunk = new byte[bytesToRead];
                        fileInput.read(chunk);
                        out.write(chunk);
                        postedPart+=chunk.length;
                        writer.writePercentages(getPercentage(postedPart, (int)fileLength), consoleOut);
                    }
                    System.out.println();
                }
                fileInput.close();
            }
            else{
                System.out.println(PrintColors.ANSI_RED + "This file is very big and can't be send!"
                        + PrintColors.ANSI_RESET);
            }
        } catch (FileNotFoundException e) {
            System.out.println(PrintColors.ANSI_RED + "Entered file doesn't exist!" + PrintColors.ANSI_RESET);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
    }


    public static File receiveBinaryFile(InputStream in, int bytesToRead, Message firstChunkMessage, String printedSenderName, String fileName, String dirName, SendingPercentagesWriter writer){
        File receivedFile=null;
        try {
            File dir = new File(dirName);
            dir.mkdir();
            receivedFile = new File(dir + "\\" + fileName);
            FileOutputStream fileOut = new FileOutputStream(receivedFile);
            int receivedPart = firstChunkMessage.getMessageContent().length;
            int currentPercentages=0;
            int fullFileSize = firstChunkMessage.getMessageLength() -
                    (Configuration.CHUNKS_SIZE - firstChunkMessage.getMessageContent().length);
            System.out.println(printedSenderName + "Receiving file " +
                    PrintColors.ANSI_CYAN + fileName + PrintColors.ANSI_RESET + "\n");
            BufferedWriter consoleOut = new BufferedWriter(new OutputStreamWriter(new
                    FileOutputStream(java.io.FileDescriptor.out), "ASCII"), 512);

            writer.writePercentages(currentPercentages, consoleOut);
            fileOut.write(firstChunkMessage.getMessageContent());

            currentPercentages = getPercentage(receivedPart, fullFileSize);
            writer.writePercentages(currentPercentages, consoleOut);
            while(bytesToRead >= Configuration.CHUNKS_SIZE){
                byte[] receivedChunk = new byte[Configuration.CHUNKS_SIZE];
                in.read(receivedChunk);
                fileOut.write(receivedChunk);
                bytesToRead-=Configuration.CHUNKS_SIZE;

                receivedPart += receivedChunk.length;
                if(currentPercentages < getPercentage(receivedPart, fullFileSize)){
                    currentPercentages = getPercentage(receivedPart, fullFileSize);
                    writer.writePercentages(currentPercentages, consoleOut);
                }
            }
            if(bytesToRead!=0){
                byte[] finalChunk = new byte[bytesToRead];
                in.read(finalChunk);
                fileOut.write(finalChunk);

                receivedPart += finalChunk.length;
                writer.writePercentages(getPercentage(receivedPart, fullFileSize), consoleOut);
                System.out.println();
            }
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return receivedFile;
    }

    public static double getRoundedPercentage(double receivedPart, double fullFileLength){
        double rawPercentage = receivedPart / fullFileLength * 100;
        return Math.rint(rawPercentage * 10.0)/10.0;
    }

    public static int getPercentage(double receivedPart, double fullFileLength){
        double receivingDoublePercentage = receivedPart/fullFileLength * 100;
        return (int)receivingDoublePercentage;
    }
}
