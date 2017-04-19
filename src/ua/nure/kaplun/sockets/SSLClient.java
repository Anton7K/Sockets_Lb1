package ua.nure.kaplun.sockets;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Anton on 17.04.2017.
 */
public class SSLClient {
    public static void main(String[] args){
        try {
            System.setProperty("javax.net.ssl.trustStore", "cacerts.jks");
            System.setProperty("javax.net.ssl.trustStorePassword","123456");

            SSLSocketFactory clientFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            SSLSocket client = (SSLSocket)clientFactory.createSocket(Configuration.SERVER_NAME, Configuration.SSL_PORT);
//            byte[] message = "Hello".getBytes();
            OutputStream out = client.getOutputStream();
            DataOutputStream dout = new DataOutputStream(out);
            FileSender.sendBinaryFileToOthersiteClient("E:\\FilesToSend\\tiger.bmp", dout, "vasya", new NullPercentagesWriter(), SpecialCommands.SEND_BINARY_FILE_TO_OTHERSITE_CLIENT);
//            dout.writeUTF("hello");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
