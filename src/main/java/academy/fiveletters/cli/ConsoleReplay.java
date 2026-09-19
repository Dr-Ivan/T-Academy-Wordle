package academy.fiveletters.cli;

import academy.fiveletters.game.GuessResult;
import academy.fiveletters.replay.ReplayResult;
import academy.fiveletters.replay.ReplayStep;
import java.io.PrintWriter;
import java.util.Objects;

public final class ConsoleReplay {

    private final ConsoleFormatter formatter = new ConsoleFormatter();
    private final PrintWriter output;

    public ConsoleReplay(PrintWriter output) {
        this.output = Objects.requireNonNull(output, "Вывод не должен быть null");
    }

    public void print(long seed, ReplayResult replay) {
        Objects.requireNonNull(replay, "Результат replay не должен быть null");

        output.println("Воспроизведение партии");
        output.println("Seed: " + seed);

        int stepNumber = 1;
        for (ReplayStep step : replay.steps()) {
            output.println("Шаг %d: \"%s\"".formatted(stepNumber, step.input()));

            switch (step.result()) {
                case GuessResult.Accepted accepted -> {
                    output.println(formatter.formatGuess(accepted));
                    output.println("Осталось попыток: " + accepted.attemptsRemaining());
                }
                case GuessResult.Rejected rejected -> output.println(formatter.formatRejection(rejected.reason()));
            }

            stepNumber++;
        }

        output.println(formatter.formatOutcome(replay.status(), replay.attemptsUsed(), replay.answer()));
        output.flush();
    }
}
