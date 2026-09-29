package academy.fiveletters;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.game.GameService;
import academy.fiveletters.support.CliRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Подсказка в интерактивной игре и replay")
class HintCliTest {

    private static final int MAX_ATTEMPTS = 6;
    private static final long SEED = 42L;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Оба режима показывают одну подсказку, отклоняют повтор и сохраняют попытку")
    void hintWorksInBothModes(boolean replay) {
        var dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
        String answer =
                new GameService().startGame(dictionary, MAX_ATTEMPTS, SEED).answer();

        CliRunner.Result result;
        if (replay) {
            result = CliRunner.run("replay", "--seed", Long.toString(SEED), "--guesses", ":hint", ":hint", answer);
        } else {
            result = CliRunner.runWithInput(":hint\n:hint\n" + answer + "\n", "--seed", Long.toString(SEED));
        }

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains(
                        "Подсказка: на позиции 1 находится буква \"%s\".".formatted(answer.charAt(0)),
                        "Подсказка уже использована.",
                        "✅✅✅✅✅ " + answer,
                        "Осталось попыток: 5",
                        "Победа!")
                .doesNotContain("Партия прервана.", "Введите слово из пяти букв.");

        assertThat(result.stdout()
                        .lines()
                        .filter(line -> line.startsWith("Подсказка:"))
                        .count())
                .isEqualTo(1L);
    }
}
