package academy.fiveletters.mandatory.mr2;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GuessRejectionReason;
import academy.fiveletters.game.GuessResult;
import java.util.Locale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Обязательные тесты: валидация ввода. */
@DisplayName("MR2. Валидация ввода")
class GuessValidationTest {

    private final WordDictionary dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
    private final GameService service = new GameService();

    @ParameterizedTest
    @ValueSource(strings = {"дом", "домики", ""})
    @DisplayName("Слово не из 5 букв отклоняется: \"{0}\"")
    void wordOfWrongLengthIsRejected(String guess) {
        var session = service.startGame(dictionary, 6, 42L);

        var result = service.applyGuess(session, guess);

        assertThat(result).isEqualTo(new GuessResult.Rejected(GuessRejectionReason.INVALID_LENGTH));
    }

    @ParameterizedTest
    @ValueSource(strings = {"дом12", "дом!!", "до ма"})
    @DisplayName("Ввод с не-буквами отклоняется: \"{0}\"")
    void nonLetterInputIsRejected(String guess) {
        var session = service.startGame(dictionary, 6, 42L);

        var result = service.applyGuess(session, guess);

        assertThat(result).isEqualTo(new GuessResult.Rejected(GuessRejectionReason.INVALID_CHARACTERS));
    }

    @Test
    @DisplayName("Слово, которого нет в словаре, отклоняется")
    void wordOutsideDictionaryIsRejected() {
        var session = service.startGame(dictionary, 6, 42L);
        String guess = "ааааа";
        assertThat(dictionary.contains(guess)).isFalse();

        var result = service.applyGuess(session, guess);

        assertThat(result).isEqualTo(new GuessResult.Rejected(GuessRejectionReason.WORD_NOT_IN_DICTIONARY));
    }

    @Test
    @DisplayName("Некорректный ввод не тратит попытку")
    void invalidInputDoesNotConsumeAttempt() {
        var session = service.startGame(dictionary, 6, 42L);
        String validGuess = dictionary.words().stream()
                .filter(word -> !word.equals(session.answer()))
                .findFirst()
                .orElseThrow();

        assertThat(service.applyGuess(session, validGuess)).isInstanceOf(GuessResult.Accepted.class);

        var historyBefore = session.attemptsHistory();
        int attemptsBefore = session.attemptsUsed();
        var statusBefore = session.status();

        for (String guess : new String[] {"дом", "дом12", "ааааа"}) {
            assertThat(service.applyGuess(session, guess)).isInstanceOf(GuessResult.Rejected.class);
            assertThat(session.attemptsHistory()).isEqualTo(historyBefore);
            assertThat(session.attemptsUsed()).isEqualTo(attemptsBefore);
            assertThat(session.status()).isEqualTo(statusBefore);
        }
    }

    @Test
    @DisplayName("Ввод не зависит от регистра: \"ОЗЕРО\" и \"озеро\" обрабатываются одинаково")
    void inputIsCaseInsensitive() {
        var lowerCaseSession = service.startGame(dictionary, 6, 42L);
        var upperCaseSession = service.startGame(dictionary, 6, 42L);
        String guess = "озеро";

        var lowerCaseResult = service.applyGuess(lowerCaseSession, guess);
        var upperCaseResult = service.applyGuess(upperCaseSession, guess.toUpperCase(Locale.ROOT));

        assertThat(lowerCaseResult).isInstanceOf(GuessResult.Accepted.class);
        assertThat(upperCaseResult).isEqualTo(lowerCaseResult);
        assertThat(upperCaseSession.attemptsHistory()).containsExactly(guess);
        assertThat(upperCaseSession.status()).isEqualTo(lowerCaseSession.status());
    }
}
