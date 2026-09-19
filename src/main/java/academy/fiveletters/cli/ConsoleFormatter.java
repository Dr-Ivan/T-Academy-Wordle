package academy.fiveletters.cli;

import academy.fiveletters.game.GuessRejectionReason;
import academy.fiveletters.game.GuessResult;
import academy.fiveletters.game.LetterStatus;
import java.util.Objects;

public final class ConsoleFormatter {

    public String formatGuess(GuessResult.Accepted result) {
        Objects.requireNonNull(result, "Результат не должен быть null");

        StringBuilder feedback = new StringBuilder();

        for (LetterStatus letter : result.letters()) {
            feedback.append(
                    switch (letter) {
                        case EXACT -> "✅";
                        case PRESENT -> "🟡";
                        case ABSENT -> "❌";
                    });
        }

        return feedback + " " + result.guess();
    }

    public String formatRejection(GuessRejectionReason reason) {
        Objects.requireNonNull(reason, "Причина отклонения не должна быть null");

        return switch (reason) {
            case INVALID_LENGTH -> "Введите слово из пяти букв.";
            case INVALID_CHARACTERS -> "Допустимы только русские буквы без ё.";
            case WORD_NOT_IN_DICTIONARY -> "Такого слова нет в словаре.";
            case GAME_FINISHED -> "Партия уже завершена.";
        };
    }
}
