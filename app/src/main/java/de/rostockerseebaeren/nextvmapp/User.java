package de.rostockerseebaeren.nextvmapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Maxa on 24.05.2016.
 */
public class User implements Serializable {

    public static int mSeebaerenGoupID = 12;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        if (mID != user.mID) return false;
        if (mSeebaer != user.mSeebaer) return false;
        if (mName != null ? !mName.equals(user.mName) : user.mName != null) return false;
        if (mUsername != null ? !mUsername.equals(user.mUsername) : user.mUsername != null)
            return false;
        if (mEmail != null ? !mEmail.equals(user.mEmail) : user.mEmail != null) return false;
        return mJoomlaPassword != null ? mJoomlaPassword.equals(user.mJoomlaPassword) : user.mJoomlaPassword == null;

    }

    @Override
    public int hashCode() {
        int result = mID;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        result = 31 * result + (mUsername != null ? mUsername.hashCode() : 0);
        result = 31 * result + (mEmail != null ? mEmail.hashCode() : 0);
        result = 31 * result + (mJoomlaPassword != null ? mJoomlaPassword.hashCode() : 0);
        result = 31 * result + (mSeebaer ? 1 : 0);
        return result;
    }

    public int mID;
    public String mName;
    public String mUsername;
    public String mEmail;
    public String mJoomlaPassword;
    public boolean mSeebaer;
    public long mLastLogin;

    public User() {
    }

    public void setUser(String strJson) {
        try {
            JSONObject groups;

            JSONObject jsonRootObject = new JSONObject(strJson);

            this.mID = jsonRootObject.getInt("id");
            this.mEmail = jsonRootObject.getString("email");
            this.mName = jsonRootObject.getString("name");
            this.mUsername = jsonRootObject.getString("username");
            this.mJoomlaPassword = jsonRootObject.getString("password");
            this.mLastLogin = jsonRootObject.getLong("lastvisitDate");
            //groups = jsonRootObject.getJSONObject("groups");
            //Get the instance of JSONArray that contains JSONObjects
            groups = jsonRootObject.getJSONObject("groups");

            if(groups.optInt(String.valueOf(mSeebaerenGoupID)) == mSeebaerenGoupID) {
                mSeebaer = true;
            }

        } catch (JSONException e) {e.printStackTrace();}
    }
}
