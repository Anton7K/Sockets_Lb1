package ua.nure.kaplun.sockets;


import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Anton on 17.04.2017.
 */
public class SSLServer extends Thread {
    private SSLSocket clientSocket;

    public static void main(String[] args) {
//        System.setProperty("javax.net.ssl.trustStore", "cacerts.jks");
//        System.setProperty("javax.net.ssl.trustStorePassword","123456");
        System.setProperty("javax.net.ssl.keyStore", "sslKeys.keystore");
        System.setProperty("javax.net.ssl.keyStorePassword", "123456");

        try {
            SSLServerSocketFactory serverFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket server = (SSLServerSocket) serverFactory.createServerSocket(Configuration.SSL_PORT);
            new SSLServer((SSLSocket) server.accept());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public SSLServer(SSLSocket clientSocket) {
        this.clientSocket = clientSocket;
        start();
    }

    @Override
    public void run() {
        try {
            InputStream in = clientSocket.getInputStream();
            DataInputStream din = new DataInputStream(in);
            while (true) {


                int messageLength = Message.getMessageLength(din);
                if (messageLength != -1) {
                    byte[] messageLengthBytes = Message.intToByteArray(messageLength);
                    int firstChunkLength = messageLength <= Configuration.CHUNKS_SIZE ? messageLength : Configuration.CHUNKS_SIZE;

                    byte[] receivedData = new byte[firstChunkLength];
                    int curIndex = 0;
                    int bytesToRead = messageLength;

                    System.arraycopy(messageLengthBytes, 0, receivedData, curIndex, messageLengthBytes.length);
                    curIndex += messageLengthBytes.length;

                    din.readFully(receivedData, curIndex, receivedData.length - curIndex);
                    bytesToRead -= receivedData.length;

                    Message receivedMessage = new Message(receivedData);
                    String specialCommand = receivedMessage.getCommand();
                    String fileName = receivedMessage.getFileName();
                    String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
                    String senderName = receivedMessage.getSenderName();
                    String printedSenderName = PrintColors.ANSI_BLUE +
                            senderName + ':' + PrintColors.ANSI_RESET + "\n\t";
                    boolean isFileVideo = VideoFormats.videoFormatsList.contains(fileName.substring(fileName.lastIndexOf('.')));
                    if(fileExtension.equals(".bmp")){
                        FileSender.receiveBinaryFileAccelerately(in, bytesToRead, receivedMessage,
                                printedSenderName, fileName, "ReceivedFiles_SSLServer",
                                new NullPercentagesWriter());
                    }
                    if (isFileVideo) {
                        FileSender.receiveBinaryFile(in, bytesToRead, receivedMessage,
                                printedSenderName, fileName, "ReceivedFiles_SSLServer",
                                new ConsolePercentagesWriter());
                    } else if(!fileExtension.equals(".bmp")){
                        FileSender.receiveBinaryFile(in, bytesToRead, receivedMessage,
                                printedSenderName, fileName, "ReceivedFiles_SSLServer",
                                new NullPercentagesWriter());
                    }
                }
            }
//            String mes = din.readUTF();
//            System.out.println(mes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
