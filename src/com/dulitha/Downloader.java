package com.dulitha;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Downloader {

    private String hostName;
    private String saveDir;
    private static Set<String> globalUrlSet = new HashSet<>();


    private static final int BUFFER_SIZE = 4096;


    public Downloader(String hostName, String saveDir) {
        this.hostName = hostName;
        this.saveDir = saveDir;
        this.startDownload(this.hostName);
    }

    private void startDownload(String urlString) {
        try {
            URL url = new URL(urlString);

            var urlList = downloadAndGetUrlList(url);

            for (var urlItem : urlList) {
                startDownload(urlItem);
            }
        } catch (IOException ie) {
            System.out.println("IO Exception occur");
        }
    }

    private Set<String> downloadAndGetUrlList(URL url) {
        Set<String> pageUrls = new HashSet<>();
        pageUrls = downloadResource(url);
        return pageUrls;
    }

    private Set<String> downloadResource(URL url) {
        Set<String> set = new HashSet<>();

        var extension = FilenameUtils.getExtension(url.getPath()); // -> xml

        var directoryPath = !extension.isEmpty() ? url.getPath().substring(0, url.getPath().lastIndexOf("/")) : url.getPath();
        var fileName = !extension.isEmpty() ? FilenameUtils.getName(url.getPath()) : "index.html";


        File directory = new File(this.saveDir + File.separator + directoryPath + File.separator );

        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(this.saveDir + File.separator + directoryPath + File.separator + fileName);

        try {

            // Create URL object
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String contentType = httpConn.getContentType();

                if (contentType.contains("text/html")) {
                    BufferedReader readr =
                            new BufferedReader(new InputStreamReader(url.openStream()));

                    // Enter filename in which you want to download
                    BufferedWriter writer =
                            new BufferedWriter(new FileWriter(file.getAbsoluteFile()));

                    // read each line from stream till end
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = readr.readLine()) != null) {
                        sb.append(line);
                        writer.write(line);
                    }
                    readr.close();
                    writer.close();
                    set = parseHtmlFileForTags(sb);
                } else {
                    InputStream inputStream = httpConn.getInputStream();
                    FileOutputStream outputStream = new FileOutputStream(file.getAbsoluteFile());


                    // read each line from stream till end
                    int bytesRead = -1;
                    byte[] buffer = new byte[BUFFER_SIZE];
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();
                }
                System.out.println("Successfully Downloaded.");
            }
            httpConn.disconnect();
        }

        // Exceptions
        catch (MalformedURLException mue) {
            System.out.println("Malformed URL Exception raised");
        } catch (IOException ie) {
            System.out.println("IOException raised");
        }

        return set;
    }

    private Set<String> parseHtmlFileForTags(StringBuilder sb) {
        Set<String> urlSet = new HashSet<>();
        Pattern pattern = Pattern.compile("^(?:[a-z]+:)?//");

        Document document = Jsoup.parse(sb.toString());
        Elements imageTags = document.select("img");
        Elements anchorTags = document.select("a");

        anchorTags.stream().map((link) -> link.attr("href")).forEachOrdered((this_url) -> {
            if (!this_url.isEmpty() && this_url.indexOf("mailto") == -1 && this_url.indexOf("tel:") == -1) {
                if (isAbsoluteUrl(this_url)) {
                    if (this_url.indexOf(this.hostName) != -1)
                        if(!this.globalUrlSet.contains(this_url)){
                            urlSet.add(this_url);
                            this.globalUrlSet.add(this_url);
                        }
                } else {
                    if(!this.globalUrlSet.contains(this.getAbsoluteUrl(this_url))){
                        urlSet.add(this.getAbsoluteUrl(this_url));
                        this.globalUrlSet.add(this.getAbsoluteUrl(this_url));
                    }
                }
            }
        });

        imageTags.stream().map((link) -> link.attr("src")).forEachOrdered((this_url) -> {
            if (!this_url.isEmpty()) {
                if (isAbsoluteUrl(this_url)) {
                    if (this_url.indexOf(this.hostName) != -1)
                        if(!this.globalUrlSet.contains(this_url)){
                            urlSet.add(this_url);
                            this.globalUrlSet.add(this_url);
                        }
                } else {
                    if(!this.globalUrlSet.contains(this.getAbsoluteUrl(this_url))){
                        urlSet.add(this.getAbsoluteUrl(this_url));
                        this.globalUrlSet.add(this.getAbsoluteUrl(this_url));
                    }
                }
            }
        });

        return urlSet;
    }

    private boolean isAbsoluteUrl(String url) {
        Pattern pattern = Pattern.compile("^(?:[a-z]+:)?//", Pattern.CASE_INSENSITIVE);
        return pattern.matcher(url).find();
    }

    private String getAbsoluteUrl(String url) {
        return this.hostName + url.replaceFirst("^/", "");
    }
}
