package ru.fruzz.extazyy.info;

import lombok.Getter;

public class BuildInfo {
    @Getter
    private String buildname;
    @Getter
    private String version;
    @Getter
    private int builddata;


    public BuildInfo(String buildname, String version, int builddata) {
        this.builddata = builddata;
        this.version = version;
        this.buildname = buildname;
    }

}
