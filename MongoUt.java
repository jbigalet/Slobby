package tvshows_renamer;

import com.mongodb.*;
import java.util.*;

public class MongoUt {

    public DB db;
    public Mongo m;
    public DBCollection dbc;
    public DBCollection dbc_checked;
    public DBCollection dbc_garbage;
    public DBCollection dbc_ass;
    public DBCollection dbc_foundshows;
    
    public MongoUt() throws Exception {
        m = new Mongo("192.168.1.53", 27017);
        db = m.getDB("tv");
        dbc = db.getCollection("files");
        dbc_checked = db.getCollection("checked_files");
        dbc_garbage = db.getCollection("garbage_files");
        dbc_ass = db.getCollection("discovery_jobs");
        dbc_foundshows = db.getCollection("found_tvshows");
    }

    public void sortTVShows(){
        dbc_foundshows.drop();
        dbc_foundshows.ensureIndex("show");

        DBCursor dbcurs = dbc_checked.find();
        for(DBObject show : dbcurs){
            Object _id = show.get("_id");
            String _show = (String)show.get("showName");
            if(_show == null) System.out.println("Plouf : ");
            Integer _episode = (Integer)show.get("episode");
            Integer _season = (Integer)show.get("season");
            DBObject showObj = dbc_foundshows.findOne(new BasicDBObject("show",_show));
            if(showObj == null){
                dbc_foundshows.insert(new BasicDBObject("show", _show));
                showObj = dbc_foundshows.findOne(new BasicDBObject("show",_show));
            }
            
            List tmp = (List)showObj.get("seasons");
            DBObject seasonObj = findSeasonOrEpisode(_season, tmp);
            if(seasonObj == null)
                tmp.add(_id);

            /*DBObject seasonObj = findSeasonOrEpisode(_season, tmp);
            if(seasonObj == null){
                dbc_foundshows.findAndModify(showObj,
                    new BasicDBObject("$push",
                    new BasicDBObjectBuilder().add("seasons", new BasicDBObject("number",_season))
                                              .get()));
                showObj = dbc_foundshows.findOne(new BasicDBObject("show",_show));
                tmp = (List)showObj.get("seasons");
                seasonObj = findSeasonOrEpisode(_season, tmp);
            }

            List tmp2 = (List)seasonObj.get("episodes");
            DBObject episodeObj = findSeasonOrEpisode(_episode, tmp2);
            if(episodeObj == null){
                dbc_foundshows.findAndModify(showObj,
                    new BasicDBObject("$set",
                    new BasicDBObjectBuilder().add("episodes", new BasicDBObject("number",_season))
                                              .get()));
                //showObj = dbc_foundshows.findOne(new BasicDBObject("show",_show));
                //tmp = (List)showObj.get("seasons");
                //seasonObj = findSeasonOrEpisode(_season, tmp);
            }
            */

            dbc_foundshows.update(dbc_foundshows.findOne(new BasicDBObject("show",_show)), showObj);
        }
    }

    public DBObject findSeasonOrEpisode(int toFind, List list){
        if(list == null)
            return null;
        for(Object o : list){
            DBObject dbo = (DBObject)o;
            int n = (Integer)dbo.get("number");
            if(n == toFind)
                return dbo;
        }
        return null;
    }
    
    public String getOne(){
        DBObject tmp = dbc.findOne();
        return (String)tmp.get("name");
    }

    public List<String> getAll(){
        DBCursor dbcurs = dbc.find();
        List<String> tmp = new ArrayList<String>();
        for(DBObject currentObj : dbcurs)
            tmp.add((String)currentObj.get("name"));
        return tmp;
    }

    public void PutInGarbage(String name){
        DBObject tmp = dbc.findOne(new BasicDBObject("name", name));
        MoveFromCollection(tmp, dbc, dbc_garbage);
    }

    public void UpdateShow(String name, String showName, Double score, Integer season, Integer episode){
        DBObject tmp = dbc.findOne(new BasicDBObject("name",name));
        String originUrl = (String)tmp.get("originUrl");
        //if(dbc_checked.getCount(new BasicDBObject("originUrl",originUrl)) == 0)
            UpdateRating(originUrl,1);
        dbc.findAndModify(tmp,
                new BasicDBObject("$set",
                new BasicDBObjectBuilder().add("showName", showName)
                                          .add("score", score)
                                          .add("season", season)
                                          .add("episode", episode)
                                          .get()));
        MoveFromCollection(dbc.findOne(new BasicDBObject("name",name)), dbc, dbc_checked);
    }

    public void UpdateRating(String url, double rate){
        if( url != null ) return ;
        DBObject tmp = dbc_ass.findOne(new BasicDBObject("url",url));
        if(tmp == null) return ;
        double prio = (Double)tmp.get("priority");
        if(prio >= 0){
            prio = Math.min(prio+rate,1d);
            dbc_ass.findAndModify(tmp,
                    new BasicDBObject("$set",
                    new BasicDBObject("priority",prio)
                    ));
        }
        String parent = (String)tmp.get("parentUrl");
        UpdateRating(parent, rate/2d);
    }

    public void MoveFromCollection(DBObject toMove, DBCollection from, DBCollection to){
        try {
            from.remove(toMove);
            to.insert(toMove);
        }
        catch (Exception e){
            System.out.println("Unable to move the Object \"" + toMove.toString()
                                + "\" from \"" + from.getFullName()
                                + "\" to \"" + to.getFullName()
                                + "\" (Error: " + e.toString() + ")");
        }
    }

    public void DropCollections(){
        dbc.drop();
        dbc_ass.drop();
        dbc_checked.drop();
        dbc_garbage.drop();
    }
}
