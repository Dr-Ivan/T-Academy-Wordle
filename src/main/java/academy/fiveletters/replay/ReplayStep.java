package academy.fiveletters.replay;

import academy.fiveletters.game.GuessResult;
import java.util.Objects;

public record ReplayStep(String input, GuessResult result) {

    public ReplayStep {
        Objects.requireNonNull(input, "Ввод не должен быть null");
        Objects.requireNonNull(result, "Результат не должен быть null");
    }
}
