package com.project2.util;

public class GeneralUtil {
    // utility method to get the base url
    public static String getBaseUrl(int port) {
        return "http://localhost:" + port + "/api";
    }
}
