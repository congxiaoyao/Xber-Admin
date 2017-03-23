package com.congxiaoyao.xber_admin.dispatch;

/**
 * Created by guo on 2017/3/16.
 */

public class CheckedFreeCar {

    private long carId;
    private String driverName;
    private String carPlate;
    private String carType;
    private boolean choosed = false;

    public CheckedFreeCar(long carId, String driverName, String carPlate, String carType) {
        this.carId = carId;
        this.driverName = driverName;
        this.carPlate = carPlate;
        this.carType = carType;
    }

    public long getCarId() {
        return carId;
    }

    public void setCarId(long carId) {
        this.carId = carId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getCarPlate() {
        return carPlate;
    }

    public void setCarPlate(String carPlate) {
        this.carPlate = carPlate;
    }

    public String getCarType() {
        return carType;
    }

    public void setCarType(String carType) {
        this.carType = carType;
    }

    public boolean isChoosed() {
        return choosed;
    }

    public void setChoosed(boolean choosed) {
        this.choosed = choosed;
    }
}
