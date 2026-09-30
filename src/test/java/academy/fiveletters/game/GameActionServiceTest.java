package academy.fiveletters.game;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.dictionary.WordDictionary;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Обработка слов и подсказок в игровой партии")
class GameActionServiceTest {

    private static final int MAX_ATTEMPTS = 6;
    private final WordDictionary dictionary = new WordDictionary(List.of("озеро", "арбуз", "опера", "азарт", "ведро"));
    private final GameActionService actions = new GameActionService(new GameService());

    @ParameterizedTest
    @ValueSource(strings = {":hint", ":HINT", " \t:HiNt \n"})
    @DisplayName("Подсказка нормализует команду и открывает первую букву без расходования попытки")
    void revealsFirstLetterWithoutConsumingAttempt(String input) {
        var session = new GameSession(dictionary, "озеро", MAX_ATTEMPTS);

        var result = actions.apply(session, input);

        assertThat(result).isEqualTo(new HintResult.Revealed(1, 'о'));
        assertThat(session.hintUsed()).isTrue();
        assertThat(session.attemptsUsed()).isZero();
        assertThat(session.attemptsHistory()).isEmpty();
        assertThat(session.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Подсказка учитывает точные совпадения из всей истории попыток")
    void skipsPositionsRevealedAcrossPreviousGuesses() {
        var session = new GameSession(dictionary, "озеро", MAX_ATTEMPTS);
        actions.apply(session, "опера");
        actions.apply(session, "азарт");

        var result = actions.apply(session, ":hint");

        assertThat(result).isEqualTo(new HintResult.Revealed(5, 'о'));
        assertThat(session.attemptsHistory()).containsExactly("опера", "азарт");
        assertThat(session.attemptsUsed()).isEqualTo(2);
    }

    @Test
    @DisplayName("Повторная подсказка отклоняется даже после новой принятой попытки")
    void rejectsRepeatedHint() {
        var session = new GameSession(dictionary, "озеро", MAX_ATTEMPTS);
        actions.apply(session, ":hint");
        actions.apply(session, "арбуз");

        var result = actions.apply(session, ":hint");

        assertThat(result).isEqualTo(new HintResult.Rejected(HintRejectionReason.ALREADY_USED));
        assertThat(session.attemptsHistory()).containsExactly("арбуз");
        assertThat(session.attemptsUsed()).isEqualTo(1);
        assertThat(session.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"озеро", "арбуз"})
    @DisplayName("После победы или поражения подсказка отклоняется без изменения сессии")
    void rejectsHintAfterGameFinished(String guess) {
        var session = new GameSession(dictionary, "озеро", 1);
        actions.apply(session, guess);
        GameStatus finalStatus = session.status();

        var result = actions.apply(session, ":hint");

        assertThat(result).isEqualTo(new HintResult.Rejected(HintRejectionReason.GAME_FINISHED));
        assertThat(session.hintUsed()).isFalse();
        assertThat(session.status()).isEqualTo(finalStatus);
        assertThat(session.attemptsHistory()).containsExactly(guess);
    }

    @Test
    @DisplayName("Если все позиции уже раскрыты, подсказка не расходуется")
    void doesNotConsumeHintWhenAllPositionsAreKnown() {
        var session = new GameSession(dictionary, "озеро", MAX_ATTEMPTS);
        actions.apply(session, "опера");
        actions.apply(session, "азарт");
        actions.apply(session, "ведро");

        var result = actions.apply(session, ":hint");

        assertThat(result).isEqualTo(new HintResult.Rejected(HintRejectionReason.NO_HIDDEN_POSITIONS));
        assertThat(session.hintUsed()).isFalse();
        assertThat(session.attemptsHistory()).containsExactly("опера", "азарт", "ведро");
        assertThat(session.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Использование подсказки в одной партии не влияет на другую")
    void sessionsHaveIndependentHints() {
        var first = new GameSession(dictionary, "озеро", MAX_ATTEMPTS);
        var second = new GameSession(dictionary, "озеро", MAX_ATTEMPTS);

        actions.apply(first, ":hint");

        assertThat(second.hintUsed()).isFalse();
        assertThat(actions.apply(second, ":hint")).isEqualTo(new HintResult.Revealed(1, 'о'));
    }

    @Test
    @DisplayName("Обычные слова проходят прежнюю валидацию и могут завершить партию победой")
    void delegatesGuessesToGameService() {
        var session = new GameSession(dictionary, "озеро", MAX_ATTEMPTS);

        assertThat(actions.apply(session, "дом12"))
                .isEqualTo(new GuessResult.Rejected(GuessRejectionReason.INVALID_CHARACTERS));
        assertThat(session.attemptsUsed()).isZero();

        assertThat(actions.apply(session, " ОЗЕРО ")).isInstanceOfSatisfying(GuessResult.Accepted.class, result -> {
            assertThat(result.guess()).isEqualTo("озеро");
            assertThat(result.status()).isEqualTo(GameStatus.WIN);
        });

        assertThat(session.attemptsHistory()).containsExactly("озеро");
        assertThat(session.hintUsed()).isFalse();
    }
}
