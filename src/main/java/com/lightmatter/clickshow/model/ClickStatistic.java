package com.lightmatter.clickshow.model;

public class ClickStatistic implements Comparable<ClickStatistic> {

    private String hourKey;
    private int mouseClickCount;
    private int keyClickCount;
    private java.sql.Timestamp createTime;

    // Constructors
    public ClickStatistic() {
    }

    public ClickStatistic(String hourKey, int mouseClickCount, int keyClickCount, java.sql.Timestamp createTime) {
        this.hourKey = hourKey;
        this.mouseClickCount = mouseClickCount;
        this.keyClickCount = keyClickCount;
        this.createTime = createTime;
    }

    // Getter and Setter methods
    public String getHourKey() {
        return hourKey;
    }

    public void setHourKey(String hourKey) {
        this.hourKey = hourKey;
    }

    public int getMouseClickCount() {
        return mouseClickCount;
    }

    public void setMouseClickCount(int mouseClickCount) {
        this.mouseClickCount = mouseClickCount;
    }

    public int getKeyClickCount() {
        return keyClickCount;
    }

    public void setKeyClickCount(int keyClickCount) {
        this.keyClickCount = keyClickCount;
    }

    public java.sql.Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(java.sql.Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ClickStatistic{" +
                "hourKey='" + hourKey + '\'' +
                ", mouseClickCount=" + mouseClickCount +
                ", keyClickCount=" + keyClickCount +
                ", createTime=" + createTime +
                '}';
    }

    @Override
    public int compareTo(ClickStatistic o) {
        return this.getCreateTime().compareTo(o.getCreateTime());
    }
}
