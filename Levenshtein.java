package tvshows_renamer;

import java.util.*;

public class Levenshtein {

   public static String[] currentWords = new String[] {"1920","1440","1337x","1080","1280","720p",
        "DIMENSION","CTU","LOL","IMMERSE","HDCP","DVDRIP","DVDSCR","WS","UP BY",
        "x720","x640","x480","480p","360p","x264","H264","187HD","FQM","LMAO",
        "XOXO","eztv","PDV","PDTV","TSXVID","XviD","DSR","DivXNL","Divx","2HD",
        "2WIRE","NL,Subs","KLAXXON","aXXo","NoTV","BRRip","BDRip","Bluray",
        "HDTV","R5","BYU","DVB","Omifast","@KIDZ","KIDZCORNER","AC3","AC-3",
        "FXG","NTSC",",WS","WS.",".WS","NL,","NLT","CN,","TC,","ISO.","Swesub",
        "READNFO","ViCiOsO","WorkPrint","OneDDL.com","fwint.com",
        ",Demonoid,com,","ExtraTorrent,com","ExtraTorrent","VOST","VOSTFR",
        "FxM","DDC","keltz","REAL,PROPER","PROPER","CVCD","VCD","LIMITED","www.",
        "www,","PDVD","PDVD-RIP","PPVRIP","www","1CDRip","2CDRip","Pre,DVD",
        "Pre-DVD","DVD","UNCUT,",",TPB","PSP","iPod","Zune","mp4","mpg","3gp",
        "wmv","CAM","mkv","m4","xRipp","YesTV","CRIMSON","EXTENDED",
        "BR-Scr","BR-Screener","SCREENER","SCR,","SCR.","UNRATED","REPACK","HQ",
        "RETAIL","NEW,SOURCE","DiTa","SHAMNBOYZ","ExtraScene",
        "MAXSPEED","ShareReactor","ShareZONE","ShareGo","aAF","xRG","STV","-MAX",
        "RESYNC","SYNC-","SYNCFIX","iTA,","_BBC","_ITV","_Channel,4",
        "_Film4","cw4f","w4f","www.CooLXviD.net","VOSTFR","VOST","FRENCH","TRUEFRENCH"};
    //String separator = "[^a-z0-9]";
    public static String separator = "[ \\\"\\+\\-\\*\\.:/;,!°\\(\\)\\[\\]\\{\\}'\\$\\#\\?\\%\\^\\¨£¤@\\|`~&_<>]";
    public static String[] SeasonAndEpisode = new String[] {
            "s?([0-9][0-9]?)e([0-9][0-9])",
            "([0-9][0-9]?)x([0-9][0-9])",
            "([0-9])([0-9][0-9])"
        };

    public static int[] getInfos(String _name){
        String name = _name.toLowerCase();
        for(String reg : SeasonAndEpisode){
            List<String>[] match = Utils.RegexSearch(name, separator+reg+separator, new int[] {1,2});
            if(!match[0].isEmpty() && !match[1].isEmpty())
                return new int[] {Utils.getInt(match[0].get(0)), Utils.getInt(match[1].get(0))};
        }
        return new int[] {-1};        
    }

    public static String CorrectFileName(String name, boolean withCurrentWords){
        String newName = " " + name.toLowerCase() + " ";
        if(withCurrentWords)
            for(String word : currentWords)
                newName = newName.replaceAll(separator + word.toLowerCase() + separator, " ");

        newName = newName.replaceAll(separator+"s?[0-9][0-9]?"+separator+"?e[0-9][0-9]"+separator, " ");
        newName = newName.replaceAll(separator+"[0-9][0-9]?[x\\*e\\.\\:][0-9][0-9]"+separator, " ");
        newName = newName.replaceAll("\\[[^\\]]+", " ");
        newName = newName.replaceAll("\\([^\\)]+", " ");
        newName = newName.replaceAll("\\{[^\\}]+", " ");
        
        //newName = newName.replaceAll(separator+"[0-9][0-9][0-9]"+separator, " ");
        newName = newName.replaceAll(separator, " ");
        newName = newName.replaceAll(" +", " ");

        return newName.trim();
    }

    public static double Distance2(String file, String show){
        String tmpFile = file;
        String fletter = show.substring(0,1);
        int pos;
        double minscore = 1000;
        while((pos = tmpFile.indexOf(fletter))!=-1){
            tmpFile = tmpFile.substring(pos);
            double score = Distance2(tmpFile, show) + ((double)pos)/5d;
            if(score<minscore)minscore = score;
        }
        System.out.println(minscore);
        return minscore;
    }

    public static double Distance(String file, String show) {
        int flen = file.length(), slen = show.length();
        int newlen = Math.min(flen, slen);
        String s = file.substring(0, newlen).toLowerCase(), t = show.toLowerCase();

        if (s == null || t == null) {
            throw new IllegalArgumentException("Strings must not be null");
        }

        int n = s.length(); // length of s
        int m = t.length(); // length of t

        if (n == 0) {
            return m;
        } else if (m == 0) {
            return n;
        }

        int p[] = new int[n + 1]; //'previous' cost array, horizontally
        int d[] = new int[n + 1]; // cost array, horizontally
        int _d[]; //placeholder to assist in swapping p and d

        // indexes into strings s and t
        int i; // iterates through s
        int j; // iterates through t

        char t_j; // jth character of t

        int cost; // cost

        for (i = 0; i <= n; i++) {
            p[i] = i;
        }

        for (j = 1; j <= m; j++) {
            t_j = t.charAt(j - 1);
            d[0] = j;

            for (i = 1; i <= n; i++) {
                cost = s.charAt(i - 1) == t_j ? 0 : 1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
            }

            // copy current distance counts to 'previous row' distance counts
            _d = p;
            p = d;
            d = _d;
        }

        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        double ld = p[n];
        ld = (1+ld)/Math.pow(newlen,1d/3d);
        return ld;
    }

}
