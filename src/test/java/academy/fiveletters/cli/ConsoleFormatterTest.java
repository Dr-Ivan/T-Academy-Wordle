package academy.fiveletters.cli;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.game.GameStatus;
import academy.fiveletters.game.GuessResult;
import academy.fiveletters.game.LetterStatus;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Форматирование результатов игры для консоли")
class ConsoleFormatterTest {

    @Test
    @DisplayName("Результат попытки отображает статусы букв по порядку и введённое слово")
    void formatsAllLetterStatusesInOrder() {
        var result = new GuessResult.Accepted(
                "арбуз",
                List.of(
                        LetterStatus.ABSENT,
                        LetterStatus.PRESENT,
                        LetterStatus.EXACT,
                        LetterStatus.ABSENT,
                        LetterStatus.PRESENT),
                GameStatus.IN_PROGRESS,
                5);

        var text = new ConsoleFormatter().formatGuess(result);

        assertThat(text).isEqualTo("❌🟡✅❌🟡 арбуз");
    }
}
