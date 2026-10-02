package academy.fiveletters.statistics;

public record StatisticsSnapshot(
        long started, long wins, long losses, long interrupted, long gamesWithHints, long winningAttempts) {

    private static final double PERCENT_FACTOR = 100.0;

    public StatisticsSnapshot {
        if (started < 0 || wins < 0 || losses < 0 || interrupted < 0 || gamesWithHints < 0 || winningAttempts < 0) {
            throw new IllegalArgumentException("Показатели статистики не могут быть отрицательными");
        }

        long recordedGames = wins + losses + interrupted;

        if (recordedGames > started || gamesWithHints > recordedGames) {
            throw new IllegalArgumentException("Количество учтённых партий не согласовано");
        }

        if (winningAttempts < wins || (wins == 0 && winningAttempts != 0)) {
            throw new IllegalArgumentException("Количество попыток в победных партиях не согласовано");
        }
    }

    public long completed() {
        return wins + losses;
    }

    public double winRate() {
        return completed() == 0 ? 0.0 : PERCENT_FACTOR * wins / completed();
    }

    public double averageWinningAttempts() {
        return wins == 0 ? 0.0 : (double) winningAttempts / wins;
    }
}
