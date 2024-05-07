package id.dkon.app.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import id.dkon.app.constants.Constants;

public class Chat extends Application implements Constants, Parcelable {

    private long withUserId, fromUserId, toUserId;
    private int id, withUserState, newMessagesCount, createAt, withUserVerify;
    private String withUserUsername, withUserFullname, withUserPhotoUrl, timeAgo, date, lastMessage, lastMessageAgo;
    private Boolean blocked = false;

    public Chat() {

    }

    public Chat(JSONObject jsonData) {

        try {

            this.setId(jsonData.getInt("id"));
            this.setFromUserId(jsonData.getLong("fromUserId"));
            this.setToUserId(jsonData.getLong("toUserId"));
            this.setWithUserId(jsonData.getLong("withUserId"));
            this.setWithUserVerify(jsonData.getInt("withUserVerify"));
            this.setWithUserState(jsonData.getInt("withUserState"));
            this.setWithUserUsername(jsonData.getString("withUserUsername"));
            this.setWithUserFullname(jsonData.getString("withUserFullname"));
            this.setWithUserPhotoUrl(jsonData.getString("withUserPhotoUrl"));
            this.setLastMessage(jsonData.getString("lastMessage"));
            this.setLastMessageAgo(jsonData.getString("lastMessageAgo"));
            this.setNewMessagesCount(jsonData.getInt("newMessagesCount"));
            this.setDate(jsonData.getString("date"));
            this.setCreateAt(jsonData.getInt("createAt"));
            this.setTimeAgo(jsonData.getString("timeAgo"));

            if (jsonData.has("withUserBlocked")) {

                this.setBlocked(jsonData.getBoolean("withUserBlocked"));
            }

        } catch (Throwable t) {

            Log.e("Chat", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Chat", jsonData.toString());
        }
    }

    public void setId(int id) {

        this.id = id;
    }

    public int getId() {

        return this.id;
    }

    public void setFromUserId(long fromUserId) {

        this.fromUserId = fromUserId;
    }

    public long getFromUserId() {

        return this.fromUserId;
    }

    public void setToUserId(long toUserId) {

        this.toUserId = toUserId;
    }

    public long getToUserId() {

        return this.toUserId;
    }

    public void setWithUserId(long withUserId) {

        this.withUserId = withUserId;
    }

    public long getWithUserId() {

        return this.withUserId;
    }

    public void setWithUserState(int withUserState) {

        this.withUserState = withUserState;
    }

    public long getWithUserVerify() {

        return this.withUserVerify;
    }

    public void setWithUserVerify(int withUserVerify) {

        this.withUserVerify = withUserVerify;
    }

    public int getWithUserState() {

        return this.withUserState;
    }

    public void setWithUserUsername(String withUserUsername) {

        this.withUserUsername = withUserUsername;
    }

    public String getWithUserUsername() {

        if (this.withUserUsername == null) {

            this.withUserUsername = "";
        }

        return this.withUserUsername;
    }

    public void setWithUserFullname(String withUserFullname) {

        this.withUserFullname = withUserFullname;
    }

    public String getWithUserFullname() {

        if (this.withUserFullname == null) {

            this.withUserFullname = "";
        }

        return this.withUserFullname;
    }

    public void setWithUserPhotoUrl(String withUserPhotoUrl) {

        this.withUserPhotoUrl = withUserPhotoUrl;
    }

    public String getWithUserPhotoUrl() {

        if (this.withUserPhotoUrl == null) {

            this.withUserPhotoUrl = "";
        }

        return this.withUserPhotoUrl;
    }

    public void setLastMessage(String lastMessage) {

        this.lastMessage = lastMessage;
    }

    public String getLastMessage() {

        if (this.lastMessage == null) {

            this.lastMessage = "";
        }

        return this.lastMessage;
    }

    public void setLastMessageAgo(String lastMessageAgo) {

        this.lastMessageAgo = lastMessageAgo;
    }

    public String getLastMessageAgo() {

        return this.lastMessageAgo;
    }

    public void setNewMessagesCount(int newMessagesCount) {

        this.newMessagesCount = newMessagesCount;
    }

    public int getNewMessagesCount() {

        return this.newMessagesCount;
    }

    public void setDate(String date) {

        this.date = date;
    }

    public String getDate() {

        if (this.date == null) {

            this.date = "";
        }

        return this.date;
    }

    public void setTimeAgo(String timeAgo) {

        this.timeAgo = timeAgo;
    }

    public String getTimeAgo() {

        if (this.timeAgo == null) {

            this.timeAgo = "";
        }

        return this.timeAgo;
    }

    public void setCreateAt(int createAt) {

        this.createAt = createAt;
    }

    public int getCreateAt() {

        return this.createAt;
    }

    public void setBlocked(Boolean blocked) {

        this.blocked = blocked;
    }

    public Boolean isBlocked() {

        return this.blocked;
    }

    public Boolean getBlocked() {

        return this.blocked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.withUserId);
        dest.writeLong(this.fromUserId);
        dest.writeLong(this.toUserId);
        dest.writeInt(this.id);
        dest.writeInt(this.withUserState);
        dest.writeInt(this.newMessagesCount);
        dest.writeInt(this.createAt);
        dest.writeInt(this.withUserVerify);
        dest.writeString(this.withUserUsername);
        dest.writeString(this.withUserFullname);
        dest.writeString(this.withUserPhotoUrl);
        dest.writeString(this.timeAgo);
        dest.writeString(this.date);
        dest.writeString(this.lastMessage);
        dest.writeString(this.lastMessageAgo);
        dest.writeValue(this.blocked);
    }

    protected Chat(Parcel in) {
        this.withUserId = in.readLong();
        this.fromUserId = in.readLong();
        this.toUserId = in.readLong();
        this.id = in.readInt();
        this.withUserState = in.readInt();
        this.newMessagesCount = in.readInt();
        this.createAt = in.readInt();
        this.withUserVerify = in.readInt();
        this.withUserUsername = in.readString();
        this.withUserFullname = in.readString();
        this.withUserPhotoUrl = in.readString();
        this.timeAgo = in.readString();
        this.date = in.readString();
        this.lastMessage = in.readString();
        this.lastMessageAgo = in.readString();
        this.blocked = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel source) {
            return new Chat(source);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };
}
