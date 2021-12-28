# Tretton37 Code Test

####Design and Logic
___
+ This application was developed for the purpose of downloading all the resources of https://tretton37.com/ to a given direcotry
+ When the initial url is provided to the programme at compile time, it takes the url and download the index html file of the website as index.html and process the HTML text of that file
+ After processing the html file we get a set of Absolute Urls.
+ And we use the ExecutorService to create several threads and CompletableFuture classes to recursively download above retrieved Urls, and so on and so forth.
#
####Stories picked to implement
___
+ Traversing recursively through https://tretton37.com/ and saving each resource file to local disk.
+ Showing download progress for downloading files.
+ Asynchronous programming is used to speed up the file downloading and traversing through the website.
#
####How to retrieve code , build and run
___
+ Clone the repository to your local machine from this github repo ( https://github.com/dulitharuvin/tretton37_code_test ), use the master branch 
+ navigate to the root directory of the cloned repo using the terminal/command prompt
+ run following commands depending on your OS
  + Mac OS
    + ./gradlew build
    + ./gradlew run
    
  + Windows OS
    + .\gradlew build
    + .\gradlew run
  
+ input the desired directory to save the downloaded files, leave blank if you want to use the same directory

