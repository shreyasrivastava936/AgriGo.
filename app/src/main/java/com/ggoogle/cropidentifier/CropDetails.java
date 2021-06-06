package com.ggoogle.cropidentifier;

public class CropDetails {
    private String district;
    private String userId;
    private MonthWiseDetails monthWiseDetails;

    @Override
    public String toString() {
        return "CropDetails{" +
                "district='" + district + '\'' +
                ", userId='" + userId + '\'' +
                ", monthWiseDetails=" + monthWiseDetails +
                '}';
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public MonthWiseDetails getMonthWiseDetails() {
        return monthWiseDetails;
    }

    public void setMonthWiseDetails(MonthWiseDetails monthWiseDetails) {
        this.monthWiseDetails = monthWiseDetails;
    }
}
