package id.dkon.app.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import id.dkon.app.constants.Constants;


public class GalleryItem implements Constants, Parcelable {

    private long id, fromUserId;
    private int itemType, createAt, likesCount, commentsCount;
    private String timeAgo, date, comment, imgUrl, previewImgUrl, originImgUrl, previewVideoImgUrl, videoUrl, area, country, city;
    private Double lat = 0.000000, lng = 0.000000;
    private Boolean myLike = false;
    private Profile owner = new Profile();

    public GalleryItem() {

    }

    public GalleryItem(JSONObject jsonData) {

        try {

            if (!jsonData.getBoolean("error")) {

                this.setId(jsonData.getLong("id"));
                this.setItemType(jsonData.getInt("itemType"));
                this.setFromUserId(jsonData.getLong("fromUserId"));
                this.setComment(jsonData.getString("comment"));
                this.setImgUrl(jsonData.getString("imgUrl"));
                this.setPreviewImgUrl(jsonData.getString("previewImgUrl"));
                this.setOriginImgUrl(jsonData.getString("originImgUrl"));
                this.setPreviewVideoImgUrl(jsonData.getString("previewVideoImgUrl"));
                this.setVideoUrl(jsonData.getString("videoUrl"));
                this.setArea(jsonData.getString("area"));
                this.setCountry(jsonData.getString("country"));
                this.setCity(jsonData.getString("city"));
                this.setCommentsCount(jsonData.getInt("commentsCount"));
                this.setLikesCount(jsonData.getInt("likesCount"));
                this.setLat(jsonData.getDouble("lat"));
                this.setLng(jsonData.getDouble("lng"));
                this.setCreateAt(jsonData.getInt("createAt"));
                this.setDate(jsonData.getString("date"));
                this.setTimeAgo(jsonData.getString("timeAgo"));

                this.setMyLike(jsonData.getBoolean("myLike"));

                if (jsonData.has("owner")) {

                    JSONObject ownerObj = (JSONObject) jsonData.getJSONObject("owner");

                    this.setOwner(new Profile(ownerObj));
                }
            }

        } catch (Throwable t) {

            Log.e("GalleryItem", "Could not parse malformed JSON: \"" + jsonData.toString() + "\"");

        } finally {

            Log.d("GalleryItem", jsonData.toString());
        }
    }

    public Profile getOwner() {

        return this.owner;
    }

    public void setOwner(Profile owner) {

        this.owner = owner;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public long getFromUserId() {

        return fromUserId;
    }

    public void setFromUserId(long fromUserId) {

        this.fromUserId = fromUserId;
    }

    public int getCommentsCount() {

        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getLikesCount() {

        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCreateAt() {

        return createAt;
    }

    public void setCreateAt(int createAt) {
        this.createAt = createAt;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public void setTimeAgo(String timeAgo) {
        this.timeAgo = timeAgo;
    }

    public String getComment() {

        if (this.comment == null) {

            this.comment = "";
        }

        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImgUrl() {

        if (this.imgUrl == null) {

            this.imgUrl = "";
        }

        return this.imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getPreviewImgUrl() {

        if (this.previewImgUrl == null) {

            this.previewImgUrl = "";
        }

        return previewImgUrl;
    }

    public void setPreviewImgUrl(String previewImgUrl) {
        this.previewImgUrl = previewImgUrl;
    }

    public String getOriginImgUrl() {

        if (this.originImgUrl == null) {

            this.originImgUrl = "";
        }

        return originImgUrl;
    }

    public void setOriginImgUrl(String originImgUrl) {
        this.originImgUrl = originImgUrl;
    }

    public String getPreviewVideoImgUrl() {

        if (this.previewVideoImgUrl == null) {

            this.previewVideoImgUrl = "";
        }

        return previewVideoImgUrl;
    }

    public void setPreviewVideoImgUrl(String previewVideoImgUrl) {
        this.previewVideoImgUrl = previewVideoImgUrl;
    }

    public String getVideoUrl() {

        if (this.videoUrl == null) {

            this.videoUrl = "";
        }

        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getArea() {

        if (this.area == null) {

            this.area = "";
        }

        return this.area;
    }

    public void setArea(String area) {

        this.area = area;
    }

    public String getCountry() {

        if (this.country == null) {

            this.country = "";
        }

        return this.country;
    }

    public void setCountry(String country) {

        this.country = country;
    }

    public String getCity() {

        if (this.city == null) {

            this.city = "";
        }

        return this.city;
    }

    public void setCity(String city) {

        this.city = city;
    }

    public Double getLat() {

        return this.lat;
    }

    public void setLat(Double lat) {

        this.lat = lat;
    }

    public Double getLng() {

        return this.lng;
    }

    public void setLng(Double lng) {

        this.lng = lng;
    }

    public String getLink() {

        return WEB_SITE + this.owner.getUsername() + "/post/" + Long.toString(this.getId());
    }

    public Boolean isMyLike() {
        return myLike;
    }

    public void setMyLike(Boolean myLike) {

        this.myLike = myLike;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.itemType);
        dest.writeLong(this.fromUserId);
        dest.writeInt(this.createAt);
        dest.writeInt(this.likesCount);
        dest.writeInt(this.commentsCount);
        dest.writeString(this.timeAgo);
        dest.writeString(this.date);
        dest.writeString(this.comment);
        dest.writeString(this.imgUrl);
        dest.writeString(this.previewImgUrl);
        dest.writeString(this.originImgUrl);
        dest.writeString(this.previewVideoImgUrl);
        dest.writeString(this.videoUrl);
        dest.writeString(this.area);
        dest.writeString(this.country);
        dest.writeString(this.city);
        dest.writeValue(this.lat);
        dest.writeValue(this.lng);
        dest.writeValue(this.myLike);
        dest.writeParcelable(this.owner, flags);
    }

    protected GalleryItem(Parcel in) {
        this.id = in.readLong();
        this.itemType = in.readInt();
        this.fromUserId = in.readLong();
        this.createAt = in.readInt();
        this.likesCount = in.readInt();
        this.commentsCount = in.readInt();
        this.timeAgo = in.readString();
        this.date = in.readString();
        this.comment = in.readString();
        this.imgUrl = in.readString();
        this.previewImgUrl = in.readString();
        this.originImgUrl = in.readString();
        this.previewVideoImgUrl = in.readString();
        this.videoUrl = in.readString();
        this.area = in.readString();
        this.country = in.readString();
        this.city = in.readString();
        this.lat = (Double) in.readValue(Double.class.getClassLoader());
        this.lng = (Double) in.readValue(Double.class.getClassLoader());
        this.myLike = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.owner = (Profile) in.readParcelable(Profile.class.getClassLoader());
    }

    public static final Creator<GalleryItem> CREATOR = new Creator<GalleryItem>() {
        @Override
        public GalleryItem createFromParcel(Parcel source) {
            return new GalleryItem(source);
        }

        @Override
        public GalleryItem[] newArray(int size) {
            return new GalleryItem[size];
        }
    };
}
