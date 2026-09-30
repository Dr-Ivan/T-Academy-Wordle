package academy.fiveletters.settings;

import java.util.Objects;

public record GameSettings(Difficulty difficulty, WordCategory category) {

    public static final GameSettings DEFAULT = new GameSettings(Difficulty.STANDARD, WordCategory.ALL);

    public GameSettings {
        Objects.requireNonNull(difficulty, "Сложность не должна быть null");
        Objects.requireNonNull(category, "Категория не должна быть null");
    }
}
