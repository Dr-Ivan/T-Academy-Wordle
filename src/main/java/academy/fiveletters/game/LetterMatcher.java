package academy.fiveletters.game;

import academy.fiveletters.word.WordRules;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Сравнивает ответ и попытку с учётом количества повторяющихся букв. */
public final class LetterMatcher {

    private static final char FIRST_LETTER = 'а';
    private static final int ALPHABET_SIZE = 'я' - FIRST_LETTER + 1;

    /**
     * Возвращает неизменяемый список из пяти статусов.
     *
     * <p>Оба аргумента должны состоять из пяти русских букв в нижнем регистре без ё.
     */
    public List<LetterStatus> match(String answer, String guess) {
        WordRules.requireCanonical(answer, "Ответ");
        WordRules.requireCanonical(guess, "Попытка");

        List<LetterStatus> result = new ArrayList<>(Collections.nCopies(WordRules.length(), LetterStatus.ABSENT));
        int[] remainingLetters = new int[ALPHABET_SIZE];

        for (int index = 0; index < WordRules.length(); index++) {
            char answerLetter = answer.charAt(index);

            if (answerLetter == guess.charAt(index)) {
                result.set(index, LetterStatus.EXACT);
            } else {
                remainingLetters[answerLetter - FIRST_LETTER]++;
            }
        }

        for (int index = 0; index < WordRules.length(); index++) {
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
}
