package academy.fiveletters.cli;

import academy.fiveletters.game.GameActionResult;
import academy.fiveletters.game.GameStatus;
import academy.fiveletters.game.GuessResult;
import academy.fiveletters.game.HintResult;
import java.io.PrintWriter;
import java.util.Objects;

/** Общий вывод результатов попыток и итогов партии. */
final class ConsoleResultPrinter {

    private final ConsoleFormatter formatter = new ConsoleFormatter();
    private final PrintWriter output;

    ConsoleResultPrinter(PrintWriter output) {
        this.output = Objects.requireNonNull(output, "Вывод не должен быть null");
    }

    void printGuessResult(GuessResult result) {
        Objects.requireNonNull(result, "Результат не должен быть null");
        switch (result) {
            case GuessResult.Accepted accepted -> {
                output.println(formatter.formatGuess(accepted));
                output.println("Осталось попыток: " + accepted.attemptsRemaining());
            }
            case GuessResult.Rejected rejected -> output.println(formatter.formatRejection(rejected.reason()));
        }
        output.flush();
    }

    void printOutcome(GameStatus status, int attemptsUsed, String answer) {
        output.println(formatter.formatOutcome(status, attemptsUsed, answer));
        output.flush();
    }

    void printActionResult(GameActionResult result) {
        Objects.requireNonNull(result, "Результат действия не должен быть null");

        switch (result) {
            case GuessResult guess -> printGuessResult(guess);
            case HintResult hint -> {
                output.println(formatter.formatHint(hint));
                output.flush();
            }
        }
    }
}
