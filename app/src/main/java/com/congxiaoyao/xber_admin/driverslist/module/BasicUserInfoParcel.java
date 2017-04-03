package com.congxiaoyao.xber_admin.driverslist.module;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jaycejia on 2017/2/18.
 */
public class BasicUserInfoParcel implements Parcelable {
    private Long userId;

    private String name;

    private Byte gender;

    private String avatar;

    private Integer age;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getGender() {
        return gender;
    }

    public void setGender(Byte gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "BasicUserInfo{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.userId);
        dest.writeString(this.name);
        dest.writeValue(this.gender);
        dest.writeString(this.avatar);
        dest.writeValue(this.age);
    }

    public BasicUserInfoParcel() {
    }

    protected BasicUserInfoParcel(Parcel in) {
        this.userId = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
        this.gender = (Byte) in.readValue(Byte.class.getClassLoader());
        this.avatar = in.readString();
        this.age = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Parcelable.Creator<BasicUserInfoParcel> CREATOR = new Parcelable.Creator<BasicUserInfoParcel>() {
        @Override
        public BasicUserInfoParcel createFromParcel(Parcel source) {
            return new BasicUserInfoParcel(source);
        }

        @Override
        public BasicUserInfoParcel[] newArray(int size) {
            return new BasicUserInfoParcel[size];
        }
    };
}
