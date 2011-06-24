package tvshows_renamer;

import java.util.*;
import java.util.regex.*;

public class Utils {

    public static List<String> RegexSearch(String SourceCode, String Regex_String, int match) {
        List<String> ResultsList = new ArrayList<String>();

        Pattern Regex = Pattern.compile(Regex_String);
        Matcher Results = Regex.matcher(SourceCode);

        while (Results.find())
            ResultsList.add( Results.group(match) );

        return ResultsList;
    }

    public static List<String>[] RegexSearch(String SourceCode, String Regex_String, int[] matchs) {
        List<String>[] ResultsList = (List<String>[]) new List[matchs.length];
        for(int i=0 ; i<ResultsList.length ; i++)
            ResultsList[i] = new ArrayList<String>();

        Pattern Regex = Pattern.compile(Regex_String);
        Matcher Results = Regex.matcher(SourceCode);

        while (Results.find()){
            for(int i=0 ; i<matchs.length ; i++)
                ResultsList[i].add( Results.group(matchs[i]) );
        }

        return ResultsList;
    }

    public static int getInt(String s){
        try{
            return Integer.parseInt(s);
        } catch (NumberFormatException e){
            System.out.println("Error: Impossible to convert to int the string : " + s);
        }
        return -1;
    }

}
