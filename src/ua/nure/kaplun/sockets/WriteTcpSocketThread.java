package ua.nure.kaplun.sockets;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;

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
                        boolean isFileVideo = VideoFormats.videoFormatsList.contains(filePath.substring(filePath.lastIndexOf('.')));
                        if(isFileVideo){
                            FileSender.sendBinaryFileToOthersiteClient(filePath, dOut, client.getClientName(), new ConsolePercentagesWriter());
                        }else {
                            FileSender.sendBinaryFileToOthersiteClient(filePath, dOut, client.getClientName(), new NullPercentagesWriter());
                        }
                        break;
                    case SpecialCommands.SEND_SERIALIZABLE_DATA:
                        Hashtable<String, Date> birthDays = generateBirthdayDays();
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(buffer);
                        oos.writeObject(birthDays);
                        oos.close();
                        buffer.close();
                        byte[] serializableData = buffer.toByteArray();

                        Message serializableMessage = new Message(SpecialCommands.SEND_SERIALIZABLE_DATA, client.getClientName(), serializableData);
                        out.write(serializableMessage.getFullMessage());
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

    private static Hashtable<String, Date> generateBirthdayDays(){
        Hashtable <String, Date> birthdayDays= new Hashtable<>();
        birthdayDays.put("Иванов", new GregorianCalendar(1996, 7, 2).getTime());
        birthdayDays.put("Петров", new GregorianCalendar(1997, 5, 3).getTime());
        birthdayDays.put("Сидоров", new GregorianCalendar(1995, 10, 27).getTime());
        birthdayDays.put("Горбушин", new GregorianCalendar(1997, 8, 11).getTime());
        birthdayDays.put("Грядин", new GregorianCalendar(1996, 1, 15).getTime());
        birthdayDays.put("Зубов", new GregorianCalendar(1996, 2, 24).getTime());
        return birthdayDays;
    }

}
