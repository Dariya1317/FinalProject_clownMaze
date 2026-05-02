package com.example.clownmaze.core.riddles;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public final class RiddleFactory {

    private RiddleFactory() {}
    public static List<BaseRiddle> loadRoom(int roomId) {
        String path = "riddles/room" + roomId + "_riddles.json";
        String raw = Gdx.files.internal(path).readString("UTF-8");
        JsonValue root = new JsonReader().parse(raw);
        List<BaseRiddle> result = new ArrayList<>();
        for (JsonValue entry : root.get("riddles")) {
            result.add(createFromJson(roomId, entry));
        }
        return result;
    }
    private static BaseRiddle createFromJson(int roomId, JsonValue entry) {
        int    id = entry.getInt("id");
        String question = entry.getString("question");
        String answer = entry.getString("answer");

        return switch (roomId) {
            case 1 -> new MathRiddle(roomId, id, question, answer);
            case 2 -> new JavaSyntaxRiddle(roomId, id, question, answer,
                                            parseChoices(entry));
            case 3 -> new PatternRiddle(roomId, id, question, answer,
                                         parseChoices(entry));
            default -> throw new IllegalArgumentException("Unknown roomId: " + roomId);
        };
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
