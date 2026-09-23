package academy.fiveletters.cli;

import academy.fiveletters.game.GameStatus;
import academy.fiveletters.game.GuessRejectionReason;
import academy.fiveletters.game.GuessResult;
import academy.fiveletters.game.HintResult;
import java.util.Objects;

public final class ConsoleFormatter {

    public String formatGuess(GuessResult.Accepted result) {
        Objects.requireNonNull(result, "Результат не должен быть null");

        StringBuilder feedback = new StringBuilder();
        result.letters().forEach(letterStatus -> feedback.append(letterStatus.symbol()));
        return feedback + " " + result.guess();
    }

    public String formatRejection(GuessRejectionReason reason) {
        Objects.requireNonNull(reason, "Причина отклонения не должна быть null");
        return reason.message();
    }

    public String formatOutcome(GameStatus status, int attemptsUsed, String answer) {
        Objects.requireNonNull(status, "Статус не должен быть null");
        Objects.requireNonNull(answer, "Ответ не должен быть null");
        return status.messageTemplate().formatted(attemptsUsed, answer);
    }

    public String formatHint(HintResult result) {
        Objects.requireNonNull(result, "Результат подсказки не должен быть null");

        return switch (result) {
            case HintResult.Revealed revealed ->
                "Подсказка: на позиции %d находится буква \"%s\".".formatted(revealed.position(), revealed.letter());
            case HintResult.Rejected rejected -> rejected.reason().message();
        };
    }
}
