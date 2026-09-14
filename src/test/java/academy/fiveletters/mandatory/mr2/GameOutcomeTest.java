package academy.fiveletters.mandatory.mr2;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: завершение партии. */
@DisplayName("MR2. Победа и поражение")
class GameOutcomeTest {

    @Test
    @Disabled("MR2: реализуй тест и удали эту строку")
    @DisplayName("Угаданное слово переводит сессию в статус WIN")
    void correctGuessWinsTheGame() {
        fail("Тест не реализован");
    }

    @Test
    @Disabled("MR2: реализуй тест и удали эту строку")
    @DisplayName("После 6 неудачных попыток сессия переходит в статус LOSE")
    void sixFailedAttemptsLoseTheGame() {
        fail("Тест не реализован");
    }

    @Test
    @Disabled("MR2: реализуй тест и удали эту строку")
    @DisplayName("При поражении показывается загаданное слово")
    void answerIsRevealedOnLoss() {
        fail("Тест не реализован");
    }

    @Test
    @Disabled("MR2: реализуй тест и удали эту строку")
    @DisplayName("Завершённая партия больше не принимает попытки")
    void finishedGameRejectsFurtherGuesses() {
        fail("Тест не реализован");
    }
}
