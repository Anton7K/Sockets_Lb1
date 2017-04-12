package ua.nure.kaplun.sockets;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Anton on 12.04.2017.
 */
public class VideoFormats {
    public static ArrayList<String> videoFormatsList = new ArrayList<>();
    static {
        String videoFormats = ".3gp, .avi, .mpg, .mov, .swf, .asf, .mp4, .wmv, .mts, .mkv, .flv";
        String[] videoFormatsArray = videoFormats.split(", ");
        videoFormatsList = new ArrayList<>(Arrays.asList(videoFormatsArray));
    }
}
