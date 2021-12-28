package com.dulitha;

import com.dulitha.util.FileUtil;

import java.io.File;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DownloadSite {

    private static final int MAX_THREADS_FOR_DOWNLOADS = 5;
    private static final String DEFAULT_DOWNLOAD_DIR = "tretton37_website";

    public static void main(String args[]) {
        String url = "https://tretton37.com/";
        String saveDir = getDownloadDirectory();
        FileUtil fileUtil = new FileUtil(saveDir);

        Downloader downloader = new Downloader(url, saveDir, fileUtil);
        var paths = downloader.startDownload(url);

        downloadRunner(downloader, paths);
    }

    private static void downloadRunner(Downloader downloader, Set<String> paths) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS_FOR_DOWNLOADS);
        try {

            var downloadTasks = paths
                    .stream()
                    .map(urlPath -> CompletableFuture.supplyAsync(() -> downloader.startDownload(urlPath), executor))
                    .collect(Collectors.toList());
            executor.shutdown();
            downloadTasks.forEach((cmp) -> {
                cmp.thenAccept(set -> downloadRunner(downloader, set));
            });
            CompletableFuture.allOf(downloadTasks.toArray(new CompletableFuture[downloadTasks.size()])).join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow();
        }
    }

    private static String getDownloadDirectory(){
        String currentDirectory = System.getProperty("user.dir");
        Scanner userInput = new Scanner(System.in);
        System.out.print("Enter directory to download tretton37.com : ");
        String directoryToDownload = userInput.nextLine();

        if(directoryToDownload.trim().isEmpty()){
            return currentDirectory.substring(0, currentDirectory.lastIndexOf("/")) + File.separator + DEFAULT_DOWNLOAD_DIR;
        }
        return directoryToDownload.trim() + File.separator + DEFAULT_DOWNLOAD_DIR;
    }
}
