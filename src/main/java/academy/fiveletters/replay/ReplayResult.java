package academy.fiveletters.replay;

import academy.fiveletters.game.GameSession;
import academy.fiveletters.game.GameStatus;
import java.util.List;
import java.util.Objects;

public final class ReplayResult {

    private final String answer;
    private final GameStatus status;
    private final int maxAttempts;
    private final List<String> attemptsHistory;
    private final List<ReplayStep> steps;
    private final boolean hintUsed;

    ReplayResult(GameSession session, List<ReplayStep> steps) {
        Objects.requireNonNull(session, "Сессия не должна быть null");
        Objects.requireNonNull(steps, "Шаги не должны быть null");

        this.answer = session.answer();
        this.status = session.status();
        this.hintUsed = session.hintUsed();
        this.maxAttempts = session.maxAttempts();
        this.attemptsHistory = List.copyOf(session.attemptsHistory());
        this.steps = List.copyOf(steps);
    }

    public String answer() {
        return answer;
    }

    public GameStatus status() {
        return status;
    }

    public int attemptsUsed() {
        return attemptsHistory.size();
    }

    public int attemptsRemaining() {
        return maxAttempts - attemptsUsed();
    }

    public List<String> attemptsHistory() {
        return attemptsHistory;
    }

    public List<ReplayStep> steps() {
        return steps;
    }

    public boolean hintUsed() {
        return hintUsed;
    }
}
