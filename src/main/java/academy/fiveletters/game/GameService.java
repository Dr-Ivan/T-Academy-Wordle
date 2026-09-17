package academy.fiveletters.game;

import academy.fiveletters.dictionary.WordDictionary;
import java.util.Objects;
import java.util.Random;

/** Создаёт игровые партии. */
public final class GameService {

    private static final int MINIMUM_DICTIONARY_SIZE = 50;

    /**
     * Создаёт новую партию.
     *
     * <p>Одинаковый набор слов и одинаковый seed дают одинаковый ответ.
     */
    public GameSession startGame(WordDictionary dictionary, int maxAttempts, long seed) {
        Objects.requireNonNull(dictionary, "Словарь не должен быть null");

        if (dictionary.size() < MINIMUM_DICTIONARY_SIZE) {
            throw new IllegalArgumentException("Для игры требуется минимум %d уникальных слов, получено: %d"
                    .formatted(MINIMUM_DICTIONARY_SIZE, dictionary.size()));
        }

        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("Количество попыток должно быть положительным");
        }

        Random random = new Random(seed);
        int answerIndex = random.nextInt(dictionary.size());
        String answer = dictionary.words().get(answerIndex);

        return new GameSession(dictionary, answer, maxAttempts);
    }
}
