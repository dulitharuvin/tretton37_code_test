package com.dulitha.util;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class FileUtil {

    private String urlString;
    private String saveDir;

    public FileUtil(String saveDir) {
        this.saveDir = saveDir;
    }

    public String getCompleteFileSavePath(String urlString) {
        this.urlString = urlString;
        return this.saveDir + File.separator + this.getDirectoryPath() + File.separator + this.getFileName();
    }

    private String getDirectoryPath() {
        return !this.getFileExtension().isEmpty() ? this.urlString.substring(0, this.urlString.lastIndexOf("/")) : this.urlString;
    }

    private String getFileName() {
        return !this.getFileExtension().isEmpty() ? FilenameUtils.getName(this.urlString) : "index.html";
    }

    private String getFileExtension() {
        return FilenameUtils.getExtension(this.urlString);
    }
}
