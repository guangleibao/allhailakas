package bglutil.ahaa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import com.amazonaws.util.json.Jackson;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.Credentials;
import software.amazon.awssdk.services.sts.model.GetFederationTokenRequest;
import software.amazon.awssdk.services.sts.model.GetFederationTokenResponse;

public class AllHailAkAs {
    public static void main(String args[]) throws Exception{
        // Log setting
        Logger.getRootLogger().setLevel(Level.OFF);
        // Parameters
        String serviceName = args[0];
        String profile = args[1];
        // Init
        String regionCode = Basic.initRegion(profile);
        Region region = Region.of(regionCode);
        ProfileCredentialsProvider pcp = null;
        if (profile.equals("default")) {
            pcp = ProfileCredentialsProvider.create();
        } else {
            pcp = ProfileCredentialsProvider.create(profile);
        }
        AwsCredentialsProviderChain acpc = AwsCredentialsProviderChain.builder().addCredentialsProvider(pcp)
                .addCredentialsProvider(InstanceProfileCredentialsProvider.create()).build();
        StsClient sts = StsClient.builder().credentialsProvider(acpc).region(region).build();
        // Set the STS Fed Username
        String username = sts.getCallerIdentity().arn().replaceAll("arn.*/", "") + "-fed";
        // Set the policy mask
        String policy = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Action\":\"*\",\"Resource\":\"*\"}]}";
        // Get the STS token
        GetFederationTokenRequest getFederationTokenRequest = GetFederationTokenRequest.builder()
                .durationSeconds(129600).name(username).policy(policy).build();
        GetFederationTokenResponse federationTokenResponse = sts.getFederationToken(getFederationTokenRequest);
        Credentials tokenCredentials = federationTokenResponse.credentials();
        // Make the SSO URL
        String tempAK = tokenCredentials.accessKeyId();
        String tempAS = tokenCredentials.secretAccessKey();
        String tempTK = tokenCredentials.sessionToken();
        String loginURL = null;
        String consoleURL = null;
        String signInURL = null;
        String sessionJson = null;
        String getSigninTokenURL = null;
        String issuerURL = null;
        if (regionCode.startsWith("cn-")) {
            issuerURL = "http://www.amazonaws.cn";
            consoleURL = "https://console.amazonaws.cn/" + (serviceName == null ? "console" : serviceName)
                    + "/home?region=" + regionCode + "#"; // Place to go
            signInURL = "https://signin.amazonaws.cn/federation";
        } else {
            issuerURL = "http://aws.amazon.com";
            consoleURL = "https://console.aws.amazon.com/" + (serviceName == null ? "console" : serviceName)
                    + "/home?region=" + regionCode + "#"; // Place to go
            signInURL = "https://signin.aws.amazon.com/federation"; // Place to handle next request
        }
        sessionJson = String.format("{\"%1$s\":\"%2$s\",\"%3$s\":\"%4$s\",\"%5$s\":\"%6$s\"}", "sessionId", tempAK,
                "sessionKey", tempAS, "sessionToken", tempTK);
        getSigninTokenURL = signInURL + "?Action=getSigninToken" + "&SessionType=json&Session="
                + URLEncoder.encode(sessionJson, "UTF-8");
        URL url = new URL(getSigninTokenURL);
        URLConnection conn = url.openConnection();
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String returnContent = bufferReader.readLine();
        String signinToken = Jackson.jsonNodeOf(returnContent).get("SigninToken").toString();
        String signinTokenParameter = "&SigninToken=" + URLEncoder.encode(signinToken, "UTF-8");
        String issuerParameter = "&Issuer=" + URLEncoder.encode(issuerURL, "UTF-8");
        String destinationParameter = "&Destination=" + URLEncoder.encode(consoleURL, "UTF-8");
        loginURL = signInURL + "?Action=login" + signinTokenParameter + issuerParameter + destinationParameter;
        System.out.println(loginURL);
    }
}