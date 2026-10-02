package academy.fiveletters.mandatory.mr3;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import academy.fiveletters.support.CliRunner;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: меню и режим автопроверки. */
@DisplayName("MR3. Меню и детерминированный режим")
class MenuTest {

    private static final int MAX_ATTEMPTS = 6;
    private static final long INITIAL_SEED = 42L;

    private final WordDictionary dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
    private final GameService service = new GameService();

    @Test
    @DisplayName("Некорректный пункт меню не роняет программу")
    void invalidMenuChoiceDoesNotCrash() {
        String answer = answerFor(INITIAL_SEED);
        String input = "неизвестно\n\n99\n1\n" + answer + "\n0\n";

        var result = CliRunner.runWithInput(input, "menu", "--seed", Long.toString(INITIAL_SEED));

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains(
                        "Неизвестный пункт меню. Введите 1, 2, 3 или 0.",
                        "Seed: 42",
                        "✅✅✅✅✅ " + answer,
                        "Победа!",
                        "До свидания!")
                .doesNotContain("Неудача.", "Партия прервана.");

        assertThat(result.stdout()
                        .lines()
                        .filter(line -> line.equals("Неизвестный пункт меню. Введите 1, 2, 3 или 0."))
                        .count())
                .isEqualTo(3L);
    }

    @Test
    @DisplayName("Можно сыграть несколько партий подряд без перезапуска")
    void severalGamesInARow() {
        long secondSeed = new Random(INITIAL_SEED).nextLong();
        String firstAnswer = answerFor(INITIAL_SEED);
        String secondAnswer = answerFor(secondSeed);
        String input = "1\n" + firstAnswer + "\n1\n" + secondAnswer + "\n0\n";

        var result = CliRunner.runWithInput(input, "menu", "--seed", Long.toString(INITIAL_SEED));

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains(
                        "Seed: " + INITIAL_SEED,
                        "Seed: " + secondSeed,
                        "✅✅✅✅✅ " + firstAnswer,
                        "✅✅✅✅✅ " + secondAnswer,
                        "До свидания!")
                .doesNotContain("Неизвестный пункт меню", "Неудача.", "Партия прервана.");

        assertThat(result.stdout()
                        .lines()
                        .filter(line -> line.startsWith("Победа!"))
                        .count())
                .isEqualTo(2L);

        assertThat(result.stdout()
                        .lines()
                        .filter(line -> line.equals("Главное меню"))
                        .count())
                .isEqualTo(3L);
    }

    @Test
    @DisplayName("Детерминированный режим даёт предсказуемый вывод для автопроверки")
    void deterministicModeProducesPredictableOutput() {
        String answer = answerFor(INITIAL_SEED);
        String input = "1\nдом12\n" + answer + "\n0\n";

        var first = CliRunner.runWithInput(input, "menu", "--seed", Long.toString(INITIAL_SEED));
        var second = CliRunner.runWithInput(input, "menu", "--seed", Long.toString(INITIAL_SEED));

        assertThat(first.exitCode()).isZero();
        assertThat(second.exitCode()).isZero();
        assertThat(first.stderr()).isEmpty();
        assertThat(second.stderr()).isEmpty();
        assertThat(first.stdout())
                .contains(
                        "Допустимы только русские буквы без ё.",
                        "✅✅✅✅✅ " + answer,
                        "Осталось попыток: 5",
                        "Победа!",
                        "До свидания!");
        assertThat(second.stdout()).isEqualTo(first.stdout());
    }

    private String answerFor(long seed) {
        return service.startGame(dictionary, MAX_ATTEMPTS, seed).answer();
    }
}
