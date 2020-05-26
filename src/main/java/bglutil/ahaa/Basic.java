package bglutil.ahaa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;

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

    public static void copyFile(String source, String dest) throws Exception {
        InputStream inStream = null;
        OutputStream outStream = null;

        File file1 = new File(source);
        File file2 = new File(dest);

        inStream = new FileInputStream(file1);
        outStream = new FileOutputStream(file2);

        byte[] buffer = new byte[1024];

        int length;
        while ((length = inStream.read(buffer)) > 0) {
            outStream.write(buffer, 0, length);
        }

        if (inStream != null)
            inStream.close();
        if (outStream != null)
            outStream.close();
    }
}