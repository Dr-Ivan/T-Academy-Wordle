package academy.fiveletters.cli;

import academy.fiveletters.game.GameStatus;
import academy.fiveletters.game.GuessRejectionReason;
import academy.fiveletters.game.GuessResult;
import academy.fiveletters.game.HintResult;
import academy.fiveletters.game.LetterStatus;
import java.util.Objects;

public final class ConsoleFormatter {

    private static final String ANSI_RESET = "\u001B[0m";

    private static final int DECIMAL_BASE = 10;
    private static final int LAST_TWO_DIGITS_DIVISOR = 100;
    private static final int SPECIAL_ENDING_START = 11;
    private static final int SPECIAL_ENDING_END = 14;
    private static final int FEW_ENDING_MAX = 4;

    private final ColorMode colorMode;

    public ConsoleFormatter() {
        this(ColorMode.NEVER);
    }

    public ConsoleFormatter(ColorMode colorMode) {
        this.colorMode = Objects.requireNonNull(colorMode, "Режим цвета не должен быть null");
    }

    public String formatGuess(GuessResult.Accepted result) {
        Objects.requireNonNull(result, "Результат не должен быть null");

        StringBuilder feedback = new StringBuilder();
        StringBuilder word = new StringBuilder();

        for (int index = 0; index < result.letters().size(); index++) {
            LetterStatus status = result.letters().get(index);
            feedback.append(status.symbol());

            if (colorMode == ColorMode.ALWAYS) {
                word.append(status.ansiColor());
            }

            word.append(result.guess().charAt(index));
            if (colorMode == ColorMode.ALWAYS) {
                word.append(ANSI_RESET);
            }
        }
        return feedback + " " + word;
    }

    public String formatRejection(GuessRejectionReason reason) {
        Objects.requireNonNull(reason, "Причина отклонения не должна быть null");
        return reason.message();
    }

    public String formatOutcome(GameStatus status, int attemptsUsed, String answer) {
        Objects.requireNonNull(status, "Статус не должен быть null");
        Objects.requireNonNull(answer, "Ответ не должен быть null");

        if (attemptsUsed < 0) {
            throw new IllegalArgumentException("Количество использованных попыток не может быть отрицательным");
        }
        return status.messageTemplate().formatted(attemptsUsed, answer, attemptsWord(attemptsUsed));
    }

    public String formatHint(HintResult result) {
        Objects.requireNonNull(result, "Результат подсказки не должен быть null");

        return switch (result) {
            case HintResult.Revealed revealed ->
                "Подсказка: на позиции %d находится буква \"%s\".".formatted(revealed.position(), revealed.letter());
            case HintResult.Rejected rejected -> rejected.reason().message();
        };
    }

    private String attemptsWord(int count) {
        int lastTwoDigits = count % LAST_TWO_DIGITS_DIVISOR;
        if (lastTwoDigits >= SPECIAL_ENDING_START && lastTwoDigits <= SPECIAL_ENDING_END) {
            return "попыток";
        }
        int lastDigit = count % DECIMAL_BASE;
        if (lastDigit == 1) {
            return "попытку";
        }
        if (lastDigit >= 2 && lastDigit <= FEW_ENDING_MAX) {
            return "попытки";
        }
        return "попыток";
    }
}
