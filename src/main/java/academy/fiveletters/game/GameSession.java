package academy.fiveletters.game;

import academy.fiveletters.dictionary.WordDictionary;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Состояние одной игровой партии. */
public final class GameSession {

    private final WordDictionary dictionary;
    private final String answer;
    private final int maxAttempts;
    private final List<String> attemptsHistory = new ArrayList<>();

    GameSession(WordDictionary dictionary, String answer, int maxAttempts) {
        this.dictionary = Objects.requireNonNull(dictionary, "Словарь не должен быть null");
        this.answer = Objects.requireNonNull(answer, "Ответ не должен быть null");

        if (!dictionary.contains(answer)) {
            throw new IllegalArgumentException("Загаданное слово должно присутствовать в словаре");
        }

        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("Количество попыток должно быть положительным");
        }

        this.maxAttempts = maxAttempts;
    }

    public String answer() {
        return answer;
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public int attemptsUsed() {
        return attemptsHistory.size();
    }

    public List<String> attemptsHistory() {
        return List.copyOf(attemptsHistory);
    }

    public GameStatus status() {
        if (!attemptsHistory.isEmpty() && answer.equals(attemptsHistory.getLast())) {
            return GameStatus.WIN;
        }

        if (attemptsUsed() >= maxAttempts) {
            return GameStatus.LOSE;
        }

        return GameStatus.IN_PROGRESS;
    }

    /**
     * Записывает слово в словарной форме: нижний регистр, без внешних пробелов.
     *
     * <p>Нормализация пользовательского ввода выполняется до вызова этого метода.
     */
    void recordGuess(String guess) {
        if (status() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("Партия уже завершена");
        }

        Objects.requireNonNull(guess, "Попытка не должна быть null");

        if (!dictionary.contains(guess)) {
            throw new IllegalArgumentException("Слово отсутствует в словаре: " + guess);
        }

        attemptsHistory.add(guess);
    }

    boolean containsWord(String word) {
        return dictionary.contains(word);
    }
}
