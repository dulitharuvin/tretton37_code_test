package com.dulitha;

import com.dulitha.util.FileUtil;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
    private FileUtil fileUtil;

    private static final int BUFFER_SIZE = 4096;
    private static final String HTML_TEXT_CONTENT_TYPE = "text/html";


    public Downloader(String hostName, String saveDir, FileUtil fileUtil) {
        this.hostName = hostName;
        this.saveDir = saveDir;
        this.fileUtil = fileUtil;
    }

    public Set<String> startDownload(String urlString) {
        try {
            URL url = new URL(urlString);
            return downloadAndGetUrlList(url);
        } catch (IOException ie) {
            System.out.println("IO Exception occur");
        }
        return new HashSet<>();
    }

    private Set<String> downloadAndGetUrlList(URL url) {
        Set<String> pageUrls = new HashSet<>();
        pageUrls = downloadResource(url);
        return pageUrls;
    }

    private Set<String> downloadResource(URL url) {
        Set<String> set = new HashSet<>();

        var extension = FilenameUtils.getExtension(url.getPath());

        var directoryPath = getDirectoryPath(url, extension);
        var fileName = getFileName(url, extension);

        createDirectory(directoryPath);

        File file = getNewFileToSave(directoryPath, fileName);

        try {
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {

                if (httpConn.getContentType().contains(HTML_TEXT_CONTENT_TYPE)) {
                    StringBuilder sb = downloadHtmlFile(url, file);
                    set = parseHtmlFileForTags(sb);
                } else {
                    downloadNonHtmlFile(file, httpConn);
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

    private void downloadNonHtmlFile(File file, HttpURLConnection httpConn) {
        try {
            InputStream inputStream = httpConn.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(file.getAbsoluteFile());

            long completeFileSize = httpConn.getContentLength();

            // read each line from stream till end
            long downloadedFileSize = 0;
            char[] animationChars = new char[]{'|', '/', '-', '\\'};

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                downloadedFileSize += bytesRead;
                outputStream.write(buffer, 0, bytesRead);
                // calculate progress
                final int currentProgress = (int) ((((double) downloadedFileSize) / ((double) completeFileSize)) * 100d);
                System.out.print("Downloading: " + currentProgress + "% " + animationChars[currentProgress % 4] + "\r");
            }
            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*private StringBuilder downloadHtmlFile(URL url, File file) throws IOException {
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
        return sb;
    }*/

    private StringBuilder downloadHtmlFile(URL url, File file) throws IOException {
        Document document = Jsoup.connect(url.toString()).get();
        StringBuilder sb = new StringBuilder(document.toString());
        String modifiedHtml = this.modifyHtmlLinks(document);
        BufferedWriter writer =
                new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
        writer.write(modifiedHtml);
        writer.close();
        return sb;
    }

    private File getNewFileToSave(String directoryPath, String fileName) {
        return new File(this.saveDir + File.separator + directoryPath + File.separator + fileName);
    }

    private void createDirectory(String directoryPath) {
        File directory = new File(this.saveDir + File.separator + directoryPath + File.separator);

        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private String getFileName(URL url, String extension) {
        return !extension.isEmpty() ? FilenameUtils.getName(url.getPath()) : "index.html";
    }

    private String getDirectoryPath(URL url, String extension) {
        return !extension.isEmpty() ? url.getPath().substring(0, url.getPath().lastIndexOf("/")) : url.getPath();
    }

    private Set<String> parseHtmlFileForTags(StringBuilder sb) {
        Set<String> urlSet = new HashSet<>();

        Document document = Jsoup.parse(sb.toString());
        Elements imageTags = document.select("img");
        Elements anchorTags = document.select("a");

        anchorTags.stream().map((link) -> link.attr("href")).forEachOrdered((this_url) -> {
            if (!this_url.isEmpty() && this_url.indexOf("mailto") == -1 && this_url.indexOf("tel:") == -1) {
                if (isAbsoluteUrl(this_url)) {
                    if (this_url.indexOf(this.hostName) != -1)
                        if (!this.globalUrlSet.contains(this_url)) {
                            urlSet.add(this_url);
                            this.globalUrlSet.add(this_url);
                        }
                } else {
                    if (!this.globalUrlSet.contains(this.getAbsoluteUrl(this_url))) {
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
                        if (!this.globalUrlSet.contains(this_url)) {
                            urlSet.add(this_url);
                            this.globalUrlSet.add(this_url);
                        }
                } else {
                    if (!this.globalUrlSet.contains(this.getAbsoluteUrl(this_url))) {
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

    private String modifyHtmlLinks(Document document) {
        Elements links = document.select("a[href]");

        for (Element link : links) {
            var this_url = link.attr("href");
            if (!this_url.isEmpty() && this_url.indexOf("mailto") == -1 && this_url.indexOf("tel:") == -1) {
                if (isAbsoluteUrl(this_url)) {
                    if (this_url.indexOf(this.hostName) != -1) {
                        var modifiedUrl = this.fileUtil.getCompleteFileSavePath(this_url);
                        link.attr("href", modifiedUrl);
                    }
                } else {
                    var modifiedUrl = this.fileUtil.getCompleteFileSavePath(this_url);
                    link.attr("href", modifiedUrl);
                }
            }
        }
        return document.toString();
    }
}
