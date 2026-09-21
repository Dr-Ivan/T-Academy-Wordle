package academy.fiveletters;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import academy.fiveletters.support.CliRunner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Воспроизведение партии через командную строку")
class ReplayCliTest {

    private final WordDictionary dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
    private final String answer =
            new GameService().startGame(dictionary, 6, 42L).answer();

    @Test
    @DisplayName("Replay сообщает об ошибочном вводе и победе без запроса интерактивного ввода")
    void replaysWinWithoutReadingStandardInput() {
        var result = CliRunner.run("replay", "--seed", "42", "--guesses", "дом12", answer);

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains(
                        "Воспроизведение партии",
                        "Seed: 42",
                        "Шаг 1: \"дом12\"",
                        "Допустимы только русские буквы без ё.",
                        "✅✅✅✅✅ " + answer,
                        "Осталось попыток: 5",
                        "Победа! Слово угадано за 1 попыток")
                .doesNotContain("Введите слово:", "Партия прервана.");
    }

    @Test
    @DisplayName("Replay показывает ответ при поражении и сообщает об отклонении ввода после завершения партии")
    void replaysLossAndReportsTrailingInput() {
        String wrongGuess = dictionary.words().stream()
                .filter(word -> !word.equals(answer))
                .findFirst()
                .orElseThrow();

        var args = new ArrayList<>(List.of("replay", "--seed", "42", "--guesses"));
        args.addAll(Collections.nCopies(6, wrongGuess));
        args.add(answer);

        var result = CliRunner.run(args.toArray(String[]::new));

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains("Шаг 7: \"" + answer + "\"", "Партия уже завершена.", "Неудача. Загаданное слово: " + answer)
                .doesNotContain("Победа!");
    }

    @Test
    @DisplayName("Пустой сценарий replay сообщает о незавершённой партии и не раскрывает ответ")
    void emptyScenarioDoesNotRevealAnswer() {
        var result = CliRunner.run("replay", "--seed", "42", "--guesses");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout()).contains("Партия не завершена.").doesNotContain(answer, "Введите слово:", "Шаг 1:");
    }

    @Test
    @DisplayName("Одинаковые seed и сценарий replay дают одинаковый вывод")
    void replayOutputIsDeterministic() {
        var first = CliRunner.run("replay", "--seed", "42", "--guesses", answer);
        var second = CliRunner.run("replay", "--seed", "42", "--guesses", answer);

        assertThat(first.exitCode()).isZero();
        assertThat(second.exitCode()).isZero();
        assertThat(second.stdout()).isEqualTo(first.stdout());
    }

    @Test
    @DisplayName("Replay без параметра seed завершается с ошибкой аргументов и кодом 2")
    void missingSeedIsUsageError() {
        var result = CliRunner.run("replay", "--guesses", "арбуз");

        assertThat(result.exitCode()).isEqualTo(2);
        assertThat(result.stdout()).isEmpty();
        assertThat(result.stderr()).contains("Ошибка аргументов:");
    }

    @Test
    @DisplayName("Replay без параметра guesses завершается с ошибкой аргументов и кодом 2")
    void missingGuessesOptionIsUsageError() {
        var result = CliRunner.run("replay", "--seed", "42");

        assertThat(result.exitCode()).isEqualTo(2);
        assertThat(result.stdout()).isEmpty();
        assertThat(result.stderr()).contains("Ошибка аргументов:");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "9223372036854775808"})
    @DisplayName("Некорректный seed в режиме replay вызывает сообщение об ошибке и код завершения 2")
    void invalidSeedIsUsageError(String seed) {
        var result = CliRunner.run("replay", "--seed", seed, "--guesses", "арбуз");

        assertThat(result.exitCode()).isEqualTo(2);
        assertThat(result.stdout()).isEmpty();
        assertThat(result.stderr()).contains("Seed должен быть целым числом");
    }
}
