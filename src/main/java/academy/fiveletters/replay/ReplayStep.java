package academy.fiveletters.replay;

import academy.fiveletters.game.GameActionResult;
import java.util.Objects;

public record ReplayStep(String input, GameActionResult result) {

    public ReplayStep {
        Objects.requireNonNull(input, "Ввод не должен быть null");
        Objects.requireNonNull(result, "Результат не должен быть null");
    }
}
