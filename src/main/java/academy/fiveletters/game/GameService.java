package academy.fiveletters.game;

import academy.fiveletters.dictionary.WordDictionary;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;

/** Создаёт игровые партии и обрабатывает попытки. */
public final class GameService {

    private static final int MINIMUM_DICTIONARY_SIZE = 50;
    private static final int WORD_LENGTH = 5;
    private static final Pattern WORD_PATTERN = Pattern.compile("[а-я]{" + WORD_LENGTH + "}");

    private final LetterMatcher letterMatcher = new LetterMatcher();

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

    public GuessResult applyGuess(GameSession session, String guess) {
        Objects.requireNonNull(session, "Сессия не должна быть null");
        Objects.requireNonNull(guess, "Попытка не должна быть null");

        if (session.status() != GameStatus.IN_PROGRESS) {
            return new GuessResult.Rejected(GuessRejectionReason.GAME_FINISHED);
        }

        String normalizedGuess = guess.strip().toLowerCase(Locale.ROOT);

        if (normalizedGuess.length() != WORD_LENGTH) {
            return new GuessResult.Rejected(GuessRejectionReason.INVALID_LENGTH);
        }

        if (!WORD_PATTERN.matcher(normalizedGuess).matches()) {
            return new GuessResult.Rejected(GuessRejectionReason.INVALID_CHARACTERS);
        }

        if (!session.containsWord(normalizedGuess)) {
            return new GuessResult.Rejected(GuessRejectionReason.WORD_NOT_IN_DICTIONARY);
        }

        List<LetterStatus> letters = letterMatcher.match(session.answer(), normalizedGuess);
        session.recordGuess(normalizedGuess);

        return new GuessResult.Accepted(
                normalizedGuess, letters, session.status(), session.maxAttempts() - session.attemptsUsed());
    }
}
