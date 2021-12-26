package com.dulitha;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DownloadSite {

    public static void main(String args[]) {
        String url = "https://tretton37.com/";
        String saveDir = "/Users/erandikiriweldeniya/Documents/Dulitha/Professional/tretton37/tretton37_website/";


        Downloader downloader = new Downloader(url, saveDir);
        var paths = downloader.startDownload(url);

        downloadRunner(downloader, paths);
    }

    private static void downloadRunner( Downloader downloader, Set<String> paths) {
        ExecutorService executor = Executors.newFixedThreadPool(5);
        try {

            var downloadTasks = paths
                    .stream()
                    .map(urlPath -> CompletableFuture.supplyAsync(() -> downloader.startDownload(urlPath), executor))
                    .collect(Collectors.toList());

            // at this point, all requests are enqueued, and threads will be assigned as they become available
            executor.shutdown();    // stops accepting requests, does not interrupt threads,
            // items in queue will still get threads when available
            downloadTasks.forEach((cmp) ->{
                cmp.thenAccept(set -> downloadRunner(downloader,set));
            });
            // wait for all downloads to complete
            CompletableFuture.allOf(downloadTasks.toArray(new CompletableFuture[downloadTasks.size()])).join();
            // at this point, all downloads are finished,
            // so it's safe to shut down executor completely
        } catch ( Exception e) {
            e.printStackTrace();
        } finally {
            executor.shutdownNow(); // call this when done with the executor.
        }
    }
}
