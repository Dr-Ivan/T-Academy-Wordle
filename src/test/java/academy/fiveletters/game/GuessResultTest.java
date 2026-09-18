package academy.fiveletters.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class GuessResultTest {

    @Test
    void acceptedResultProtectsLetterStatusesFromChanges() {
        var original = new ArrayList<>(Collections.nCopies(5, LetterStatus.ABSENT));

        var result = new GuessResult.Accepted("арбуз", original, GameStatus.IN_PROGRESS, 5);

        original.set(0, LetterStatus.EXACT);

        assertThat(result.letters())
                .containsExactly(
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT,
                        LetterStatus.ABSENT);

        assertThatThrownBy(() -> result.letters().set(0, LetterStatus.EXACT))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void acceptsWinOnLastAttempt() {
        var result = new GuessResult.Accepted("озеро", Collections.nCopies(5, LetterStatus.EXACT), GameStatus.WIN, 0);

        assertThat(result.status()).isEqualTo(GameStatus.WIN);
        assertThat(result.attemptsRemaining()).isZero();
    }

    @Test
    void acceptsLossWithNoAttemptsRemaining() {
        var result = new GuessResult.Accepted("арбуз", Collections.nCopies(5, LetterStatus.ABSENT), GameStatus.LOSE, 0);

        assertThat(result.status()).isEqualTo(GameStatus.LOSE);
        assertThat(result.attemptsRemaining()).isZero();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4, 6})
    void rejectsWrongNumberOfLetterStatuses(int size) {
        List<LetterStatus> letters = Collections.nCopies(size, LetterStatus.ABSENT);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new GuessResult.Accepted("арбуз", letters, GameStatus.IN_PROGRESS, 5));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "дом", "ОЗЕРО", "дом12", "озёро"})
    void rejectsNonCanonicalWord(String guess) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new GuessResult.Accepted(
                        guess, Collections.nCopies(5, LetterStatus.ABSENT), GameStatus.IN_PROGRESS, 5));
    }

    @ParameterizedTest
    @CsvSource({
        "WIN, 1, ABSENT",
        "IN_PROGRESS, 1, EXACT",
        "LOSE, 0, EXACT",
        "LOSE, 1, ABSENT",
        "IN_PROGRESS, 0, ABSENT",
        "IN_PROGRESS, -1, ABSENT"
    })
    void rejectsInconsistentOutcome(GameStatus status, int remaining, LetterStatus letterStatus) {
        assertThatIllegalArgumentException()
                .isThrownBy(() ->
                        new GuessResult.Accepted("озеро", Collections.nCopies(5, letterStatus), status, remaining));
    }

    @Test
    void rejectsNullLetterStatus() {
        var letters = new ArrayList<>(Collections.nCopies(5, LetterStatus.ABSENT));
        letters.set(0, null);

        assertThatNullPointerException()
                .isThrownBy(() -> new GuessResult.Accepted("арбуз", letters, GameStatus.IN_PROGRESS, 5));
    }

    @Test
    void rejectionRequiresReason() {
        assertThatNullPointerException().isThrownBy(() -> new GuessResult.Rejected(null));
    }
}
