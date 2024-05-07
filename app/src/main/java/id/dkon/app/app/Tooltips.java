package id.dkon.app.app;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;

import id.dkon.app.constants.Constants;

public class Tooltips extends Application implements Constants, Parcelable {

    private Boolean show_otp_tooltip = true;

    public Tooltips() {

    }

    public void setShowOtpTooltip(Boolean show_otp_tooltip) {

        this.show_otp_tooltip = show_otp_tooltip;
    }

    public Boolean isAllowShowOtpTooltip() {

        return this.show_otp_tooltip;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeValue(this.show_otp_tooltip);
    }

    protected Tooltips(Parcel in) {

        this.show_otp_tooltip = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Creator<Tooltips> CREATOR = new Creator<Tooltips>() {
        @Override
        public Tooltips createFromParcel(Parcel source) {
            return new Tooltips(source);
        }

        @Override
        public Tooltips[] newArray(int size) {
            return new Tooltips[size];
        }
    };
}
