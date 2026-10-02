package academy.fiveletters.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GameSession;
import academy.fiveletters.game.HintService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Статистика игрока за запуск")
class PlayerStatisticsTest {

    private static final int MAX_ATTEMPTS = 6;
    private static final long SEED = 42L;

    private final WordDictionary dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
    private final GameService service = new GameService();
    private final PlayerStatistics statistics = new PlayerStatistics();

    @Test
    @DisplayName("До первой партии показатели и вычисляемые значения равны нулю")
    void startsEmpty() {
        var snapshot = statistics.snapshot();

        assertThat(snapshot).isEqualTo(new StatisticsSnapshot(0, 0, 0, 0, 0, 0));
        assertThat(snapshot.winRate()).isZero();
        assertThat(snapshot.averageWinningAttempts()).isZero();
    }

    @Test
    @DisplayName("Победа, поражение и прерывание учитываются с правильными знаменателями")
    void countsMixedOutcomes() {
        var win = newSession(MAX_ATTEMPTS);
        statistics.startGame(win);
        service.applyGuess(win, wrongGuessFor(win));
        new HintService().requestHint(win);
        service.applyGuess(win, win.answer());
        statistics.finishGame();

        var loss = newSession(1);
        statistics.startGame(loss);
        service.applyGuess(loss, wrongGuessFor(loss));
        statistics.finishGame();

        var interrupted = newSession(MAX_ATTEMPTS);
        statistics.startGame(interrupted);
        new HintService().requestHint(interrupted);
        statistics.finishGame();

        var snapshot = statistics.snapshot();

        assertThat(snapshot).isEqualTo(new StatisticsSnapshot(3, 1, 1, 1, 2, 2));
        assertThat(snapshot.completed()).isEqualTo(2);
        assertThat(snapshot.winRate()).isEqualTo(50.0);
        assertThat(snapshot.averageWinningAttempts()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Среднее число попыток рассчитывается по нескольким победам")
    void averagesAttemptsAcrossWins() {
        var first = newSession(MAX_ATTEMPTS);
        statistics.startGame(first);
        service.applyGuess(first, first.answer());
        statistics.finishGame();

        var second = newSession(MAX_ATTEMPTS);
        statistics.startGame(second);
        service.applyGuess(second, wrongGuessFor(second));
        service.applyGuess(second, wrongGuessFor(second));
        service.applyGuess(second, second.answer());
        statistics.finishGame();

        assertThat(statistics.snapshot().winRate()).isEqualTo(100.0);
        assertThat(statistics.snapshot().averageWinningAttempts()).isEqualTo(2.0);
    }

    @Test
    @DisplayName("Повторное завершение без новой партии не меняет статистику")
    void rejectsRepeatedFinish() {
        statistics.startGame(newSession(MAX_ATTEMPTS));
        statistics.finishGame();
        var before = statistics.snapshot();

        assertThatIllegalStateException().isThrownBy(statistics::finishGame);

        assertThat(statistics.snapshot()).isEqualTo(before);
    }

    @Test
    @DisplayName("Новую партию нельзя зарегистрировать до окончания учёта предыдущей")
    void rejectsOverlappingGames() {
        statistics.startGame(newSession(MAX_ATTEMPTS));

        assertThatIllegalStateException().isThrownBy(() -> statistics.startGame(newSession(MAX_ATTEMPTS)));

        assertThat(statistics.snapshot().started()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ранее полученный снимок не меняется после окончания партии")
    void snapshotDoesNotChange() {
        var session = newSession(MAX_ATTEMPTS);
        statistics.startGame(session);
        var before = statistics.snapshot();

        service.applyGuess(session, session.answer());
        statistics.finishGame();

        assertThat(before).isEqualTo(new StatisticsSnapshot(1, 0, 0, 0, 0, 0));
        assertThat(statistics.snapshot()).isEqualTo(new StatisticsSnapshot(1, 1, 0, 0, 0, 1));
    }

    private GameSession newSession(int maxAttempts) {
        return service.startGame(dictionary, maxAttempts, SEED);
    }

    private String wrongGuessFor(GameSession session) {
        return dictionary.words().stream()
                .filter(word -> !word.equals(session.answer()))
                .findFirst()
                .orElseThrow();
    }
}
