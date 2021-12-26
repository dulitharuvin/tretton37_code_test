package com.dulitha;

public class DownloadSite {

    public static void main(String args[]) {
        String url = "https://tretton37.com/";
        String saveDir = "/Users/erandikiriweldeniya/Documents/Dulitha/Professional/tretton37/tretton37_website/";

        Downloader downloader = new Downloader(url, saveDir);
    }
}
