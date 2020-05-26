package bglutil.ahaa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.time.Instant;
import java.util.Hashtable;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.AccessKey;
import software.amazon.awssdk.services.iam.model.AccessKeyMetadata;
import software.amazon.awssdk.services.iam.model.DeleteAccessKeyRequest;

public class RotateAk {
    public static void main(String args[]) throws Exception {
        // Log setting
        Logger.getRootLogger().setLevel(Level.OFF);
        // Parameters
        String profile = args[0];
        // Init
        String regionCode = Basic.initRegion(profile);
                // China Region?
                URI endpointNew = null;
                Region region = null;
                    
                    if(regionCode.startsWith("cn-")){
                        endpointNew = new URI("https://iam.cn-north-1.amazonaws.com.cn");
                        region = Region.of("cn-north-1");
                    }
                    else{
                        endpointNew = new URI("https://iam.amazonaws.com");
                        region = Region.of("us-east-1");
                    }
        ProfileCredentialsProvider pcp = null;
        if (profile.equals("default")) {
            pcp = ProfileCredentialsProvider.create();
        } else {
            pcp = ProfileCredentialsProvider.create(profile);
        }
        AwsCredentialsProviderChain acpc = AwsCredentialsProviderChain.builder().addCredentialsProvider(pcp)
                .addCredentialsProvider(InstanceProfileCredentialsProvider.create()).build();

        IamClient iam = IamClient.builder().credentialsProvider(acpc).region(region)
            .endpointOverride(endpointNew)
        .build();
        AccessKey ak = iam.createAccessKey().accessKey();
        // Backup
        String home = System.getProperty("user.home");
        String credFile = home + "/.aws/credentials";
        String credFileBackup = home + "/.aws/credentials_ahakas_backup";
        Basic.copyFile(credFile, credFileBackup);
        System.out.println("Rotating AK/AS");
        System.out.println("Credentials file backed up to " + credFileBackup);
        // Update single AK.
        BufferedReader br = new BufferedReader(new FileReader(new File(credFile)));
        StringBuffer sb = new StringBuffer();
        String line = null;
        line = br.readLine();
        boolean found = false;
        String oldAccessKeyId = null;
        String oldAccessKeySecret = null;
        while (line != null) {
            // Profile there?
            if (!found && line.matches("^\\[ {0,}" + profile + ".*\\]$")) {
                found = true;
            }
            // Update AK
            if (found && line.matches("^aws_access_key_id {0,}= {0,}.*$")) {
                oldAccessKeyId = line.replace("aws_access_key_id", "").replace("=", "").trim();
                sb.append("aws_access_key_id = " + ak.accessKeyId() + "\n");
            }
            // Update AS
            else if (found && line.matches("^aws_secret_access_key {0,}= {0,}.*$")) {
                found = false;
                oldAccessKeySecret = line.replace("aws_secret_access_key", "").replace("=", "").trim();
                sb.append("aws_secret_access_key = " + ak.secretAccessKey() + "\n");
            } else {
                sb.append(line + "\n");
            }
            line = br.readLine();
        }
        br.close();
        String newCredFile = new String(sb);
        // Batch replaces all OLD AK/AS with NEW ones.
        String akasUpdated = newCredFile.replaceAll(oldAccessKeyId.replaceAll("\\+", "\\\\+"), ak.accessKeyId())
                .replaceAll(oldAccessKeySecret.replaceAll("\\+", "\\\\+"), ak.secretAccessKey());
        FileWriter fw = new FileWriter(credFile, false);
        fw.write(akasUpdated);
        fw.close();
        // Remove old credentials
        Instant minDate = null;
        Hashtable<Instant, String> dateToKey = new Hashtable<Instant, String>();
        for (AccessKeyMetadata akm : iam.listAccessKeys().accessKeyMetadata()) {
            dateToKey.put(akm.createDate(), akm.accessKeyId());
            if (minDate == null) {
                minDate = akm.createDate();
            } else {
                if (minDate.compareTo(akm.createDate()) > 0) {
                    minDate = akm.createDate();
                }
            }
        }
        iam.deleteAccessKey(DeleteAccessKeyRequest.builder().accessKeyId(dateToKey.get(minDate)).build());
        System.out.println("Old access key removed");
    }
}