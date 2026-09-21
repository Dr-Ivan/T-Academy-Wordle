package academy.fiveletters;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.game.GameService;
import academy.fiveletters.support.CliRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Командная строка и запуск игры")
class MainTest {

    @Test
    @DisplayName("Запуск с seed скрывает ответ и корректно завершается при конце ввода")
    void startsSessionWithSpecifiedSeed() {
        var dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
        var expectedSession = new GameService().startGame(dictionary, 6, 42L);

        var result = CliRunner.run("--seed", "42");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains(
                        "Seed: 42",
                        "Угадайте слово из пяти букв за 6 попыток.",
                        "Введите слово:",
                        "Ввод завершён. Партия прервана.")
                .doesNotContain(expectedSession.answer(), "Победа!", "Неудача.", "демонстрация MR1");
    }

    @Test
    @DisplayName("Отдельные запуски с одинаковым seed дают одинаковый вывод")
    void sameSeedProducesSameOutputAcrossProcesses() {
        var first = CliRunner.run("--seed", "42");
        var second = CliRunner.run("--seed", "42");

        assertThat(first.exitCode()).isZero();
        assertThat(second.exitCode()).isZero();
        assertThat(second.stdout()).isEqualTo(first.stdout());
    }

    @Test
    @DisplayName("Автоматически выбранный seed позволяет повторить вывод первоначального запуска")
    void automaticallyChosenSeedCanBeReused() {
        var first = CliRunner.run();

        assertThat(first.exitCode()).isZero();

        String seed = first.stdout()
                .lines()
                .filter(line -> line.startsWith("Seed: "))
                .map(line -> line.substring("Seed: ".length()))
                .findFirst()
                .orElseThrow();

        var repeated = CliRunner.run("--seed", seed);

        assertThat(repeated.exitCode()).isZero();
        assertThat(repeated.stdout()).isEqualTo(first.stdout());
    }

    @Test
    @DisplayName("Параметр --help выводит справку без запуска сессии")
    void showsHelpWithoutStartingSession() {
        var result = CliRunner.run("--help");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout()).contains("Использование:", "--seed").doesNotContain("Seed: ", "Введите слово:");
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "1.5", "", "9223372036854775808"})
    @DisplayName("Некорректный seed вызывает сообщение об ошибке и завершение с кодом 2")
    void rejectsInvalidSeed(String seed) {
        var result = CliRunner.run("--seed", seed);

        assertThat(result.exitCode()).isEqualTo(2);
        assertThat(result.stdout()).isEmpty();
        assertThat(result.stderr()).contains("Ошибка аргументов:", "Seed должен быть целым числом");
    }

    @ParameterizedTest
    @ValueSource(strings = {"--seed", "--unknown", "42"})
    @DisplayName("Неполные или неизвестные аргументы вызывают справку и завершение с кодом 2")
    void rejectsIncompleteOrUnknownArguments(String argument) {
        var result = CliRunner.run(argument);

        assertThat(result.exitCode()).isEqualTo(2);
        assertThat(result.stdout()).isEmpty();
        assertThat(result.stderr()).contains("Ошибка аргументов:", "Использование:");
    }

    @Test
    @DisplayName("Лишние аргументы запуска отклоняются с кодом 2")
    void rejectsExtraArguments() {
        var result = CliRunner.run("--seed", "42", "extra");

        assertThat(result.exitCode()).isEqualTo(2);
        assertThat(result.stdout()).isEmpty();
        assertThat(result.stderr()).contains("Ошибка аргументов:");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "-9223372036854775808", "9223372036854775807"})
    @DisplayName("Отрицательный seed и границы long принимаются при запуске приложения")
    void acceptsNegativeAndBoundarySeeds(String seed) {
        var result = CliRunner.run("--seed", seed);

        assertThat(result.exitCode()).isZero();
        assertThat(result.stdout()).contains("Seed: " + seed);
        assertThat(result.stderr()).isEmpty();
    }

    @Test
    @DisplayName("Некорректный консольный ввод не расходует попытку перед победным ответом")
    void invalidInputDoesNotConsumeAttemptBeforeWin() {
        var dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
        var session = new GameService().startGame(dictionary, 6, 42L);
        String input = "дом12\n" + session.answer() + "\n";

        var result = CliRunner.runWithInput(input, "--seed", "42");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains(
                        "Допустимы только русские буквы без ё.",
                        "✅✅✅✅✅ " + session.answer(),
                        "Осталось попыток: 5",
                        "Победа! Слово угадано за 1 попыток")
                .doesNotContain("Неудача.", "Партия прервана.");
    }

    @Test
    @DisplayName("После победы приложение завершает игру без обработки оставшегося ввода")
    void doesNotProcessInputAfterWin() {
        var dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
        var session = new GameService().startGame(dictionary, 6, 42L);
        String input = session.answer() + "\nдом12\n";

        var result = CliRunner.runWithInput(input, "--seed", "42");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains("Победа!")
                .doesNotContain("Допустимы только русские буквы без ё.", "Партия прервана.");
    }
}
