package com.congxiaoyao.xber_admin.spotmanage;

import android.os.Parcel;
import android.os.Parcelable;

import com.congxiaoyao.httplib.response.Spot;

/**
 * Created by congxiaoyao on 2017/3/30.
 */

public class ParcelSpot implements Parcelable {

    private Long spotId;

    private String spotName;

    private Double latitude;

    private Double longitude;

    public ParcelSpot(Long spotId, String spotName, Double latitude, Double longitude) {
        this.spotId = spotId;
        this.spotName = spotName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }

    public String getSpotName() {
        return spotName;
    }

    public void setSpotName(String spotName) {
        this.spotName = spotName == null ? null : spotName.trim();
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "ParcelSpot{" +
                "spotId=" + spotId +
                ", spotName='" + spotName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.spotId);
        dest.writeString(this.spotName);
        dest.writeValue(this.latitude);
        dest.writeValue(this.longitude);
    }

    protected ParcelSpot(Parcel in) {
        this.spotId = (Long) in.readValue(Long.class.getClassLoader());
        this.spotName = in.readString();
        this.latitude = (Double) in.readValue(Double.class.getClassLoader());
        this.longitude = (Double) in.readValue(Double.class.getClassLoader());
    }

    public static final Parcelable.Creator<ParcelSpot> CREATOR = new Parcelable.Creator<ParcelSpot>() {
        @Override
        public ParcelSpot createFromParcel(Parcel source) {
            return new ParcelSpot(source);
        }

        @Override
        public ParcelSpot[] newArray(int size) {
            return new ParcelSpot[size];
        }
    };
}
