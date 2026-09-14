package academy.fiveletters.mandatory.mr1;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: одинаковый seed обязан давать одинаковое загаданное слово. */
@DisplayName("MR1. Воспроизводимость по seed")
class SeedTest {

    @Test
    @Disabled("MR1: реализуй тест и удали эту строку")
    @DisplayName("Одинаковый seed даёт одинаковое загаданное слово")
    void sameSeedProducesSameAnswer() {
        fail("Тест не реализован");
    }

    @Test
    @Disabled("MR1: реализуй тест и удали эту строку")
    @DisplayName("Разные seed'ы дают разные слова хотя бы иногда")
    void differentSeedsProduceDifferentAnswers() {
        fail("Тест не реализован");
    }
}
