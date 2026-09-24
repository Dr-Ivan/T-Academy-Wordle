package academy.fiveletters;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.dictionary.DictionaryCatalog;
import academy.fiveletters.dictionary.DictionaryCatalogLoader;
import academy.fiveletters.game.GameService;
import academy.fiveletters.settings.Difficulty;
import academy.fiveletters.settings.GameSettings;
import academy.fiveletters.settings.WordCategory;
import academy.fiveletters.support.CliRunner;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Выбор и сохранение настроек через меню")
class SettingsCliTest {

    private static final int MAX_ATTEMPTS = 6;
    private static final long INITIAL_SEED = 42L;

    private final DictionaryCatalog catalog = new DictionaryCatalogLoader().load();
    private final GameService service = new GameService();

    @ParameterizedTest
    @CsvSource({
        "1, 3, STANDARD, ALL",
        "1, 4, STANDARD, NATURE",
        "1, 5, STANDARD, EVERYDAY",
        "2, 3, EASY, ALL",
        "2, 4, EASY, NATURE",
        "2, 5, EASY, EVERYDAY"
    })
    @DisplayName("Каждая комбинация настроек запускает партию с выбранным словарём")
    void startsGameWithSelectedSettings(
            String difficultyChoice, String categoryChoice, Difficulty difficulty, WordCategory category) {
        var settings = new GameSettings(difficulty, category);
        String answer = answerFor(settings, INITIAL_SEED);

        String input = "2\n" + difficultyChoice + "\n" + categoryChoice + "\n0\n1\n" + answer + "\n0\n";

        var result = runMenu(input);

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains(
                        "Сложность: " + difficulty.title(),
                        "Категория: " + category.title(),
                        "Seed: " + INITIAL_SEED,
                        "✅✅✅✅✅ " + answer,
                        "Победа!",
                        "До свидания!")
                .doesNotContain("Неизвестный пункт", "Партия прервана.", "Такого слова нет в словаре.");
    }

    @Test
    @DisplayName("Настройки сохраняются между партиями, а их просмотр не расходует seed")
    void preservesSettingsAndSeedSequence() {
        var settings = new GameSettings(Difficulty.EASY, WordCategory.NATURE);
        long secondSeed = new Random(INITIAL_SEED).nextLong();

        String firstAnswer = answerFor(settings, INITIAL_SEED);
        String secondAnswer = answerFor(settings, secondSeed);

        String input = "2\n2\n4\n0\n1\n" + firstAnswer + "\n2\n0\n1\n" + secondAnswer + "\n0\n";

        var result = runMenu(input);

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains("✅✅✅✅✅ " + firstAnswer, "✅✅✅✅✅ " + secondAnswer, "До свидания!")
                .doesNotContain("Неизвестный пункт", "Партия прервана.", "Такого слова нет в словаре.");

        assertThat(result.stdout()
                        .lines()
                        .filter(line -> line.startsWith("Seed: "))
                        .toList())
                .containsExactly("Seed: " + INITIAL_SEED, "Seed: " + secondSeed);

        assertThat(result.stdout()
                        .lines()
                        .filter(line -> line.startsWith("Победа!"))
                        .count())
                .isEqualTo(2L);
    }

    @Test
    @DisplayName("Ошибочный ввод в настройках сохраняет выбранные значения")
    void invalidChoicePreservesSettings() {
        var settings = new GameSettings(Difficulty.EASY, WordCategory.NATURE);
        String answer = answerFor(settings, INITIAL_SEED);
        String input = "2\n2\n4\nошибка\n\n99\n0\n1\n" + answer + "\n0\n";

        var result = runMenu(input);

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains("✅✅✅✅✅ " + answer, "Победа!", "До свидания!")
                .doesNotContain("Партия прервана.", "Такого слова нет в словаре.");

        assertThat(result.stdout()
                        .lines()
                        .filter(line -> line.equals("Неизвестный пункт настроек. Введите число от 0 до 7."))
                        .count())
                .isEqualTo(3L);
    }

    @Test
    @DisplayName("Слово вне выбранной категории отклоняется без расходования попытки")
    void rejectsWordOutsideSelectedDictionary() {
        var settings = new GameSettings(Difficulty.EASY, WordCategory.NATURE);
        String answer = answerFor(settings, INITIAL_SEED);

        var result = runMenu("2\n2\n4\n0\n1\nдиван\n" + answer + "\n0\n");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains(
                        "Такого слова нет в словаре.",
                        "✅✅✅✅✅ " + answer,
                        "Осталось попыток: 5",
                        "Победа!",
                        "До свидания!")
                .doesNotContain("Партия прервана.", "Неудача.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"2\n", "2\n2\n4\n"})
    @DisplayName("Конец ввода в настройках завершает приложение без запуска партии")
    void endOfInputInSettingsClosesApplication(String input) {
        var result = runMenu(input);

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains("Настройки", "Ввод завершён. Программа закрыта.")
                .doesNotContain("Seed: ", "Введите слово:", "Партия прервана.");

        assertThat(result.stdout()
                        .lines()
                        .filter(line -> line.equals("Главное меню"))
                        .count())
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("Одна партия и replay используют одинаковый выбранный словарь и правила подсказки")
    void directGameAndReplayUseSelectedDictionary() {
        var settings = new GameSettings(Difficulty.EASY, WordCategory.NATURE);
        String answer = answerFor(settings, INITIAL_SEED);
        String seed = Long.toString(INITIAL_SEED);

        var direct = CliRunner.runWithInput(
                ":hint\nдиван\n" + answer + "\n", "--seed", seed, "--difficulty", "easy", "--category", "nature");

        var replay = CliRunner.run(
                "replay",
                "--category",
                "nature",
                "--difficulty",
                "easy",
                "--seed",
                seed,
                "--guesses",
                ":hint",
                "диван",
                answer);

        for (var result : java.util.List.of(direct, replay)) {
            assertThat(result.exitCode()).isZero();
            assertThat(result.stderr()).isEmpty();
            assertThat(result.stdout())
                    .contains(
                            "Сложность: Лёгкая",
                            "Категория: Природа",
                            "Подсказка: на позиции 1 находится буква \"%s\".".formatted(answer.charAt(0)),
                            "Такого слова нет в словаре.",
                            "✅✅✅✅✅ " + answer,
                            "Осталось попыток: 5",
                            "Победа!")
                    .doesNotContain("Партия прервана.", "Неудача.");
        }
    }

    private String answerFor(GameSettings settings, long seed) {
        return service.startGame(catalog.select(settings), MAX_ATTEMPTS, seed).answer();
    }

    private CliRunner.Result runMenu(String input) {
        return CliRunner.runWithInput(input, "menu", "--seed", Long.toString(INITIAL_SEED));
    }
}
