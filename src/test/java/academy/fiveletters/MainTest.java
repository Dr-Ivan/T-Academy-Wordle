package academy.fiveletters;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.game.GameService;
import academy.fiveletters.support.CliRunner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Командная строка и демонстрационный запуск MR1")
class MainTest {

    @Test
    @DisplayName("Запуск с seed выводит параметры новой сессии и ожидаемый ответ")
    void startsSessionWithSpecifiedSeed() {
        var dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
        var expectedSession = new GameService().startGame(dictionary, 6, 42L);

        var result = CliRunner.run("--seed", "42");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains(
                        "Seed: 42",
                        "Слов в словаре: " + dictionary.size(),
                        "Ответ (демонстрация MR1): " + expectedSession.answer(),
                        "Лимит попыток: 6",
                        "Использовано попыток: 0",
                        "Статус: IN_PROGRESS");
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
        assertThat(result.stdout()).contains("Использование:", "--seed").doesNotContain("Статус:");
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
}
