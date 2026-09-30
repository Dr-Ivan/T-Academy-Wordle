package academy.fiveletters;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import academy.fiveletters.support.CliRunner;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Управление цветом через CLI и меню")
class ColorCliTest {

    private static final int MAX_ATTEMPTS = 6;
    private static final long SEED = 42L;

    private final WordDictionary dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
    private final GameService service = new GameService();

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Цвет меняет только оформление обычной партии и replay")
    void colorDoesNotChangeGameOutput(boolean replay) {
        String answer = answerFor(SEED);

        var plain = runScenario(replay, "never", answer);
        var colored = runScenario(replay, "always", answer);

        assertThat(plain.exitCode()).isZero();
        assertThat(colored.exitCode()).isZero();
        assertThat(plain.stderr()).isEmpty();
        assertThat(colored.stderr()).isEmpty();

        assertThat(plain.stdout()).contains("Победа!").doesNotContain("\u001B");
        assertThat(colored.stdout()).contains("\u001B[32m", "\u001B[0m");

        assertThat(stripColors(colored.stdout())).isEqualTo(plain.stdout());
    }

    @Test
    @DisplayName("Меню включает и выключает цвет между партиями без изменения seed")
    void menuCanChangeColorBetweenGames() {
        String firstAnswer = answerFor(SEED);
        long secondSeed = new Random(SEED).nextLong();
        String secondAnswer = answerFor(secondSeed);

        String input = "2\n6\n0\n1\n" + firstAnswer + "\n2\n7\n0\n1\n" + secondAnswer + "\n0\n";

        var result = CliRunner.runWithInput(input, "menu", "--seed", Long.toString(SEED));

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains("Цвет: Включён", "Цвет: Выключен", "Побед: 2")
                .doesNotContain("Неизвестный пункт", "Партия прервана.");

        var feedback = result.stdout()
                .lines()
                .filter(line -> line.startsWith("✅✅✅✅✅ "))
                .toList();

        assertThat(feedback).hasSize(2);
        assertThat(feedback.get(0)).contains("\u001B[32m");
        assertThat(stripColors(feedback.get(0))).isEqualTo("✅✅✅✅✅ " + firstAnswer);
        assertThat(feedback.get(1)).isEqualTo("✅✅✅✅✅ " + secondAnswer);

        assertThat(result.stdout()
                        .lines()
                        .filter(line -> line.startsWith("Seed: "))
                        .toList())
                .containsExactly("Seed: " + SEED, "Seed: " + secondSeed);
    }

    @Test
    @DisplayName("Меню использует цветной режим из аргументов запуска")
    void menuUsesInitialColorMode() {
        String answer = answerFor(SEED);

        var result = CliRunner.runWithInput(
                "1\n" + answer + "\n0\n", "menu", "--seed", Long.toString(SEED), "--color", "always");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout()).contains("Цвет: Включён", "\u001B[32m", "Победа!");
    }

    @Test
    @DisplayName("Без параметра color приложение не выводит ANSI-последовательности")
    void defaultModeHasNoAnsiCodes() {
        String answer = answerFor(SEED);

        var result = CliRunner.runWithInput(answer + "\n", "--seed", Long.toString(SEED));

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout()).contains("✅✅✅✅✅ " + answer).doesNotContain("\u001B");
    }

    private CliRunner.Result runScenario(boolean replay, String color, String answer) {
        if (replay) {
            return CliRunner.run(
                    "replay", "--seed", Long.toString(SEED), "--color", color, "--guesses", ":hint", "дом12", answer);
        }

        return CliRunner.runWithInput(
                ":hint\nдом12\n" + answer + "\n", "--seed", Long.toString(SEED), "--color", color);
    }

    private String answerFor(long seed) {
        return service.startGame(dictionary, MAX_ATTEMPTS, seed).answer();
    }

    private String stripColors(String text) {
        return text.replaceAll("\u001B\\[[0-9;]*m", "");
    }
}
