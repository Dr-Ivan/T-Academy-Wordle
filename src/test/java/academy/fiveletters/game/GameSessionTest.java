package academy.fiveletters.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import academy.fiveletters.dictionary.WordDictionary;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GameSessionTest {

    private final WordDictionary dictionary = new WordDictionary(List.of("озеро", "арбуз", "сорок"));

    @Test
    void startsWithEmptyHistoryAndNoAttemptsUsed() {
        var session = new GameSession(dictionary, "озеро", 6);

        assertThat(session.answer()).isEqualTo("озеро");
        assertThat(session.maxAttempts()).isEqualTo(6);
        assertThat(session.attemptsUsed()).isZero();
        assertThat(session.attemptsHistory()).isEmpty();
        assertThat(session.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsNonPositiveAttemptLimit(int maxAttempts) {
        assertThatIllegalArgumentException().isThrownBy(() -> new GameSession(dictionary, "озеро", maxAttempts));
    }

    @Test
    void rejectsAnswerOutsideDictionary() {
        assertThatIllegalArgumentException().isThrownBy(() -> new GameSession(dictionary, "ветер", 6));
    }

    @Test
    void recordsAcceptedGuessesInOrder() {
        var session = new GameSession(dictionary, "озеро", 6);

        session.recordGuess("арбуз");
        session.recordGuess("сорок");

        assertThat(session.attemptsHistory()).containsExactly("арбуз", "сорок");
        assertThat(session.attemptsUsed()).isEqualTo(2);
        assertThat(session.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void rejectedGuessDoesNotChangeSession() {
        var session = new GameSession(dictionary, "озеро", 6);
        session.recordGuess("арбуз");

        assertThatIllegalArgumentException().isThrownBy(() -> session.recordGuess("ветер"));

        assertThat(session.attemptsHistory()).containsExactly("арбуз");
        assertThat(session.attemptsUsed()).isEqualTo(1);
        assertThat(session.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void historyIsAnUnmodifiableSnapshot() {
        var session = new GameSession(dictionary, "озеро", 6);
        session.recordGuess("арбуз");
        var snapshot = session.attemptsHistory();

        assertThatThrownBy(() -> snapshot.add("сорок")).isInstanceOf(UnsupportedOperationException.class);
        session.recordGuess("сорок");

        assertThat(snapshot).containsExactly("арбуз");
        assertThat(session.attemptsHistory()).containsExactly("арбуз", "сорок");
    }

    @Test
    void correctGuessWinsBeforeAttemptLimit() {
        var session = new GameSession(dictionary, "озеро", 6);

        session.recordGuess("озеро");

        assertThat(session.status()).isEqualTo(GameStatus.WIN);
        assertThat(session.attemptsUsed()).isEqualTo(1);
    }

    @Test
    void correctGuessOnLastAttemptWins() {
        var session = new GameSession(dictionary, "озеро", 2);

        session.recordGuess("арбуз");
        session.recordGuess("озеро");

        assertThat(session.status()).isEqualTo(GameStatus.WIN);
        assertThat(session.attemptsUsed()).isEqualTo(2);
    }

    @Test
    void exhaustedAttemptsLoseGame() {
        var session = new GameSession(dictionary, "озеро", 2);

        session.recordGuess("арбуз");
        session.recordGuess("сорок");

        assertThat(session.status()).isEqualTo(GameStatus.LOSE);
        assertThat(session.attemptsUsed()).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"озеро", "арбуз"})
    void finishedSessionRejectsFurtherGuesses(String firstGuess) {
        var session = new GameSession(dictionary, "озеро", 1);
        session.recordGuess(firstGuess);
        var finalStatus = session.status();

        assertThatIllegalStateException().isThrownBy(() -> session.recordGuess("сорок"));

        assertThat(session.attemptsHistory()).containsExactly(firstGuess);
        assertThat(session.attemptsUsed()).isEqualTo(1);
        assertThat(session.status()).isEqualTo(finalStatus);
    }
}
