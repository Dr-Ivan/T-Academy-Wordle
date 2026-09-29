package academy.fiveletters;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.game.GameService;
import academy.fiveletters.support.CliRunner;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Статистика в консольном приложении")
class StatisticsCliTest {

    private static final int MAX_ATTEMPTS = 6;
    private static final long SEED = 42L;

    @Test
    @DisplayName("Просмотр статистики не расходует seed и не меняет итог двух партий")
    void menuShowsCombinedStatistics() {
        var dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
        var service = new GameService();

        String firstAnswer = service.startGame(dictionary, MAX_ATTEMPTS, SEED).answer();
        long secondSeed = new Random(SEED).nextLong();
        String secondAnswer =
                service.startGame(dictionary, MAX_ATTEMPTS, secondSeed).answer();

        String wrongGuess = dictionary.words().stream()
                .filter(word -> !word.equals(secondAnswer))
                .findFirst()
                .orElseThrow();

        String input = "3\n1\n:hint\n" + firstAnswer + "\n3\n1\n" + (wrongGuess + "\n").repeat(MAX_ATTEMPTS) + "3\n0\n";

        var result = CliRunner.runWithInput(input, "menu", "--seed", Long.toString(SEED));

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains("Победа!", "Неудача.", "До свидания!")
                .doesNotContain("Неизвестный пункт", "Партия прервана.");

        String summary = result.stdout().substring(result.stdout().lastIndexOf("Статистика за запуск"));

        assertThat(summary)
                .contains(
                        "Начато партий: 2",
                        "Побед: 1",
                        "Поражений: 1",
                        "Прервано партий: 0",
                        "Партий с подсказкой: 1",
                        "Процент побед: 50.0%",
                        "Среднее число попыток при победе: 1.0");
    }

    @Test
    @DisplayName("Конец ввода во время игры учитывается как прерывание, а не поражение")
    void endOfInputCountsAsInterruption() {
        var result = CliRunner.runWithInput("1\n:hint\n", "menu", "--seed", Long.toString(SEED));

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains(
                        "Партия прервана.",
                        "Начато партий: 1",
                        "Побед: 0",
                        "Поражений: 0",
                        "Прервано партий: 1",
                        "Партий с подсказкой: 1",
                        "Процент побед: 0.0%",
                        "Среднее число попыток при победе: 0.0");
    }

    @Test
    @DisplayName("Новый запуск программы начинает статистику с нуля")
    void statisticsDoesNotPersistAcrossProcesses() {
        var first = CliRunner.runWithInput("1\n", "menu", "--seed", Long.toString(SEED));
        var second = CliRunner.runWithInput("3\n0\n");

        assertThat(first.exitCode()).isZero();
        assertThat(second.exitCode()).isZero();
        assertThat(second.stderr()).isEmpty();
        assertThat(first.stdout()).contains("Начато партий: 1");
        assertThat(second.stdout()).contains("Начато партий: 0", "Прервано партий: 0");
    }

    @Test
    @DisplayName("Replay не выводит статистику игрока")
    void replayDoesNotParticipateInPlayerStatistics() {
        var result = CliRunner.run("replay", "--seed", Long.toString(SEED), "--guesses", ":hint");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout()).contains("Воспроизведение партии").doesNotContain("Статистика за запуск");
    }
}
