package academy.fiveletters.cli;

import academy.fiveletters.replay.ReplayResult;
import academy.fiveletters.replay.ReplayStep;
import academy.fiveletters.settings.GameSettings;
import java.io.PrintWriter;
import java.util.Objects;

public final class ConsoleReplay {

    private final ConsoleResultPrinter resultPrinter;
    private final PrintWriter output;

    public ConsoleReplay(PrintWriter output) {
        this.output = Objects.requireNonNull(output, "Вывод не должен быть null");
        this.resultPrinter = new ConsoleResultPrinter(this.output);
    }

    public void print(long seed, GameSettings settings, ReplayResult replay) {
        Objects.requireNonNull(settings, "Настройки не должны быть null");
        Objects.requireNonNull(replay, "Результат replay не должен быть null");

        output.println("Воспроизведение партии");
        output.println("Seed: " + seed);
        resultPrinter.printSettings(settings);

        int stepNumber = 1;
        for (ReplayStep step : replay.steps()) {
            output.println("Шаг %d: \"%s\"".formatted(stepNumber, step.input()));
            resultPrinter.printActionResult(step.result());
            stepNumber++;
        }

        resultPrinter.printOutcome(replay.status(), replay.attemptsUsed(), replay.answer());
    }
}
