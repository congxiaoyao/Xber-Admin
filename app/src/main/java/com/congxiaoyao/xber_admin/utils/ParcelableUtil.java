package com.congxiaoyao.xber_admin.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.congxiaoyao.xber_admin.spotmanage.ParcelSpot;

/**
 * Created by dongyayun on 16/1/17.
 */
public class ParcelableUtil {
    public static byte[] marshall(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static <T extends Parcelable> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        return creator.createFromParcel(parcel);
    }

}