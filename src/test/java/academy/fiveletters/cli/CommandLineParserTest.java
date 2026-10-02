package academy.fiveletters.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import academy.fiveletters.settings.Difficulty;
import academy.fiveletters.settings.GameSettings;
import academy.fiveletters.settings.WordCategory;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Разбор режимов и настроек командной строки")
class CommandLineParserTest {

    private final CommandLineParser parser = new CommandLineParser();

    @Test
    @DisplayName("Прежняя команда одной партии использует настройки по умолчанию")
    void preservesDefaultSettings() {
        var result = parser.parse(new String[] {"--seed", "42"});

        assertThat(result).isEqualTo(new LaunchOptions.Play(42L, GameSettings.DEFAULT, ColorMode.NEVER));
    }

    @Test
    @DisplayName("Порядок параметров и регистр значений настроек не влияют на результат")
    void acceptsReorderedOptionsAndMixedCaseValues() {
        var result = parser.parse(new String[] {"--category", "NaTuRe", "--seed", "-42", "--difficulty", "EaSy"});

        assertThat(result)
                .isEqualTo(new LaunchOptions.Play(
                        -42L, new GameSettings(Difficulty.EASY, WordCategory.NATURE), ColorMode.NEVER));
    }

    @Test
    @DisplayName("Меню получает переданные настройки и seed")
    void parsesMenuSettings() {
        var result =
                parser.parse(new String[] {"menu", "--difficulty", "easy", "--seed", "42", "--category", "everyday"});

        assertThat(result)
                .isEqualTo(new LaunchOptions.Menu(
                        42L, new GameSettings(Difficulty.EASY, WordCategory.EVERYDAY), ColorMode.NEVER));
    }

    @Test
    @DisplayName("Меню без seed принимает настройки и выбирает seed автоматически")
    void menuDoesNotRequireSeed() {
        var result = parser.parse(new String[] {"menu", "--category", "nature"});

        assertThat(result)
                .isInstanceOfSatisfying(
                        LaunchOptions.Menu.class,
                        menu -> assertThat(menu.settings())
                                .isEqualTo(new GameSettings(Difficulty.STANDARD, WordCategory.NATURE)));
    }

    @Test
    @DisplayName("Replay сохраняет все строки после guesses без разбора как параметров")
    void preservesReplayInputVerbatim() {
        var result = parser.parse(new String[] {
            "replay",
            "--category",
            "nature",
            "--seed",
            "42",
            "--difficulty",
            "easy",
            "--guesses",
            ":hint",
            " АрБуЗ ",
            "--seed",
            "-7"
        });

        assertThat(result)
                .isEqualTo(new LaunchOptions.Replay(
                        42L,
                        new GameSettings(Difficulty.EASY, WordCategory.NATURE),
                        ColorMode.NEVER,
                        List.of(":hint", " АрБуЗ ", "--seed", "-7")));
    }

    @Test
    @DisplayName("Replay разрешает пустой сценарий при наличии guesses")
    void acceptsEmptyReplay() {
        var result = parser.parse(new String[] {"replay", "--seed", "42", "--guesses"});

        assertThat(result).isEqualTo(new LaunchOptions.Replay(42L, GameSettings.DEFAULT, ColorMode.NEVER, List.of()));
    }

    @ParameterizedTest
    @MethodSource("invalidArguments")
    @DisplayName("Неполные, неизвестные и повторные параметры отклоняются")
    void rejectsInvalidArguments(List<String> arguments) {
        assertThatIllegalArgumentException().isThrownBy(() -> parser.parse(arguments.toArray(String[]::new)));
    }

    @Test
    @DisplayName("Цветной режим разбирается независимо от игровых настроек")
    void parsesColorMode() {
        var result = parser.parse(new String[] {"--color", "AlWaYs", "--seed", "42"});

        assertThat(result).isEqualTo(new LaunchOptions.Play(42L, GameSettings.DEFAULT, ColorMode.ALWAYS));
    }

    private static Stream<List<String>> invalidArguments() {
        return Stream.of(
                List.of("--seed"),
                List.of("--seed", "42", "--difficulty"),
                List.of("--seed", "42", "--category", "--difficulty", "easy"),
                List.of("--seed", "42", "--unknown", "value"),
                List.of("--seed", "42", "--seed", "43"),
                List.of("--seed", "42", "--difficulty", "easy", "--difficulty", "standard"),
                List.of("--seed", "42", "--category", "nature", "--category", "all"),
                List.of("--seed", "42", "--difficulty", "hard"),
                List.of("--seed", "42", "--category", "unknown"),
                List.of("--difficulty", "easy"),
                List.of("--seed", "42", "--guesses"),
                List.of("replay", "--seed", "42"),
                List.of("replay", "--guesses", "арбуз"),
                List.of("menu", "--seed", "42", "extra"),
                List.of("--seed", "42", "--color"),
                List.of("--seed", "42", "--color", "auto"),
                List.of("--seed", "42", "--color", "always", "--color", "never"));
    }
}
