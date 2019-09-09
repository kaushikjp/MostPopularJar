package com.JFrog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

public class MostPopularJarFile {
    public static void main(String[] args) {
        try {
            //URL
            URL url = new URL("http://104.197.149.8/artifactory/api/search/aql");
            URLConnection connection = url.openConnection();

            String usernameColonPassword = "admin:e8dbIGUt5p";
            String basicAuthPayload = "Basic " + Base64.getEncoder().encodeToString(usernameColonPassword.getBytes());
            connection.addRequestProperty("Authorization", basicAuthPayload);

            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write("items.find(\n" +
                    "    {\n" +
                    "        \"name\":{\"$match\":\"*.jar\"}\n" +
                    "    }\n" +
                    " ).include(\"name\", \"stat.downloads\")");
            out.close();

            InputStream response = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
            StringBuilder sb = new StringBuilder();

            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            JSONObject json = new JSONObject(sb.toString());
            JSONArray resultsArr = json.getJSONArray("results");

            String mostPopularJarFile = null;
            String secondMostPopularJarFile =  null;

            long numMostDownloadsCount = 0;
            long numSecondMostDownloadsCount = 0;

            for (int i = 0; i < resultsArr.length(); i++) {
                JSONObject currJsonObj = resultsArr.getJSONObject(i);

                String strJarName = currJsonObj.getString("name");
                long numDownloads = currJsonObj.getJSONArray("stats").getJSONObject(0).getLong("downloads");

                if( numDownloads > numMostDownloadsCount ) {
                    // Make the old max the new 2nd max.
                    numSecondMostDownloadsCount = numMostDownloadsCount;
                    secondMostPopularJarFile = mostPopularJarFile;
                    // This is the new max.
                    numMostDownloadsCount = numDownloads;
                    mostPopularJarFile = strJarName;
                }
                // It's not the max, is it the 2nd max?
                else if( numDownloads > numSecondMostDownloadsCount ) {
                    numSecondMostDownloadsCount = numDownloads;
                    secondMostPopularJarFile = strJarName;
                }
            }
            String strOutput1 = "Most Popular JAR is \t\t \"%s\" has \"%d\" downloads";
            System.out.println(strOutput1.format(strOutput1, new Object[]{mostPopularJarFile, numMostDownloadsCount}));

            String strOutput2 = "Second Most Popular JAR is \t \"%s\" has \"%d\" downloads";
            System.out.println(strOutput1.format(strOutput2, new Object[]{secondMostPopularJarFile, numSecondMostDownloadsCount}));
        } catch (Exception e) {
            System.out.println("Error while calling JFrog REST Service");
            System.out.println(e);
        }
    }
}