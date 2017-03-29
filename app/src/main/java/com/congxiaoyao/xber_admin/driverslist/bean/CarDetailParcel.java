package com.congxiaoyao.xber_admin.driverslist.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.congxiaoyao.httplib.response.BasicUserInfo;

/**
 * Created by Jaycejia on 2017/2/18.
 */
public class CarDetailParcel implements Parcelable {
    private Long carId;
    private BasicUserInfoParcel userInfo;
    private String plate;
    private String spec;

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public BasicUserInfoParcel getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(BasicUserInfoParcel userInfo) {
        this.userInfo = userInfo;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    @Override
    public String toString() {
        return "CarDetail{" +
                "carId=" + carId +
                ", userInfo=" + userInfo +
                ", plate='" + plate + '\'' +
                ", spec='" + spec + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.carId);
        dest.writeParcelable(this.userInfo, flags);
        dest.writeString(this.plate);
        dest.writeString(this.spec);
    }

    public CarDetailParcel() {
    }

    protected CarDetailParcel(Parcel in) {
        this.carId = (Long) in.readValue(Long.class.getClassLoader());
        this.userInfo = in.readParcelable(BasicUserInfoParcel.class.getClassLoader());
        this.plate = in.readString();
        this.spec = in.readString();
    }

    public static final Parcelable.Creator<CarDetailParcel> CREATOR = new Parcelable.Creator<CarDetailParcel>() {
        @Override
        public CarDetailParcel createFromParcel(Parcel source) {
            return new CarDetailParcel(source);
        }

        @Override
        public CarDetailParcel[] newArray(int size) {
            return new CarDetailParcel[size];
        }
    };
}
