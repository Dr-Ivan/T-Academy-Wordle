package academy.fiveletters.game;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public sealed interface GuessResult permits GuessResult.Accepted, GuessResult.Rejected {

    record Accepted(String guess, List<LetterStatus> letters, GameStatus status, int attemptsRemaining)
            implements GuessResult {

        private static final int WORD_LENGTH = 5;
        private static final Pattern WORD_PATTERN = Pattern.compile("[а-я]{" + WORD_LENGTH + "}");

        public Accepted {
            Objects.requireNonNull(guess, "Слово не должно быть null");
            Objects.requireNonNull(letters, "Статусы букв не должны быть null");
            Objects.requireNonNull(status, "Статус партии не должен быть null");

            if (!WORD_PATTERN.matcher(guess).matches()) {
                throw new IllegalArgumentException("Слово должно содержать пять русских букв в нижнем регистре без ё");
            }

            letters = List.copyOf(letters);

            if (letters.size() != WORD_LENGTH) {
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
