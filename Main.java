package tvshows_renamer;

import java.util.regex.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class Main {

    public static String[] tvshows;
    public static MongoUt mongo;

    public static void main(String[] args) throws Exception {
        //SetTVShowsList();
        tvshows = GetTVShowsList();
        //tvshows = GetTopTVShowsList(10000);

        mongo = new MongoUt();
        //mongo.DropCollections();
        while(true){
            MongoTest();
            System.out.println("No job found");
            Thread.sleep(5000);
        }

        //ExtractVoteNumberFromIMDB();
        //ExtractOnlyTVShowsFromTop(5); -- bugged

        //DirectoryTest("H:\\- Downloads -\\- Vus -\\test");
        
        //Directory_Check_Launcher.Launch();
    }

    public static void MongoTest() throws Exception{
        List<String> listToTest = mongo.getAll();
        int n = listToTest.size();
        //System.out.println(n);
        int perc = n/100;
        //for(int i=0 ; i<100 ; i++) System.out.print("-");
        //System.out.println();

        int count = 0;
        for(String toTest : listToTest){
            //if(count++%perc == 0) System.out.print("|");
            //System.out.println(toTest);
            if(Levenshtein.getInfos(toTest).length > 1){
                Object[] tmp = FileTest(toTest);
                if((Double)tmp[2]<1){
                    mongo.UpdateShow(toTest, (String)tmp[0], (Double)tmp[2], (Integer)tmp[3], (Integer)tmp[4]);
                    System.out.println(toTest + " -> " + tmp[0] + 
                            " (" + tmp[1] + ")" + " @ " + tmp[2] +
                            " [" + tmp[3] + "x" + tmp[4] + "]");
                    //break;
                }
                else mongo.PutInGarbage(toTest);
            } else mongo.PutInGarbage(toTest);
        }
        //System.out.println();
    }

    public static void ExtractOnlyTVShowsFromTop(int top) throws Exception {
        String[] tmp = GetPage("file:///C:/TEST/listWithVotes.txt").split("\\n");
        TreeMap<String,Double> votes = new TreeMap<String,Double>();
        int i=0, j=tmp.length-1;
        while( i < top ){
            String show = tmp[j].split(" -> ")[1];
            System.out.println(i + " / " + j + " -> " + show);
            String API = GetPage("http://www.imdbapi.com/?i=&r=XML&t=" + URLEncoder.encode(show, "UTF-8"));
            List<String> idMatch = Utils.RegexSearch(API, "id=\\\"([^\\\"]+)", 1);
            if(!idMatch.isEmpty()){
                String ID = idMatch.get(0);
                String realIMDB = GetPage("http://www.imdb.com/title/" + ID + "/");
                List<String> tvshowMatch = Utils.RegexSearch(realIMDB, "(content='tv_show')", 1);
                if(!tvshowMatch.isEmpty()){
                    i++;
                    List<String> votesMatch = Utils.RegexSearch(realIMDB, "'([0-9]+) IMDb users have given an average vote", 1);
                    if(votesMatch.isEmpty())
                        votes.put(show, -3+Math.random());
                    else
                        votes.put(show, Double.parseDouble(votesMatch.get(0))+Math.random());
                }
            }
            j--;
        }
        SortedSet<Map.Entry<String,Double>> sortedVotes = entriesSortedByValues(votes);
        List<String> toAddOnFile = new ArrayList<String>();
        for(Map.Entry<String,Double> tmp2 : sortedVotes)
            toAddOnFile.add(tmp2.getValue() + " -> " + tmp2.getKey());
        write_file(toAddOnFile, "C:\\TEST\\listWithVotesProper.txt");
    }

    static <K, V extends Comparable<? super V>> SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<Map.Entry<K, V>>(
                new Comparator<Map.Entry<K, V>>() {
                    @Override
                    public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                        return e1.getValue().compareTo(e2.getValue());
                    }
                });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    public static void ExtractVoteNumberFromIMDB() throws Exception {
        TreeMap<String,Double> votes = new TreeMap<String,Double>();
        int i=0;
        for(String show : tvshows){
            System.out.println(i++ + " : " + show); //if(i>100) break;
            String imdbPage = GetPage("http://www.imdbapi.com/?i=&r=XML&t=" + URLEncoder.encode(show, "UTF-8"));
            List<String> titleMatch = Utils.RegexSearch(imdbPage, "title=\\\"([^\\\"]+)", 1);
            if(titleMatch.isEmpty())
                votes.put(show, -3D + Math.random());
            else{
                String title = titleMatch.get(0);
                if(Levenshtein.Distance(show, title) > 1)
                    votes.put(show, -2D + Math.random());
                else{
                    List<String> votesMatch = Utils.RegexSearch(imdbPage, "votes=\\\"([0-9]+)", 1);
                    if(votesMatch.isEmpty())
                        votes.put(show, -1D + Math.random());
                    else
                        votes.put(show, Double.parseDouble(votesMatch.get(0)) + Math.random()); // random to avoid same values
                }
            }
        }
        SortedSet<Map.Entry<String,Double>> sortedVotes = entriesSortedByValues(votes);
        List<String> toAddOnFile = new ArrayList<String>();
        for(Map.Entry<String,Double> tmp : sortedVotes)
            toAddOnFile.add(tmp.getValue() + " -> " + tmp.getKey());
        write_file(toAddOnFile, "C:\\TEST\\listWithVotes.txt");
    }

    public static Object[] FileTest(String tfile) throws Exception {
        String file = Levenshtein.CorrectFileName(tfile.substring(0,tfile.lastIndexOf(".")),true);
        double minScore = Integer.MAX_VALUE;
        String bestShow = "";
        for(String show : tvshows){
            double score = Levenshtein.Distance(file, show);
            if(score < minScore){
                minScore = score;
                bestShow = show;
            }
        }
        int[] infos = Levenshtein.getInfos(tfile);
        return new Object[] {bestShow, file, minScore, infos[0], infos[1]};
    }

    public static void DirectoryTest(String location) throws Exception {
        File dir = new File(location);
        String[] filelist = dir.list();
        for(String file : filelist){
            Object[] tmp = FileTest(file);
            System.out.println(file + " -> " + tmp[0] + " (" + tmp[1] + ")" + " @ " + tmp[2]);
        }
    }

    public static String[] GetTopTVShowsList(int n) throws Exception {
        String[] tmp = GetPage("file:///C:/TEST/listWithVotes.txt").split("\\n");
        int N = Math.min(n, tmp.length);
        String[] f = new String[N];
        for(int i=0 ; i<N ; i++)
            f[i] = tmp[tmp.length-1-i].split(" -> ")[1].replaceAll(" [1-2][0-9][0-9][0-9]$", "");
        System.out.println("Top TV Show list retrieved : " + f.length + " founded.");
        return f;
    }

    public static String[] GetTVShowsList() throws Exception {
        String[] tmp = GetPage("file:///C:/TEST/list.txt").split("\\n");
        System.out.println("TV Show list retrieved : " + tmp.length + " founded.");
        return tmp;
    }

    public static void SetTVShowsList() throws Exception {
        String TVRage = GetPage("http://www.tvrage.com/all.php", new String[][] {{"fletter","all"}});
        List<String> list = Utils.RegexSearch(TVRage, "<td><a href=[^>]+>(<[^>]+>)*([^<]+)<", 2);
        list = correctThe(list);
        write_file(list, "C:\\TEST\\list.txt");
        System.out.println("TV Show list created : " + list.size() + " founded.");
    }

    public static List<String> correctThe(List<String> list){
        List<String> newList = new ArrayList<String>();
        for(String tmp : list){
            String newshow = tmp;
            if(tmp.endsWith(", The")) newshow = "The " + tmp.substring(0, tmp.length()-5);
            if(tmp.endsWith(", A")) newshow = "A " + tmp.substring(0, tmp.length()-3);
            newList.add(Levenshtein.CorrectFileName(newshow,false));
            //newList.add(newshow);
        }
        return newList;
    }


    public static String GetPage(String main_url) throws Exception {
        return GetPage(main_url, new String[][] {});
    }

    public static String GetPage(String main_url, String[][] _post) throws Exception {
        StringBuilder webpage = new StringBuilder();
        
        URLConnection URL_Connection = (new URL(main_url)).openConnection();      //Creation d'une connection
        URL_Connection.setDoOutput(true);

        if(_post.length != 0){
            OutputStreamWriter WR = new OutputStreamWriter(URL_Connection.getOutputStream());
            WR.write(rewrite_post(_post));
            WR.flush();
        }

        InputStream Stream = URL_Connection.getInputStream();          //Creation du stream
        BufferedReader BR = new BufferedReader(new InputStreamReader(Stream));  //Association buffer/stream pour lecture

        String line;
        while((line=BR.readLine())!=null)
            webpage.append(line).append("\n");

        BR.close();
        return webpage.toString();
    }

    public static String rewrite_post(String[][] _post) throws Exception{
        if(_post.length == 0) return "";
        
        String tmp = URLEncoder.encode(_post[0][0], "UTF-8") + "=" + URLEncoder.encode(_post[0][1], "UTF-8");
        for(int i=1 ; i<_post.length ; i++)
            tmp += URLEncoder.encode(_post[i][0], "UTF-8") + "=" + URLEncoder.encode(_post[i][1], "UTF-8");

        return tmp;
    }

    public static void write_file(List<String> toWrite, String file_location) throws Exception {
        FileWriter fstream = new FileWriter(file_location);
        BufferedWriter out = new BufferedWriter(fstream);

        for(String line : toWrite)
            out.write(line + "\n");

        out.close();
    }
}
