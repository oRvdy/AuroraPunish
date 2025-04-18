package com.auroramc.punish.database.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum DataType {

    MONGODB("MongoDB"),
    REDIS("Redis");

    private final String name;
}
