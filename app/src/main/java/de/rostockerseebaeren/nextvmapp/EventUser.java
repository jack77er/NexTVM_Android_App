package de.rostockerseebaeren.nextvmapp;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Maxa on 25.05.2016.
 */
public class EventUser implements Parcelable {


    public enum EVENT_USER_STATE {NONE, YES_NOT_ACK, YES_ACK, NO, NO_FORCED, MAYBE, }

    public String mName;
    public String mComment;
    public int mID;
    public EVENT_USER_STATE state;


    public EventUser(JSONObject j) throws JSONException {
        this.mName = j.getString("username");
        this.mComment = j.getString("comment");
        this.mID = j.getInt("user_id");
        switch (j.getString("state")) {
            case "0":
                this.state = EVENT_USER_STATE.NONE;
                break;
            case "1": // yes
                if(j.getInt("acknowledged") == 1) {
                    this.state = EVENT_USER_STATE.YES_ACK;
                } else {
                    this.state = EVENT_USER_STATE.YES_NOT_ACK;
                }
                break;
            case "2": // maybe
                this.state = EVENT_USER_STATE.MAYBE;
                break;
            case "3": // no
                this.state = EVENT_USER_STATE.NO;
                break;
            case "4": // forced no
                this.state = EVENT_USER_STATE.NO_FORCED;
                break;
            default:
                this.state = EVENT_USER_STATE.NONE;
                break;

        }
    }

    protected EventUser(Parcel in) {
        mName = in.readString();
        mComment = in.readString();
        mID = in.readInt();
        state = (EVENT_USER_STATE) in.readValue(EVENT_USER_STATE.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mName);
        dest.writeString(mComment);
        dest.writeInt(mID);
        dest.writeValue(state);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<EventUser> CREATOR = new Parcelable.Creator<EventUser>() {
        @Override
        public EventUser createFromParcel(Parcel in) {
            return new EventUser(in);
        }

        @Override
        public EventUser[] newArray(int size) {
            return new EventUser[size];
        }
    };
}