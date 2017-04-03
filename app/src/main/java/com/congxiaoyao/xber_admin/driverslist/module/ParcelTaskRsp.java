package com.congxiaoyao.xber_admin.driverslist.module;

import android.os.Parcel;
import android.os.Parcelable;

import com.congxiaoyao.xber_admin.spotmanage.ParcelSpot;

/**
 * Created by Jaycejia on 2017/2/18.
 */
public class ParcelTaskRsp implements Parcelable {

    private Long taskId;

    private ParcelSpot startSpot;

    private ParcelSpot endSpot;

    private String content;

    private Long createUser;

    private Long realStartTime;

    private Long realEndTime;

    private Integer status;

    private String note;

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public ParcelSpot getStartSpot() {
        return startSpot;
    }

    public void setStartSpot(ParcelSpot startSpot) {
        this.startSpot = startSpot;
    }

    public ParcelSpot getEndSpot() {
        return endSpot;
    }

    public void setEndSpot(ParcelSpot endSpot) {
        this.endSpot = endSpot;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setRealEndTime(Long realEndTime) {
        this.realEndTime = realEndTime;
    }

    public Long getRealEndTime() {
        return realEndTime;
    }

    public void setRealStartTime(Long realStartTime) {
        this.realStartTime = realStartTime;
    }

    public Long getRealStartTime() {
        return realStartTime;
    }

    @Override
    public String toString() {
        return "ParcelTaskRsp{" +
                "taskId=" + taskId +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", note='" + note + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.taskId);
        dest.writeParcelable(this.startSpot, flags);
        dest.writeParcelable(this.endSpot, flags);
        dest.writeString(this.content);
        dest.writeValue(this.createUser);
        dest.writeValue(this.realStartTime);
        dest.writeValue(this.realEndTime);
        dest.writeValue(this.status);
        dest.writeString(this.note);
    }

    public ParcelTaskRsp() {
    }

    protected ParcelTaskRsp(Parcel in) {
        this.taskId = (Long) in.readValue(Long.class.getClassLoader());
        this.startSpot = in.readParcelable(ParcelSpot.class.getClassLoader());
        this.endSpot = in.readParcelable(ParcelSpot.class.getClassLoader());
        this.content = in.readString();
        this.createUser = (Long) in.readValue(Long.class.getClassLoader());
        this.realStartTime = (Long) in.readValue(Long.class.getClassLoader());
        this.realEndTime = (Long) in.readValue(Long.class.getClassLoader());
        this.status = (Integer) in.readValue(Integer.class.getClassLoader());
        this.note = in.readString();
    }

    public static final Parcelable.Creator<ParcelTaskRsp> CREATOR = new Parcelable.Creator<ParcelTaskRsp>() {
        @Override
        public ParcelTaskRsp createFromParcel(Parcel source) {
            return new ParcelTaskRsp(source);
        }

        @Override
        public ParcelTaskRsp[] newArray(int size) {
            return new ParcelTaskRsp[size];
        }
    };
}
