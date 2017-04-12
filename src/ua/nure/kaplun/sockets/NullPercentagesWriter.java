package ua.nure.kaplun.sockets;

import java.io.Writer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Anton on 12.04.2017.
 */
public class NullPercentagesWriter implements SendingPercentagesWriter{
    @Override
    public void writePercentages(int percentages, Writer out) {
    }
}
