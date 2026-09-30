package academy.fiveletters.mandatory.mr2;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.game.LetterMatcher;
import academy.fiveletters.game.LetterStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: раскраска букв. */
@DisplayName("MR2. Проверка букв")
class LetterMatchingTest {

    private final LetterMatcher matcher = new LetterMatcher();

    @Test
    @DisplayName("Базовый случай: загадано \"озеро\", ввод \"арбуз\" -> ❌🟡❌❌🟡")
    void basicCase() {
        var result = matcher.match("озеро", "арбуз");

        assertThat(result)
                .containsExactly(
                        LetterStatus.ABSENT,
                        LetterStatus.PRESENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.PRESENT);
    }

    @Test
    @DisplayName("Полное совпадение: загадано \"озеро\", ввод \"озеро\" -> ✅✅✅✅✅")
    void exactMatch() {
        var result = matcher.match("озеро", "озеро");

        assertThat(result)
                .containsExactly(
                        LetterStatus.EXACT,
                        LetterStatus.EXACT,
                        LetterStatus.EXACT,
                        LetterStatus.EXACT,
                        LetterStatus.EXACT);
    }

    @Test
    @DisplayName("Повторяющиеся буквы: загадано \"сорок\", ввод \"оооом\" -> ❌✅❌✅❌")
    void repeatedLettersAreNotDoubleCounted() {
        var result = matcher.match("сорок", "оооом");

        assertThat(result)
                .containsExactly(
                        LetterStatus.ABSENT,
                        LetterStatus.EXACT,
                        LetterStatus.ABSENT,
                        LetterStatus.EXACT,
                        LetterStatus.ABSENT);
    }

    @Test
    @DisplayName("Ни одна буква не подошла: все позиции ❌")
    void noMatchingLetters() {
        var result = matcher.match("озеро", "банан");

        assertThat(result)
                .containsExactly(
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT);
    }
}
