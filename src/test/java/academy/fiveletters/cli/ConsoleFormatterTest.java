package academy.fiveletters.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import academy.fiveletters.game.GameStatus;
import academy.fiveletters.game.GuessResult;
import academy.fiveletters.game.LetterStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Форматирование результатов игры для консоли")
class ConsoleFormatterTest {

    @Test
    @DisplayName("Результат попытки отображает статусы букв по порядку и введённое слово")
    void formatsAllLetterStatusesInOrder() {
        var text = new ConsoleFormatter().formatGuess(mixedResult());

        assertThat(text).isEqualTo("❌🟡✅❌🟡 арбуз");
        assertThat(text).doesNotContain("\u001B");
    }

    @Test
    @DisplayName("Режим NEVER сохраняет обычный формат без ANSI-последовательностей")
    void neverModeProducesPlainText() {
        var text = new ConsoleFormatter(ColorMode.NEVER).formatGuess(mixedResult());

        assertThat(text).isEqualTo("❌🟡✅❌🟡 арбуз");
        assertThat(text).doesNotContain("\u001B");
    }

    @Test
    @DisplayName("Цветной режим окрашивает каждую букву по статусу и сбрасывает цвет после неё")
    void colorsLettersAndResetsFormatting() {
        var text = new ConsoleFormatter(ColorMode.ALWAYS).formatGuess(mixedResult());

        assertThat(text)
                .isEqualTo("❌🟡✅❌🟡 "
                        + "\u001B[90mа\u001B[0m"
                        + "\u001B[33mр\u001B[0m"
                        + "\u001B[32mб\u001B[0m"
                        + "\u001B[90mу\u001B[0m"
                        + "\u001B[33mз\u001B[0m");
    }

    @Test
    @DisplayName("Удаление ANSI-последовательностей восстанавливает обычный текст результата")
    void coloredAndPlainModesContainSameText() {
        var result = mixedResult();
        String plain = new ConsoleFormatter(ColorMode.NEVER).formatGuess(result);
        String colored = new ConsoleFormatter(ColorMode.ALWAYS).formatGuess(result);

        String withoutColors = colored.replaceAll("\u001B\\[[0-9;]*m", "");

        assertThat(withoutColors).isEqualTo(plain);
    }

    @ParameterizedTest
    @CsvSource({
        "1, попытку",
        "2, попытки",
        "4, попытки",
        "5, попыток",
        "6, попыток",
        "11, попыток",
        "12, попыток",
        "14, попыток",
        "21, попытку",
        "22, попытки",
        "24, попытки",
        "25, попыток",
        "101, попытку",
        "111, попыток",
        "112, попыток"
    })
    @DisplayName("Сообщение победы использует правильную форму слова попытка")
    void formatsWinningAttemptCount(int attemptsUsed, String expectedWord) {
        String text = new ConsoleFormatter().formatOutcome(GameStatus.WIN, attemptsUsed, "озеро");

        assertThat(text).isEqualTo("Победа! Слово угадано за %d %s".formatted(attemptsUsed, expectedWord));
    }

    @Test
    @DisplayName("Сообщения поражения и незавершённой партии сохраняют прежний формат")
    void preservesOtherOutcomes() {
        var formatter = new ConsoleFormatter();

        assertThat(formatter.formatOutcome(GameStatus.LOSE, 6, "озеро")).isEqualTo("Неудача. Загаданное слово: озеро");

        assertThat(formatter.formatOutcome(GameStatus.IN_PROGRESS, 0, "озеро")).isEqualTo("Партия не завершена.");
    }

    @Test
    @DisplayName("Отрицательное число использованных попыток отклоняется при форматировании")
    void rejectsNegativeAttemptCount() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ConsoleFormatter().formatOutcome(GameStatus.WIN, -1, "озеро"));
    }

    private GuessResult.Accepted mixedResult() {
        return new GuessResult.Accepted(
                "арбуз",
                List.of(
                        LetterStatus.ABSENT,
                        LetterStatus.PRESENT,
                        LetterStatus.EXACT,
                        LetterStatus.ABSENT,
                        LetterStatus.PRESENT),
                GameStatus.IN_PROGRESS,
                5);
    }
}
