package academy.fiveletters.mandatory.mr3;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: меню и режим автопроверки. */
@DisplayName("MR3. Меню и детерминированный режим")
class MenuTest {

    @Test
    @Disabled("MR3: реализуй тест и удали эту строку")
    @DisplayName("Некорректный пункт меню не роняет программу")
    void invalidMenuChoiceDoesNotCrash() {
        fail("Тест не реализован");
    }

    @Test
    @Disabled("MR3: реализуй тест и удали эту строку")
    @DisplayName("Можно сыграть несколько партий подряд без перезапуска")
    void severalGamesInARow() {
        fail("Тест не реализован");
    }

    @Test
    @Disabled("MR3: реализуй тест и удали эту строку")
    @DisplayName("Детерминированный режим даёт предсказуемый вывод для автопроверки")
    void deterministicModeProducesPredictableOutput() {
        fail("Тест не реализован");
    }
}
