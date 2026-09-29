package academy.fiveletters.statistics;

import academy.fiveletters.game.GameSession;
import academy.fiveletters.game.GameStatus;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public final class PlayerStatistics {

    private @Nullable GameSession activeSession;

    private long started;
    private long wins;
    private long losses;
    private long interrupted;
    private long gamesWithHints;
    private long winningAttempts;

    public void startGame(GameSession session) {
        Objects.requireNonNull(session, "Сессия не должна быть null");

        if (activeSession != null) {
            throw new IllegalStateException("Предыдущая партия ещё не учтена");
        }
        if (session.status() != GameStatus.IN_PROGRESS || session.attemptsUsed() != 0 || session.hintUsed()) {
            throw new IllegalArgumentException("Регистрировать нужно новую игровую сессию");
        }
        activeSession = session;
        started++;
    }

    public void finishGame() {
        GameSession session = activeSession;
        if (session == null) {
            throw new IllegalStateException("Нет партии для завершения учёта");
        }

        switch (session.status()) {
            case WIN -> {
                wins++;
                winningAttempts += session.attemptsUsed();
            }
            case LOSE -> losses++;
            case IN_PROGRESS -> interrupted++;
        }
        if (session.hintUsed()) {
            gamesWithHints++;
        }
        activeSession = null;
    }

    public StatisticsSnapshot snapshot() {
        return new StatisticsSnapshot(started, wins, losses, interrupted, gamesWithHints, winningAttempts);
    }
}
