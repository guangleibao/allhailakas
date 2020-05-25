package bglutil.ahaa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Basic {
    public static String initRegion(String profileName) {
        File config = new File(System.getProperty("user.home") + "/.aws/config");
        String regionCode = null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(config));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String line = null;
        try {
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int profileReady = 0;
        String profile = null;
        String region = null;
        while (line != null) {
            if (line.matches("^\\[ {0,}profile.*\\]$")) {
                profile = line.replaceAll("\\[ {0,}profile +", "").replaceAll(" {0,}\\] {0,}$", "");
                profileReady = 1;
            } else if (line.matches("^\\[ {0,}default {0,}]$")) {
                profile = "default";
                profileReady = 1;
            }
            try {
                line = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (line != null && line.matches("^ {0,}region {0,}=.*$")) {
                region = line.replaceAll(" {0,}region {0,}= {0,}", "").replaceAll(" {0,}$", "");
                if (profileReady == 1) {
                    if (profile.equals(profileName)) {
                        regionCode = region;
                    }
                    profileReady = 0;
                }
            }
        }
        return regionCode;
    }
}