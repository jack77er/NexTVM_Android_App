package de.rostockerseebaeren.nextvmapp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Maxa on 08.12.2016.
 */
public class TvmCategory {
    public int mID;
    public String mName;
    public boolean mPublished;
    public int mViewID;

    public TvmCategory(int id, String name, boolean published) {
        this.mID = id;
        this.mName = name;
        this.mPublished = published;
    }

    public TvmCategory() {

    }

    public TvmCategory(JSONObject jsonObject) {
        try {
            this.mID = jsonObject.getInt("id");
            this.mName = jsonObject.getString("name");
            this.mPublished = jsonObject.getString("published").equals("1"); // true if "1", false otherwise
        } catch (JSONException e) {e.printStackTrace();}
    }

    public void setCategory(String strJson) {
        try {
            JSONObject jsonRootObject = new JSONObject(strJson);

            this.mID = jsonRootObject.getInt("id");
            this.mName = jsonRootObject.getString("name");
            this.mPublished = jsonRootObject.getBoolean("published");
        } catch (JSONException e) {e.printStackTrace();}
    }

}
