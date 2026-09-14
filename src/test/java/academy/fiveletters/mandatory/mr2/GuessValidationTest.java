package academy.fiveletters.mandatory.mr2;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Обязательные тесты: валидация ввода. */
@DisplayName("MR2. Валидация ввода")
class GuessValidationTest {

    @ParameterizedTest
    @ValueSource(strings = {"дом", "домики", ""})
    @Disabled("MR2: реализуй тест и удали эту строку")
    @DisplayName("Слово не из 5 букв отклоняется: \"{0}\"")
    void wordOfWrongLengthIsRejected(String guess) {
        fail("Тест не реализован");
    }

    @ParameterizedTest
    @ValueSource(strings = {"дом12", "дом!!", "до ма"})
    @Disabled("MR2: реализуй тест и удали эту строку")
    @DisplayName("Ввод с не-буквами отклоняется: \"{0}\"")
    void nonLetterInputIsRejected(String guess) {
        fail("Тест не реализован");
    }

    @Test
    @Disabled("MR2: реализуй тест и удали эту строку")
    @DisplayName("Слово, которого нет в словаре, отклоняется")
    void wordOutsideDictionaryIsRejected() {
        fail("Тест не реализован");
    }

    @Test
    @Disabled("MR2: реализуй тест и удали эту строку")
    @DisplayName("Некорректный ввод не тратит попытку")
    void invalidInputDoesNotConsumeAttempt() {
        fail("Тест не реализован");
    }

    @Test
    @Disabled("MR2: реализуй тест и удали эту строку")
    @DisplayName("Ввод не зависит от регистра: \"ОЗЕРО\" и \"озеро\" обрабатываются одинаково")
    void inputIsCaseInsensitive() {
        fail("Тест не реализован");
    }
}
