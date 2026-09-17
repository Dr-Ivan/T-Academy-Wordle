package academy.fiveletters.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/** Сравнивает ответ и попытку с учётом количества повторяющихся букв. */
public final class LetterMatcher {

    private static final int WORD_LENGTH = 5;
    private static final char FIRST_LETTER = 'а';
    private static final int ALPHABET_SIZE = 'я' - FIRST_LETTER + 1;
    private static final Pattern WORD_PATTERN = Pattern.compile("[а-я]{" + WORD_LENGTH + "}");

    /**
     * Возвращает неизменяемый список из пяти статусов.
     *
     * <p>Оба аргумента должны состоять из пяти русских букв в нижнем регистре без ё.
     */
    public List<LetterStatus> match(String answer, String guess) {
        validateWord(answer, "Ответ");
        validateWord(guess, "Попытка");

        List<LetterStatus> result = new ArrayList<>(Collections.nCopies(WORD_LENGTH, LetterStatus.ABSENT));
        int[] remainingLetters = new int[ALPHABET_SIZE];

        for (int index = 0; index < WORD_LENGTH; index++) {
            char answerLetter = answer.charAt(index);

            if (answerLetter == guess.charAt(index)) {
                result.set(index, LetterStatus.EXACT);
            } else {
                remainingLetters[answerLetter - FIRST_LETTER]++;
            }
        }

        for (int index = 0; index < WORD_LENGTH; index++) {
            if (result.get(index) == LetterStatus.EXACT) {
                continue;
            }

            int letterIndex = guess.charAt(index) - FIRST_LETTER;

            if (remainingLetters[letterIndex] > 0) {
                result.set(index, LetterStatus.PRESENT);
                remainingLetters[letterIndex]--;
            }
        }

        return List.copyOf(result);
    }

    private static void validateWord(String word, String argumentName) {
        Objects.requireNonNull(word, argumentName + " не может быть null");

        if (!WORD_PATTERN.matcher(word).matches()) {
            throw new IllegalArgumentException(
                    argumentName + " должен содержать пять русских букв в нижнем регистре без ё");
        }
    }
}
