package com.congxiaoyao;

import android.content.Context;
import android.content.SharedPreferences;

import com.congxiaoyao.httplib.request.gson.GsonHelper;

/**
 * Created by congxiaoyao on 2017/3/15.
 */

public class Admin {

    private String userName;
    private String password;

    private String nickName;

    private String token;

    public Admin(String userName, String password, String nickName, String token) {
        this.userName = userName;
        this.password = password;
        this.nickName = nickName;
        this.token = token;
    }

    public Admin() {

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static Admin fromSharedPreference(Context context) {
        SharedPreferences xber_sp = context.getSharedPreferences("xber_sp", Context.MODE_PRIVATE);
        return fromSharedPreference(xber_sp);
    }

    private static Admin fromSharedPreference(SharedPreferences sharedPreferences) {
        String json = sharedPreferences.getString("admin", null);
        if (json == null) return null;
        Admin admin = GsonHelper.getInstance().fromJson(json, Admin.class);
        return admin;
    }

    public void save(Context context) {
        SharedPreferences xber_sp = context.getSharedPreferences("xber_sp", Context.MODE_PRIVATE);
        save(xber_sp);
    }

    private void save(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor edit = sharedPreferences.edit();
        String json = GsonHelper.getInstance().toJson(this);
        edit.putString("admin", json);
        edit.commit();
    }

    @Override
    public String toString() {
        return "Admin{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", nickName='" + nickName + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}

