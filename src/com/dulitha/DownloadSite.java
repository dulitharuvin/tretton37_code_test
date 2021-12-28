package com.dulitha;

import com.dulitha.util.FileUtil;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DownloadSite {

    private static final int MAX_THREADS_FOR_DOWNLOADS = 5;

    public static void main(String args[]) {
        String url = "https://tretton37.com/";
        String saveDir = "/Users/erandikiriweldeniya/Documents/Dulitha/Professional/tretton37/tretton37_website";
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
}
