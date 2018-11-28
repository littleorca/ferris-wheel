package com.ctrip.ferriswheel.core.bean;

import java.io.Serializable;

public final class Version implements Serializable {
    private final int major;
    private final int minor;
    private final int build;

    public Version(Version another) {
        this(another.major, another.minor, another.build);
    }

    public Version(int major, int minor, int build) {
        this.major = major;
        this.minor = minor;
        this.build = build;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getBuild() {
        return build;
    }

}
