package academy.fiveletters.game;

import academy.fiveletters.word.WordRules;
import java.util.List;
import java.util.Objects;

public sealed interface GuessResult extends GameActionResult permits GuessResult.Accepted, GuessResult.Rejected {

    record Accepted(String guess, List<LetterStatus> letters, GameStatus status, int attemptsRemaining)
            implements GuessResult {

        public Accepted {
            WordRules.requireCanonical(guess, "Слово");
            Objects.requireNonNull(letters, "Статусы букв не должны быть null");
            Objects.requireNonNull(status, "Статус партии не должен быть null");

            letters = List.copyOf(letters);

            if (letters.size() != WordRules.length()) {
                throw new IllegalArgumentException("Результат должен содержать пять статусов букв");
            }

            if (attemptsRemaining < 0) {
                throw new IllegalArgumentException("Количество оставшихся попыток не может быть отрицательным");
            }

            boolean exactMatch = letters.stream().allMatch(letter -> letter == LetterStatus.EXACT);

            if (exactMatch != (status == GameStatus.WIN)) {
                throw new IllegalArgumentException("Статус победы должен соответствовать полному совпадению");
            }

            if (status == GameStatus.LOSE && attemptsRemaining != 0) {
                throw new IllegalArgumentException("При поражении не должно оставаться попыток");
            }

            if (status == GameStatus.IN_PROGRESS && attemptsRemaining == 0) {
                throw new IllegalArgumentException("Продолжающаяся партия должна иметь оставшиеся попытки");
            }
        }
    }

    record Rejected(GuessRejectionReason reason) implements GuessResult {

        public Rejected {
            Objects.requireNonNull(reason, "Причина отклонения не должна быть null");
        }
    }
}
