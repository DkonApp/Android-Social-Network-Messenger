package id.dkon.app.model;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;

import id.dkon.app.constants.Constants;

public class MediaItem extends Application implements Constants, Parcelable {

    private String selectedImageFileName, imageUrl;
    private String selectedVideoFileName, videoUrl;
    private int type = 0; // 0 = image; 1 = video

    public MediaItem() {

    }

    public MediaItem(String selectedImageFileName) {

        this.setSelectedImageFileName(selectedImageFileName);
    }

    public MediaItem(String selectedImageFileName, String selectedVideoFileName, String imageUrl, String videoUrl, int type) {

        this.setSelectedImageFileName(selectedImageFileName);
        this.setSelectedVideoFileName(selectedVideoFileName);
        this.setImageUrl(imageUrl);
        this.setVideoUrl(videoUrl);
        this.setType(type);
    }

    //

    public void setType(int type) {

        this.type = type;
    }

    public int getType() {

        return this.type;
    }

    // Image

    public void setSelectedImageFileName(String selectedImageFileName) {

        this.selectedImageFileName = selectedImageFileName;
    }

    public String getSelectedImageFileName() {

        return this.selectedImageFileName;
    }

    public void setImageUrl(String imageUrl) {

        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {

        if (this.imageUrl == null) {

            this.imageUrl = "";
        }

        return this.imageUrl;
    }

    // Video

    public void setSelectedVideoFileName(String selectedVideoFileName) {

        this.selectedVideoFileName = selectedVideoFileName;
    }

    public String getSelectedVideoFileName() {

        return this.selectedVideoFileName;
    }

    public void setVideoUrl(String videoUrl) {

        this.videoUrl = videoUrl;
    }

    public String getVideoUrl() {

        if (this.videoUrl == null) {

            this.videoUrl = "";
        }

        return this.videoUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.selectedImageFileName);
        dest.writeString(this.imageUrl);

        dest.writeString(this.videoUrl);
        dest.writeString(this.selectedVideoFileName);

        dest.writeInt(this.type);

    }

    protected MediaItem(Parcel in) {
        this.selectedImageFileName = in.readString();
        this.imageUrl = in.readString();

        this.videoUrl = in.readString();
        this.selectedVideoFileName = in.readString();

        this.type = in.readInt();
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel source) {
            return new MediaItem(source);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };
}
