package com.congxiaoyao.xber_admin.publishedtask.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.congxiaoyao.httplib.response.CarDetail;
import com.congxiaoyao.httplib.response.Task;
import com.congxiaoyao.xber_admin.spotmanage.ParcelSpot;

/**
 * Created by congxiaoyao on 2017/4/3.
 */

public class TaskRspAndDriver implements Parcelable {

    private Long taskId;

    private Long carId;

    private Long startTime;

    private ParcelSpot startSpot;

    private Long endTime;

    private ParcelSpot endSpot;

    private String content;

    private Long createUser;

    private Long createTime;

    private Long realStartTime;

    private Long realEndTime;

    private Integer status;

    private String note;

    private CarDetail carDetail;

    public CarDetail getCarDetail() {
        return carDetail;
    }

    public void setCarDetail(CarDetail carDetail) {
        this.carDetail = carDetail;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public ParcelSpot getStartSpot() {
        return startSpot;
    }

    public void setStartSpot(ParcelSpot startSpot) {
        this.startSpot = startSpot;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
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

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getRealStartTime() {
        return realStartTime;
    }

    public void setRealStartTime(Long realStartTime) {
        this.realStartTime = realStartTime;
    }

    public Long getRealEndTime() {
        return realEndTime;
    }

    public void setRealEndTime(Long realEndTime) {
        this.realEndTime = realEndTime;
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

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String generateStatusDefaultName() {
        switch (status) {
            case Task.STATUS_COMPLETED:
                return "已送达";
            case Task.STATUS_DELIVERED:
                return "待出发";
            case Task.STATUS_EXECUTING:
                return "运输中";
            default:
                return "???";
        }
    }

    @Override
    public String toString() {
        return "TaskRsp{" +
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
        dest.writeValue(this.carId);
        dest.writeValue(this.startTime);
        dest.writeParcelable(this.startSpot, flags);
        dest.writeValue(this.endTime);
        dest.writeParcelable(this.endSpot, flags);
        dest.writeString(this.content);
        dest.writeValue(this.createUser);
        dest.writeValue(this.createTime);
        dest.writeValue(this.realStartTime);
        dest.writeValue(this.realEndTime);
        dest.writeValue(this.status);
        dest.writeString(this.note);
    }

    public TaskRspAndDriver() {
    }

    protected TaskRspAndDriver(Parcel in) {
        this.taskId = (Long) in.readValue(Long.class.getClassLoader());
        this.carId = (Long) in.readValue(Long.class.getClassLoader());
        this.startTime = (Long) in.readValue(Long.class.getClassLoader());
        this.startSpot = in.readParcelable(ParcelSpot.class.getClassLoader());
        this.endTime = (Long) in.readValue(Long.class.getClassLoader());
        this.endSpot = in.readParcelable(ParcelSpot.class.getClassLoader());
        this.content = in.readString();
        this.createUser = (Long) in.readValue(Long.class.getClassLoader());
        this.createTime = (Long) in.readValue(Long.class.getClassLoader());
        this.realStartTime = (Long) in.readValue(Long.class.getClassLoader());
        this.realEndTime = (Long) in.readValue(Long.class.getClassLoader());
        this.status = (Integer) in.readValue(Integer.class.getClassLoader());
        this.note = in.readString();
    }

    public static final Parcelable.Creator<TaskRspAndDriver> CREATOR = new Parcelable.Creator<TaskRspAndDriver>() {
        @Override
        public TaskRspAndDriver createFromParcel(Parcel source) {
            return new TaskRspAndDriver(source);
        }

        @Override
        public TaskRspAndDriver[] newArray(int size) {
            return new TaskRspAndDriver[size];
        }
    };
}
