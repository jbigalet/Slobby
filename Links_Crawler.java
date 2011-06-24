package tvshows_renamer;

import java.util.*;
import java.io.*;
import java.lang.*;
import java.net.*;
import java.util.regex.*;

public class Links_Crawler {

    public static class Worker extends Thread {
        int title;
        int max = Integer.MIN_VALUE;

        public Worker(int title) {
            this.title = title;
        }

        @Override
        public void run() {
            int breakCount = 0;
            while( breakCount < 50 ){
                String job = getJob();
                if(job == null){
                    breakCount++;
                    //System.out.println("No job for thread " + title);
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {}
                }
                else {
                    breakCount = 0;
                    //System.out.println("Job found for thread " + title + " -> " + job);
                    doJob(job);
                }
            }
        }

        public void doJob(String job){
            try {
                String page = getPage(job);
                URL jobURL = new URL(job);
                List<String> flat_links = RegexSearch(page, "href=['\\\"]([^'\\\"]+)", 1);
                List<String> real_links = new ArrayList<String>();
                List<String> jobToAdd = new ArrayList<String>();
                for(String link : flat_links)
                    if(isValidExt(link)){
                        URL url = new URL(jobURL, link);
                        String normalised_url = url.toExternalForm();
                        real_links.add(normalised_url);
                        int sharpPos = normalised_url.indexOf("#");
                        if( sharpPos != -1 )
                            normalised_url = normalised_url.substring(0, sharpPos);
                        if(isInternal(url))
                            if(isToReallyDo(normalised_url))
                                jobToAdd.add(normalised_url);
                    }
                toDo.addAll(jobToAdd);
                //graph.put(job, real_links);
                writeInFile(job, real_links);
            } catch (Exception e) {
                System.out.println("Error in thread " + title + ". Unable to do the job. (" + e.toString() + ")");
            }
        }

        public void writeInFile(String job, List<String> links){
            int id = getID();
            List<String> toWrite = new ArrayList<String>();
            toWrite.add(job);
            toWrite.addAll(links);
            write_file(toWrite, "C:\\TEST\\MR_Map\\" + id + ".txt");
        }

        public synchronized int getID(){
            return id++;
        }

        public boolean isInternal(URL url){
            return (url.getHost().equals(host));
        }

        public synchronized boolean isToReallyDo(String toDo){
            if(notToDo.contains(toDo))
                return false;
            else {
                notToDo.add(toDo);
                return true;
            }
        }

        public boolean isValidExt(String link){
            for(String ext : unabled_ext)
                if(link.endsWith("." + ext))
                    return false;
            if(link.startsWith("javascript"))
                return false;
            return true;
        }

        public synchronized String getJob(){
            if(toDo.isEmpty())
                return null;
            else {
                String job = toDo.get(0);
                toDo.remove(0);
                return job;
            }
        }
    }

    public static String host;
    public static List<String> toDo = new ArrayList<String>();
    public static Set<String> notToDo = new HashSet<String>();
    //public static TreeMap<String,List<String>> graph = new TreeMap<String, List<String>>();
    public static int id = 0;
    public static String[] unabled_ext = new String[] {"jpg","jpeg","gif","bmp","png","css","js","ico","xml"};
    public static MongoUt mongo;

    public static void main(String[] args) throws Exception {
        int nThreads = 100;
        String main = "http://megarelease.net";
        //String main = "http://127.0.0.1";
        mongo = new MongoUt();
        URL mainPage = new URL(main);
        host = mainPage.getHost();
        toDo.add(main);
        notToDo.add(main);
        notToDo.add(main + "/");
        Worker[] threads = new Worker[nThreads];
        for (int i = 0; i < nThreads; i++) {
            threads[i] = new Worker(i);
            threads[i].start();
        }
        try {
            for (int i = 0; i < nThreads; i++)
                threads[i].join();
        } catch (InterruptedException e) {}
        /*
        List<String> toWrite = new ArrayList<String>();
        for(String link : graph.navigableKeySet()){
            StringBuilder tmp = new StringBuilder(link);
            tmp.append(" -> ");
            List<String> links = graph.get(link);
            for(String related : links)
                tmp.append(related).append(" ; ");
            toWrite.add(tmp.toString());
        }*/
    }

    public static void write_file(List<String> toWrite, String file_location) {
        try {
            FileWriter fstream = new FileWriter(file_location);
            BufferedWriter out = new BufferedWriter(fstream);

            for(String line : toWrite)
                out.write(line + "\n");

            out.close();
        } catch (Exception e) {
            System.out.println("Error in file writting (" + e.toString() + ")");
        }
    }

    public static String getPage(String url_string){
        StringBuilder SourcePage = new StringBuilder();
        try{
            URL PageURL = new URL(url_string);
            URLConnection URL_Connection = PageURL.openConnection();
            InputStream Stream = URL_Connection.getInputStream();
            BufferedReader BR = new BufferedReader(new InputStreamReader(Stream));
            String line;
            while((line=BR.readLine())!=null)
                    SourcePage = SourcePage.append("\n").append(line);
            BR.close();
        }
        catch(FileNotFoundException e){
            System.out.println("Page not found : " + e.toString());
        }
        catch(IOException e){
            System.out.println("HTTP Error : " + e.toString());
            try{
                Thread.sleep(500);
            } catch (Exception ee) {}
            return getPage(url_string);
        }
        catch(Exception e){
            System.out.println("Error to get the page : " + url_string + " (" + e.toString() + ")");
        }

        return SourcePage.toString();
    }

    public static List<String> RegexSearch(String SourceCode, String Regex_String, int match) {
        List<String> ResultsList = new ArrayList<String>();

        Pattern Regex = Pattern.compile(Regex_String);
        Matcher Results = Regex.matcher(SourceCode);

        while (Results.find())
            ResultsList.add( Results.group(match) );

        return ResultsList;
    }
}
