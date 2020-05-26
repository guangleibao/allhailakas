package bglutil.ahaa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Basic {
    public static String initRegion(String profileName) throws Exception {
        File config = new File(System.getProperty("user.home") + "/.aws/config");
        String regionCode = null;
        BufferedReader br = new BufferedReader(new FileReader(config));
        String line = br.readLine();
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
            line = br.readLine();
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
        br.close();
        return regionCode;
    }
}