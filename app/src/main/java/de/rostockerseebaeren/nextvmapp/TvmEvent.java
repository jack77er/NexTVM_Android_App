package de.rostockerseebaeren.nextvmapp;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Maxa on 24.05.2016.
 */
public class TvmEvent implements Parcelable {

    public int mID;
    public String mUsername;
    public Date mDate;
    public String mDateString;
    public int mDuration;
    public String mTitle;
    public int mMaxUsers;
    public int mDeadline;
    public String mLocation;
    public String mComment;
    public boolean mClosed;
    public ArrayList<EventUser> mUsers;
    public EventUser.EVENT_USER_STATE mUserState;
    public boolean mUserStateAck;
    public String mUserComment;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TvmEvent)) return false;

        TvmEvent tvmEvent = (TvmEvent) o;

        return mID == tvmEvent.mID;

    }

    @Override
    public int hashCode() {
        return mID;
    }

    public TvmEvent() {

    }

    public TvmEvent(JSONObject j) throws JSONException {
        this.mID = j.getInt("id");
        this.mUsername = j.getString("username"); // trainer
        // get Date --> parse from String
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TimeZone tz = TimeZone.getTimeZone("Europe/Berlin");
        dateFormat.setTimeZone(tz);
        try {
            mDate = dateFormat.parse(j.getString("date") + " " + j.getString("starttime"));
        } catch (ParseException e) {
            mDate = new Date();
            e.printStackTrace();
        }
        dateFormat.applyLocalizedPattern("EEEE', ' dd.MM.yyyy HH:mm");
        this.mDateString = dateFormat.format(mDate);
        //this.mDateString = j.getString("date") + " " + j.getString("starttime");
        this.mDuration = j.getInt("duration");
        this.mTitle = j.getString("title");
        this.mMaxUsers = j.getInt("max_users");
        this.mDeadline = j.getInt("deadline");
        this.mLocation = j.getString("location");
        this.mComment = j.getString("event_comment");
        this.mClosed = !j.getString("closed").equals("0");
        this.mUserStateAck= !j.getString("user_state_ack").equals("0");
        this.mUserComment = j.getString("user_comment");

        switch (j.getInt("user_state")) {
            case 0:
                mUserState = EventUser.EVENT_USER_STATE.NONE;
                break;
            case 1:
                if(this.mUserStateAck) {
                    mUserState = EventUser.EVENT_USER_STATE.YES_ACK;
                } else {
                    mUserState = EventUser.EVENT_USER_STATE.YES_NOT_ACK;
                }
                break;
            case 2:
                mUserState = EventUser.EVENT_USER_STATE.MAYBE;
                break;
            case 3:
                mUserState = EventUser.EVENT_USER_STATE.NO;
                break;
            case 4:
                mUserState = EventUser.EVENT_USER_STATE.NO_FORCED;
                break;
            default:
                mUserState = EventUser.EVENT_USER_STATE.NONE;
                break;
        }

        JSONArray users = j.getJSONArray("users");
        mUsers = new ArrayList<EventUser>();
        for(int i = 0 ; i < users.length(); i++) {
            mUsers.add(new EventUser(users.getJSONObject(i)));
        }
    }


    protected TvmEvent(Parcel in) {
        mID = in.readInt();
        mUsername = in.readString();
        long tmpMDate = in.readLong();
        mDate = tmpMDate != -1 ? new Date(tmpMDate) : null;
        mDateString = in.readString();
        mDuration = in.readInt();
        mTitle = in.readString();
        mMaxUsers = in.readInt();
        mDeadline = in.readInt();
        mLocation = in.readString();
        mComment = in.readString();
        mClosed = in.readByte() != 0x00;
        if (in.readByte() == 0x01) {
            mUsers = new ArrayList<EventUser>();
            in.readList(mUsers, EventUser.class.getClassLoader());
        } else {
            mUsers = null;
        }
        mUserState = (EventUser.EVENT_USER_STATE) in.readValue(EventUser.EVENT_USER_STATE.class.getClassLoader());
        mUserStateAck = in.readByte() != 0x00;
        mUserComment = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mID);
        dest.writeString(mUsername);
        dest.writeLong(mDate != null ? mDate.getTime() : -1L);
        dest.writeString(mDateString);
        dest.writeInt(mDuration);
        dest.writeString(mTitle);
        dest.writeInt(mMaxUsers);
        dest.writeInt(mDeadline);
        dest.writeString(mLocation);
        dest.writeString(mComment);
        dest.writeByte((byte) (mClosed ? 0x01 : 0x00));
        if (mUsers == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mUsers);
        }
        dest.writeValue(mUserState);
        dest.writeByte((byte) (mUserStateAck ? 0x01 : 0x00));
        dest.writeString(mUserComment);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TvmEvent> CREATOR = new Parcelable.Creator<TvmEvent>() {
        @Override
        public TvmEvent createFromParcel(Parcel in) {
            return new TvmEvent(in);
        }

        @Override
        public TvmEvent[] newArray(int size) {
            return new TvmEvent[size];
        }
    };
}