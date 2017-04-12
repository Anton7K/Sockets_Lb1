package ua.nure.kaplun.sockets;

import java.io.IOException;
import java.io.Writer;

/**
 * Created by Anton on 12.04.2017.
 */
public class ConsolePercentagesWriter implements SendingPercentagesWriter {
    @Override
    public void writePercentages(int percentages, Writer out) {
        try {
            out.write("\r" + percentages + "%");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
