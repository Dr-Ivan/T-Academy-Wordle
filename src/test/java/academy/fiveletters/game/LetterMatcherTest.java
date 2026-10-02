package academy.fiveletters.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Сопоставление букв загаданного и введённого слова")
class LetterMatcherTest {

    private final LetterMatcher matcher = new LetterMatcher();

    @Test
    @DisplayName("Точное совпадение имеет приоритет перед другими вхождениями той же буквы")
    void exactMatchesTakePriorityOverEarlierMisplacedLetters() {
        var result = matcher.match("арбуз", "ааааа");

        assertThat(result)
                .containsExactly(
                        LetterStatus.EXACT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT);

        var lateMatch = matcher.match("книга", "ааааа");

        assertThat(lateMatch)
                .containsExactly(
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.EXACT);
    }

    @Test
    @DisplayName("После точных совпадений оставшиеся вхождения букв распределяются слева направо")
    void distributesRemainingOccurrencesFromLeftToRight() {
        var result = matcher.match("сорок", "крооо");

        assertThat(result)
                .containsExactly(
                        LetterStatus.PRESENT,
                        LetterStatus.PRESENT,
                        LetterStatus.PRESENT,
                        LetterStatus.EXACT,
                        LetterStatus.ABSENT);
    }

    @Test
    @DisplayName("Одно вхождение буквы в ответе не засчитывается для нескольких позиций ввода")
    void misplacedLetterCannotBeUsedTwice() {
        var result = matcher.match("арбуз", "ббабб");

        assertThat(result)
                .containsExactly(
                        LetterStatus.PRESENT,
                        LetterStatus.ABSENT,
                        LetterStatus.PRESENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT);
    }

    @Test
    @DisplayName("Список статусов букв нельзя изменить")
    void returnsUnmodifiableResult() {
        var result = matcher.match("озеро", "арбуз");

        assertThatThrownBy(() -> result.set(0, LetterStatus.EXACT)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("Последующее сопоставление не изменяет результат предыдущего")
    void callsDoNotShareMutableState() {
        var first = matcher.match("озеро", "озеро");
        var second = matcher.match("озеро", "банан");

        assertThat(first)
                .containsExactly(
                        LetterStatus.EXACT,
                        LetterStatus.EXACT,
                        LetterStatus.EXACT,
                        LetterStatus.EXACT,
                        LetterStatus.EXACT);
        assertThat(second)
                .containsExactly(
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "дом", "домики", "дом12", "ОЗЕРО", "озёро", "apple"})
    @DisplayName("Загаданное слово вне формата пяти русских строчных букв без ё отклоняется")
    void rejectsInvalidAnswer(String answer) {
        assertThatIllegalArgumentException().isThrownBy(() -> matcher.match(answer, "арбуз"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "дом", "домики", "дом12", "ОЗЕРО", "озёро", "до ма"})
    @DisplayName("Введённое слово вне формата пяти русских строчных букв без ё отклоняется")
    void rejectsInvalidGuess(String guess) {
        assertThatIllegalArgumentException().isThrownBy(() -> matcher.match("озеро", guess));
    }
}
