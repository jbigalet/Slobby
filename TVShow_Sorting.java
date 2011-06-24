package tvshows_renamer;

import java.util.*;
import com.mongodb.*;

public class TVShow_Sorting {

    public static MongoUt mongo;
    
    public static void main(String[] args) throws Exception {
        mongo = new MongoUt();
        mongo.sortTVShows();
    }
    
}
