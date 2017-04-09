package ua.nure.kaplun.sockets;

import java.io.*;

/**
 * Created by Anton on 11.03.2017.
 */
public class FileSender {
    public static void sendBinaryFileToOthersiteClient(String path, DataOutputStream out, String senderName){
        try{
            File sendingFile = new File(path);
            long fileLength = sendingFile.length();
            if(fileLength == (int)fileLength) {
                FileInputStream fileInput = new FileInputStream(sendingFile);
                int fullMessageLength = Message.getFullMessageLength((int)fileLength,
                        SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT, senderName, sendingFile.getName());
                int messageHeaderLength = Message.getHeaderLength((int)fileLength,
                        SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT, senderName, sendingFile.getName());
                if (fullMessageLength <= Configuration.CHUNKS_SIZE) {
                    byte[] fileContent = new byte[(int) fileLength];
                    fileInput.read(fileContent);
                    Message message = new Message(SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT,
                            senderName, sendingFile.getName(), 0 ,fileContent);
                    out.write(message.getFullMessage());
                } else {
                    int bytesToRead = (int)fileLength;

                    //send first chunk with header
                    byte[] firstFileChunk = new byte[Configuration.CHUNKS_SIZE - messageHeaderLength];
                    fileInput.read(firstFileChunk);
                    bytesToRead -= firstFileChunk.length;
                    Message message = new Message(SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT,
                            senderName, sendingFile.getName(), fullMessageLength, firstFileChunk);
                    out.write(message.getFullMessage());
                    out.flush();

                    //send remaining chunks
                    byte[] chunk;
                    while(bytesToRead > Configuration.CHUNKS_SIZE){
                        chunk=new byte[Configuration.CHUNKS_SIZE];
                        fileInput.read(chunk);
                        bytesToRead -= Configuration.CHUNKS_SIZE;
                        out.write(chunk);
                    }
                    if(bytesToRead > 0){
                        chunk = new byte[bytesToRead];
                        fileInput.read(chunk);
                        out.write(chunk);
                    }
                }
                fileInput.close();
//                byte[] specialCommandBytes = SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT.getBytes();
//                byte[] senderNameBytes = senderName.getBytes();
//                byte[] fileNameBytes = sendingFile.getName().getBytes();
//                byte nameSeparator = (byte) '@';
//
//                int additionalInfoLength = specialCommandBytes.length + senderNameBytes.length +
//                        fileNameBytes.length + 3;
//                byte[] fileBytes = new byte[(int) fileLength + additionalInfoLength];
//
//                for(int i =0; i<specialCommandBytes.length; i++){
//                    fileBytes[i]=specialCommandBytes[i];
//                }
//                fileBytes[4] = nameSeparator;
//                int currentIndex=5;
//                for(int i=0; currentIndex<senderNameBytes.length; currentIndex++, i++){
//                    fileBytes[currentIndex] = senderNameBytes[i];
//                }
//                fileBytes[currentIndex] = nameSeparator;
//                currentIndex++;
//                for (int i=0; i<fileNameBytes.length; i++, currentIndex++){
//                    fileBytes[currentIndex]=fileNameBytes[i];
//                }
//                fileBytes[currentIndex] = nameSeparator;
//                currentIndex++;
//                fileInput.read(fileBytes, currentIndex ,fileBytes.length-currentIndex);
//                out.write(fileBytes);
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

//    public static byte[] receiveBinaryFile(InputStream in){
//        byte[] fileContent = new byte[8192];
//        try {
//            in.read(fileContent);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return fileContent;
//    }

    public static File receiveBinaryFile(InputStream in, int bytesToRead, Message firstChunkMessage, String printedSenderName, String fileName, String dirName){
        File receivedFile=null;
        try {
            File dir = new File(dirName);
            dir.mkdir();
            receivedFile = new File(dir + "\\" + fileName);
            FileOutputStream fileOut = new FileOutputStream(receivedFile);

            fileOut.write(firstChunkMessage.getMessageContent());
            while(bytesToRead >= Configuration.CHUNKS_SIZE){
                byte[] receivedChunk = new byte[Configuration.CHUNKS_SIZE];
                in.read(receivedChunk);
                fileOut.write(receivedChunk);
                bytesToRead-=Configuration.CHUNKS_SIZE;
            }
            if(bytesToRead!=0){
                byte[] finalChunk = new byte[bytesToRead];
                in.read(finalChunk);
                fileOut.write(finalChunk);
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


}
