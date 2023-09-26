package com.lightmatter.clickshow.model;

public class Configuration {

    private int id;
    private String configType;
    private String configValue;

    // 构造方法
    public Configuration(int id, String configType, String configValue) {
        this.id = id;
        this.configType = configType;
        this.configValue = configValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                "id=" + id +
                ", configType='" + configType + '\'' +
                ", configValue='" + configValue + '\'' +
                '}';
    }
}
