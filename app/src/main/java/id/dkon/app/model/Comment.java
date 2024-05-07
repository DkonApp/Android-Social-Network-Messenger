package id.dkon.app.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import id.dkon.app.constants.Constants;

public class Comment implements Constants, Parcelable {

    private long id, itemId, fromUserId, itemFromUserId, replyToUserId;
    private int fromUserState, fromUserVerified, createAt;
    private String comment, replyToUserUsername, replyToUserFullname, timeAgo;
    private Profile owner;

    public Comment() {

    }

    public Comment(JSONObject jsonData) {

        try {

            this.setId(jsonData.getLong("id"));
            this.setFromUserId(jsonData.getLong("fromUserId"));
            this.setReplyToUserId(jsonData.getLong("replyToUserId"));
            this.setReplyToUserUsername(jsonData.getString("replyToUserUsername"));
            this.setReplyToUserFullname(jsonData.getString("replyToFullname"));
            this.setText(jsonData.getString("comment"));
            this.setTimeAgo(jsonData.getString("timeAgo"));
            this.setCreateAt(jsonData.getInt("createAt"));

            if (jsonData.has("itemId")) {

                this.setItemId(jsonData.getLong("itemId"));
            }

            if (jsonData.has("owner")) {

                JSONObject ownerObj = (JSONObject) jsonData.getJSONObject("owner");

                this.setOwner(new Profile(ownerObj));
            }

            if (jsonData.has("itemFromUserId")) {

                this.setItemFromUserId(jsonData.getLong("itemFromUserId"));
            }

        } catch (Throwable t) {

            Log.e("Comment", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Comment", jsonData.toString());
        }
    }

    public Profile getOwner() {
        return this.owner;
    }

    public void setOwner(Profile owner) {
        this.owner = owner;
    }

    public void setId(long id) {

        this.id = id;
    }

    public long getId() {

        return this.id;
    }

    public void setItemId(long itemId) {

        this.itemId = itemId;
    }

    public long getItemId() {

        return this.itemId;
    }

    public void setFromUserId(long fromUserId) {

        this.fromUserId = fromUserId;
    }

    public long getFromUserId() {

        return this.fromUserId;
    }

    public void setItemFromUserId(long itemFromUserId) {

        this.itemFromUserId = itemFromUserId;
    }

    public long getItemFromUserId() {

        return this.itemFromUserId;
    }

    public void setReplyToUserId(long replyToUserId) {

        this.replyToUserId = replyToUserId;
    }

    public long getReplyToUserId() {

        return this.replyToUserId;
    }

    public void setFromUserState(int fromUserState) {

        this.fromUserState = fromUserState;
    }

    public int getFromUserState() {

        return this.fromUserState;
    }

    public void setFromUserVerified(int fromUserVerified) {

        this.fromUserVerified = fromUserVerified;
    }

    public int getFromUserVerified() {

        return this.fromUserVerified;
    }

    public void setText(String comment) {

        this.comment = comment;
    }

    public String getText() {

        if (this.comment == null) {

            this.comment = "";
        }

        return this.comment;
    }

    public void setTimeAgo(String timeAgo) {

        this.timeAgo = timeAgo;
    }

    public String getTimeAgo() {

        return this.timeAgo;
    }

    public void setReplyToUserUsername(String replyToUserUsername) {

        this.replyToUserUsername = replyToUserUsername;
    }

    public String getReplyToUserUsername() {

        return this.replyToUserUsername;
    }

    public void setReplyToUserFullname(String replyToUserFullname) {

        this.replyToUserFullname = replyToUserFullname;
    }

    public String getReplyToUserFullname() {

        return this.replyToUserFullname;
    }

    public void setCreateAt(int createAt) {

        this.createAt = createAt;
    }

    public int getCreateAt() {

        return this.createAt;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.itemId);
        dest.writeLong(this.fromUserId);
        dest.writeLong(this.replyToUserId);
        dest.writeInt(this.fromUserState);
        dest.writeInt(this.fromUserVerified);
        dest.writeInt(this.createAt);
        dest.writeString(this.comment);
        dest.writeString(this.replyToUserUsername);
        dest.writeString(this.replyToUserFullname);
        dest.writeString(this.timeAgo);
        dest.writeParcelable(this.owner, flags);
        dest.writeLong(this.itemFromUserId);
    }

    protected Comment(Parcel in) {
        this.id = in.readLong();
        this.itemId = in.readLong();
        this.fromUserId = in.readLong();
        this.replyToUserId = in.readLong();
        this.fromUserState = in.readInt();
        this.fromUserVerified = in.readInt();
        this.createAt = in.readInt();
        this.comment = in.readString();
        this.replyToUserUsername = in.readString();
        this.replyToUserFullname = in.readString();
        this.timeAgo = in.readString();
        this.owner = (Profile) in.readParcelable(Profile.class.getClassLoader());
        this.itemFromUserId = in.readLong();
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        @Override
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        @Override
        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
}
