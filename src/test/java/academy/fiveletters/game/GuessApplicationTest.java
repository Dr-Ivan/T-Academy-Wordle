package academy.fiveletters.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import academy.fiveletters.dictionary.WordDictionary;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GuessApplicationTest {

    private final WordDictionary dictionary = new WordDictionary(List.of("озеро", "арбуз", "сорок"));
    private final GameService service = new GameService();

    @Test
    void acceptedGuessReturnsFeedbackAndUpdatesSession() {
        var session = new GameSession(dictionary, "озеро", 6);

        var result = service.applyGuess(session, "арбуз");

        assertThat(result)
                .isEqualTo(new GuessResult.Accepted(
                        "арбуз",
                        List.of(
                                LetterStatus.ABSENT,
                                LetterStatus.PRESENT,
                                LetterStatus.ABSENT,
                                LetterStatus.ABSENT,
                                LetterStatus.PRESENT),
                        GameStatus.IN_PROGRESS,
                        5));
        assertThat(session.attemptsHistory()).containsExactly("арбуз");
        assertThat(session.attemptsUsed()).isEqualTo(1);
        assertThat(session.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void normalizesCaseAndOuterWhitespaceBeforeRecordingGuess() {
        var session = new GameSession(dictionary, "озеро", 6);

        var result = service.applyGuess(session, " \tАрБуЗ \n");

        assertThat(result).isInstanceOf(GuessResult.Accepted.class);
        assertThat(session.attemptsHistory()).containsExactly("арбуз");
    }

    @ParameterizedTest
    @ValueSource(strings = {"apple", "озёро", "до ма"})
    void rejectsUnsupportedCharactersWithoutChangingSession(String guess) {
        var session = new GameSession(dictionary, "озеро", 6);

        var result = service.applyGuess(session, guess);

        assertThat(result).isEqualTo(new GuessResult.Rejected(GuessRejectionReason.INVALID_CHARACTERS));
        assertThat(session.attemptsHistory()).isEmpty();
        assertThat(session.attemptsUsed()).isZero();
        assertThat(session.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void validatesAgainstCurrentSessionDictionary() {
        var restrictedDictionary = new WordDictionary(List.of("озеро"));
        var session = new GameSession(restrictedDictionary, "озеро", 6);

        var result = service.applyGuess(session, "арбуз");

        assertThat(result).isEqualTo(new GuessResult.Rejected(GuessRejectionReason.WORD_NOT_IN_DICTIONARY));
        assertThat(session.attemptsHistory()).isEmpty();
    }

    @Test
    void acceptedResultRemainsUnchangedAfterNextGuess() {
        var session = new GameSession(dictionary, "озеро", 6);
        var first = service.applyGuess(session, "арбуз");

        var second = service.applyGuess(session, "озеро");

        assertThat(first).isInstanceOfSatisfying(GuessResult.Accepted.class, accepted -> {
            assertThat(accepted.status()).isEqualTo(GameStatus.IN_PROGRESS);
            assertThat(accepted.attemptsRemaining()).isEqualTo(5);
        });
        assertThat(second).isInstanceOfSatisfying(GuessResult.Accepted.class, accepted -> {
            assertThat(accepted.status()).isEqualTo(GameStatus.WIN);
            assertThat(accepted.attemptsRemaining()).isEqualTo(4);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"озеро", "арбуз"})
    void rejectsGuessAfterWinOrLoss(String firstGuess) {
        var session = new GameSession(dictionary, "озеро", 1);
        assertThat(service.applyGuess(session, firstGuess)).isInstanceOf(GuessResult.Accepted.class);
        var finalStatus = session.status();

        var result = service.applyGuess(session, "сорок");

        assertThat(result).isEqualTo(new GuessResult.Rejected(GuessRejectionReason.GAME_FINISHED));
        assertThat(session.attemptsHistory()).containsExactly(firstGuess);
        assertThat(session.attemptsUsed()).isEqualTo(1);
        assertThat(session.status()).isEqualTo(finalStatus);
    }

    @Test
    void nullGuessIsProgrammingErrorAndDoesNotChangeSession() {
        var session = new GameSession(dictionary, "озеро", 6);

        assertThatNullPointerException().isThrownBy(() -> service.applyGuess(session, null));

        assertThat(session.attemptsHistory()).isEmpty();
        assertThat(session.attemptsUsed()).isZero();
    }

    @Test
    void correctSixthGuessWinsGame() {
        var session = new GameSession(dictionary, "озеро", 6);

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThat(service.applyGuess(session, "арбуз")).isInstanceOf(GuessResult.Accepted.class);
        }

        assertThat(session.status()).isEqualTo(GameStatus.IN_PROGRESS);

        var result = service.applyGuess(session, "озеро");

        assertThat(result)
                .isEqualTo(new GuessResult.Accepted(
                        "озеро",
                        List.of(
                                LetterStatus.EXACT,
                                LetterStatus.EXACT,
                                LetterStatus.EXACT,
                                LetterStatus.EXACT,
                                LetterStatus.EXACT),
                        GameStatus.WIN,
                        0));
        assertThat(session.status()).isEqualTo(GameStatus.WIN);
        assertThat(session.attemptsUsed()).isEqualTo(6);
        assertThat(session.attemptsHistory()).containsExactly("арбуз", "арбуз", "арбуз", "арбуз", "арбуз", "озеро");
    }

    @Test
    void rejectedInputDoesNotConsumeLastAttempt() {
        var session = new GameSession(dictionary, "озеро", 6);

        for (int attempt = 0; attempt < 5; attempt++) {
            assertThat(service.applyGuess(session, "арбуз")).isInstanceOf(GuessResult.Accepted.class);
        }

        var rejected = service.applyGuess(session, "дом12");

        assertThat(rejected).isEqualTo(new GuessResult.Rejected(GuessRejectionReason.INVALID_CHARACTERS));
        assertThat(session.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(session.attemptsUsed()).isEqualTo(5);

        var accepted = service.applyGuess(session, "озеро");

        assertThat(accepted).isInstanceOfSatisfying(GuessResult.Accepted.class, result -> {
            assertThat(result.status()).isEqualTo(GameStatus.WIN);
            assertThat(result.attemptsRemaining()).isZero();
        });
        assertThat(session.status()).isEqualTo(GameStatus.WIN);
        assertThat(session.attemptsUsed()).isEqualTo(6);
    }
}
