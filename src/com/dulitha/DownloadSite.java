package com.dulitha;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class DownloadSite {

    public static void DownloadWebPage(String webpage)
    {
        Set<String> set = new HashSet<>();

        String PATH = "/Users/erandikiriweldeniya/Documents/Dulitha/Professional/tretton37/tretton37_website/";
        String directoryName = PATH;
        String fileName = "index.html";

        File directory = new File(directoryName);
        if (! directory.exists()){
            directory.mkdirs();
        }

        File file = new File(directoryName + "/" + fileName);

        try {

            // Create URL object
            URL url = new URL(webpage);
            BufferedReader readr =
                    new BufferedReader(new InputStreamReader(url.openStream()));

            // Enter filename in which you want to download
            BufferedWriter writer =
                    new BufferedWriter(new FileWriter(file.getAbsoluteFile()));

            // read each line from stream till end
            String line;
            while ((line = readr.readLine()) != null) {
                writer.write(line);
            }

            readr.close();
            writer.close();
            System.out.println("Successfully Downloaded.");
        }

        // Exceptions
        catch (MalformedURLException mue) {
            System.out.println("Malformed URL Exception raised");
        }
        catch (IOException ie) {
            System.out.println("IOException raised");
        }
    }

    public static void main(String args[]){
        String url = "https://tretton37.com/";
        DownloadWebPage(url);
    }
}
