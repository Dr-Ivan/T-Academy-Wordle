package academy.fiveletters.replay;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GameStatus;
import academy.fiveletters.game.GuessRejectionReason;
import academy.fiveletters.game.GuessResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class ReplayServiceTest {

    private final WordDictionary dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
    private final GameService gameService = new GameService();
    private final ReplayService replayService = new ReplayService(gameService);

    @Test
    void replayMatchesSequentialApplicationOfGuesses() {
        var session = gameService.startGame(dictionary, 6, 42L);
        String wrongGuess = wrongGuessFor(session.answer());
        List<String> inputs =
                List.of("дом12", wrongGuess, " " + session.answer().toUpperCase(Locale.ROOT) + " ");

        List<ReplayStep> expectedSteps = new ArrayList<>();
        for (String input : inputs) {
            expectedSteps.add(new ReplayStep(input, gameService.applyGuess(session, input)));
        }

        var replay = replayService.replay(dictionary, 6, 42L, inputs);

        assertThat(replay.steps()).containsExactlyElementsOf(expectedSteps);
        assertThat(replay.answer()).isEqualTo(session.answer());
        assertThat(replay.status()).isEqualTo(GameStatus.WIN);
        assertThat(replay.attemptsUsed()).isEqualTo(2);
        assertThat(replay.attemptsRemaining()).isEqualTo(4);
        assertThat(replay.attemptsHistory()).containsExactlyElementsOf(session.attemptsHistory());
    }

    @Test
    void recordsRejectedInputWithoutConsumingAttempt() {
        var replay = replayService.replay(dictionary, 6, 42L, List.of("", "дом12"));

        assertThat(replay.steps())
                .containsExactly(
                        new ReplayStep("", new GuessResult.Rejected(GuessRejectionReason.INVALID_LENGTH)),
                        new ReplayStep("дом12", new GuessResult.Rejected(GuessRejectionReason.INVALID_CHARACTERS)));
        assertThat(replay.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(replay.attemptsHistory()).isEmpty();
        assertThat(replay.attemptsRemaining()).isEqualTo(6);
    }

    @Test
    void recordsInputAfterWinAsRejected() {
        String answer = gameService.startGame(dictionary, 6, 42L).answer();

        var replay = replayService.replay(dictionary, 6, 42L, List.of(answer, "дом12"));

        assertThat(replay.status()).isEqualTo(GameStatus.WIN);
        assertThat(replay.attemptsHistory()).containsExactly(answer);
        assertThat(replay.steps()).hasSize(2);
        assertThat(replay.steps().getLast())
                .isEqualTo(new ReplayStep("дом12", new GuessResult.Rejected(GuessRejectionReason.GAME_FINISHED)));
    }

    @Test
    void recordsInputAfterLossAsRejected() {
        String answer = gameService.startGame(dictionary, 6, 42L).answer();
        String wrongGuess = wrongGuessFor(answer);
        var inputs = new ArrayList<>(Collections.nCopies(6, wrongGuess));
        inputs.add(answer);

        var replay = replayService.replay(dictionary, 6, 42L, inputs);

        assertThat(replay.status()).isEqualTo(GameStatus.LOSE);
        assertThat(replay.attemptsUsed()).isEqualTo(6);
        assertThat(replay.attemptsRemaining()).isZero();
        assertThat(replay.steps()).hasSize(7);
        assertThat(replay.steps().getLast())
                .isEqualTo(new ReplayStep(answer, new GuessResult.Rejected(GuessRejectionReason.GAME_FINISHED)));
    }

    @Test
    void emptyScenarioLeavesFreshSessionInProgress() {
        var replay = replayService.replay(dictionary, 6, 42L, List.of());

        assertThat(replay.steps()).isEmpty();
        assertThat(replay.attemptsHistory()).isEmpty();
        assertThat(replay.attemptsUsed()).isZero();
        assertThat(replay.attemptsRemaining()).isEqualTo(6);
        assertThat(replay.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void repeatedReplayCreatesIndependentGame() {
        String answer = gameService.startGame(dictionary, 6, 42L).answer();
        List<String> inputs = List.of(answer);

        var first = replayService.replay(dictionary, 6, 42L, inputs);
        var second = replayService.replay(dictionary, 6, 42L, inputs);

        assertThat(second.steps()).isEqualTo(first.steps());
        assertThat(second.answer()).isEqualTo(first.answer());
        assertThat(second.status()).isEqualTo(GameStatus.WIN);
        assertThat(second.attemptsUsed()).isEqualTo(1);
    }

    @Test
    void resultDoesNotExposeMutableCollections() {
        String answer = gameService.startGame(dictionary, 6, 42L).answer();
        var inputs = new ArrayList<>(List.of(answer));

        var replay = replayService.replay(dictionary, 6, 42L, inputs);
        inputs.clear();

        assertThat(replay.steps()).hasSize(1);
        assertThat(replay.attemptsHistory()).containsExactly(answer);
        assertThatThrownBy(() -> replay.steps().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> replay.attemptsHistory().clear()).isInstanceOf(UnsupportedOperationException.class);
    }

    private String wrongGuessFor(String answer) {
        return dictionary.words().stream()
                .filter(word -> !word.equals(answer))
                .findFirst()
                .orElseThrow();
    }
}
