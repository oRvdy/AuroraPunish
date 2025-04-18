package com.auroramc.punish.types;

import com.auroramc.punish.utils.TimeUtils;

public class PunishmentType {
    public enum Type {
        MUTE,
        BAN
    }

    private final Type type;
    private final long durationMillis;

    public PunishmentType(Type type, long durationMillis) {
        this.type = type;
        this.durationMillis = durationMillis;
    }

    public Type getType() {
        return type;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public String getDuration() {
        return TimeUtils.millisToReadable(durationMillis);
    }
}
