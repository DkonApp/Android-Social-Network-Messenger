package id.dkon.app.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import id.dkon.app.constants.Constants;


public class Feeling extends Application implements Constants, Parcelable {

    private long id;
    private int createAt;
    private String title, imgUrl;

    public Feeling() {

    }

    public Feeling(JSONObject jsonData) {

        try {

            if (!jsonData.getBoolean("error")) {

                this.setId(jsonData.getLong("id"));

                this.setImgUrl(jsonData.getString("imgUrl"));
                this.setTitle(jsonData.getString("title"));
                this.setCreateAt(jsonData.getInt("createAt"));
            }

        } catch (Throwable t) {

            Log.e("Feeling", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("Feeling", jsonData.toString());
        }
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    public void setCreateAt(int createAt) {
        this.createAt = createAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.createAt);
        dest.writeString(this.title);
        dest.writeString(this.imgUrl);
    }

    protected Feeling(Parcel in) {
        this.id = in.readLong();
        this.createAt = in.readInt();
        this.title = in.readString();
        this.imgUrl = in.readString();
    }

    public static final Creator<Feeling> CREATOR = new Creator<Feeling>() {
        @Override
        public Feeling createFromParcel(Parcel source) {
            return new Feeling(source);
        }

        @Override
        public Feeling[] newArray(int size) {
            return new Feeling[size];
        }
    };
}
