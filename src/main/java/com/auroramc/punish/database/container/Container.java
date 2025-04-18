package com.auroramc.punish.database.container;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Getter
@AllArgsConstructor
public abstract class Container {

    @NonNull
    private String key;

    @Setter
    private Object value;

    public String parseToString() {
        return String.valueOf(this.value);
    }

    public Long parseToLong() {
        return Long.parseLong(this.value.toString());
    }

    public Integer parseToInt() {
        return Integer.parseInt(this.value.toString());
    }

    public JSONObject parseToJson() {
        try {
            return (JSONObject) new JSONParser().parse(this.value.toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONArray parseToJsonArray() {
        try {
            return (JSONArray) new JSONParser().parse(this.value.toString());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void save();
    public abstract void load();
}
