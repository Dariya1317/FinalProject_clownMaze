package com.example.clownmaze.core.riddles;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public final class RiddleFactory {

    private RiddleFactory() {}

    /** Load riddles for a specific level+room pair.
     *  Falls back to the original room-only files for Level 1. */
    public static List<BaseRiddle> loadRoom(int level, int roomId) {
        String path = "riddles/level" + level + "/room" + roomId + "_riddles.json";
        if (!Gdx.files.internal(path).exists()) {
            path = "riddles/room" + roomId + "_riddles.json";
        }
        String raw = Gdx.files.internal(path).readString("UTF-8");
        JsonValue root = new JsonReader().parse(raw);
        List<BaseRiddle> result = new ArrayList<>();
        for (JsonValue entry : root.get("riddles")) {
            result.add(createFromJson(roomId, entry));
        }
        return result;
    }

    /** Legacy overload — keeps existing call sites working. */
    public static List<BaseRiddle> loadRoom(int roomId) {
        return loadRoom(1, roomId);
    }

    /** Build a riddle from JSON using the "type" field (text / choice). */
    private static BaseRiddle createFromJson(int roomId, JsonValue entry) {
        int    id       = entry.getInt("id");
        String question = entry.getString("question");
        String answer   = entry.getString("answer");
        String type     = entry.getString("type", "text");

        if ("choice".equals(type)) {
            return new JavaSyntaxRiddle(roomId, id, question, answer, parseChoices(entry));
        }
        return new MathRiddle(roomId, id, question, answer);
    }

    private static List<String> parseChoices(JsonValue entry) {
        List<String> choices = new ArrayList<>();
        JsonValue arr = entry.get("choices");
        if (arr != null) {
            for (JsonValue c : arr) {
                choices.add(c.asString());
            }
        }
        return choices;
    }
}
