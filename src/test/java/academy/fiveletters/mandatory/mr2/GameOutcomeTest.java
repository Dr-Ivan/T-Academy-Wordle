package academy.fiveletters.mandatory.mr2;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GameSession;
import academy.fiveletters.game.GameStatus;
import academy.fiveletters.game.GuessRejectionReason;
import academy.fiveletters.game.GuessResult;
import academy.fiveletters.game.LetterStatus;
import academy.fiveletters.support.CliRunner;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: завершение партии. */
@DisplayName("MR2. Победа и поражение")
class GameOutcomeTest {

    private final WordDictionary dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
    private final GameService service = new GameService();

    @Test
    @DisplayName("Угаданное слово переводит сессию в статус WIN")
    void correctGuessWinsTheGame() {
        var session = service.startGame(dictionary, 6, 42L);

        var result = service.applyGuess(session, session.answer());

        assertThat(result)
                .isEqualTo(new GuessResult.Accepted(
                        session.answer(), Collections.nCopies(5, LetterStatus.EXACT), GameStatus.WIN, 5));
        assertThat(session.status()).isEqualTo(GameStatus.WIN);
        assertThat(session.attemptsUsed()).isEqualTo(1);
        assertThat(session.attemptsHistory()).containsExactly(session.answer());
    }

    @Test
    @DisplayName("После 6 неудачных попыток сессия переходит в статус LOSE")
    void sixFailedAttemptsLoseTheGame() {
        var session = service.startGame(dictionary, 6, 42L);
        String wrongGuess = wrongGuessFor(session);

        for (int attempt = 1; attempt <= 6; attempt++) {
            var result = service.applyGuess(session, wrongGuess);
            int expectedRemaining = 6 - attempt;
            GameStatus expectedStatus = attempt == 6 ? GameStatus.LOSE : GameStatus.IN_PROGRESS;

            assertThat(result).isInstanceOfSatisfying(GuessResult.Accepted.class, accepted -> {
                assertThat(accepted.status()).isEqualTo(expectedStatus);
                assertThat(accepted.attemptsRemaining()).isEqualTo(expectedRemaining);
            });
            assertThat(session.status()).isEqualTo(expectedStatus);
            assertThat(session.attemptsUsed()).isEqualTo(attempt);
        }

        assertThat(session.attemptsHistory()).containsExactlyElementsOf(Collections.nCopies(6, wrongGuess));
    }

    @Test
    @DisplayName("При поражении показывается загаданное слово")
    void answerIsRevealedOnLoss() {
        var session = service.startGame(dictionary, 6, 42L);
        String wrongGuess = wrongGuessFor(session);
        String input = (wrongGuess + "\n").repeat(6);

        var result = CliRunner.runWithInput(input, "--seed", "42");

        assertThat(result.exitCode()).isZero();
        assertThat(result.stderr()).isEmpty();
        assertThat(result.stdout())
                .contains("Осталось попыток: 0", "Неудача. Загаданное слово: " + session.answer())
                .doesNotContain("Победа!", "Партия прервана.");
    }

    @Test
    @DisplayName("Завершённая партия больше не принимает попытки")
    void finishedGameRejectsFurtherGuesses() {
        var wonSession = service.startGame(dictionary, 1, 42L);
        var lostSession = service.startGame(dictionary, 1, 42L);

        assertThat(service.applyGuess(wonSession, wonSession.answer())).isInstanceOf(GuessResult.Accepted.class);
        assertThat(service.applyGuess(lostSession, wrongGuessFor(lostSession)))
                .isInstanceOf(GuessResult.Accepted.class);

        assertThat(wonSession.status()).isEqualTo(GameStatus.WIN);
        assertThat(lostSession.status()).isEqualTo(GameStatus.LOSE);

        assertFurtherGuessRejected(wonSession);
        assertFurtherGuessRejected(lostSession);
    }

    private String wrongGuessFor(GameSession session) {
        return dictionary.words().stream()
                .filter(word -> !word.equals(session.answer()))
                .findFirst()
                .orElseThrow();
    }

    private void assertFurtherGuessRejected(GameSession session) {
        var historyBefore = session.attemptsHistory();
        int attemptsBefore = session.attemptsUsed();
        var statusBefore = session.status();

        var result = service.applyGuess(session, session.answer());

        assertThat(result).isEqualTo(new GuessResult.Rejected(GuessRejectionReason.GAME_FINISHED));
        assertThat(session.attemptsHistory()).isEqualTo(historyBefore);
        assertThat(session.attemptsUsed()).isEqualTo(attemptsBefore);
        assertThat(session.status()).isEqualTo(statusBefore);
    }
}
