package academy.fiveletters.cli;

import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GameSession;
import academy.fiveletters.game.GameStatus;
import academy.fiveletters.game.GuessResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

public final class ConsoleGame {

    private final GameService service;
    private final ConsoleResultPrinter resultPrinter;
    private final BufferedReader input;
    private final PrintWriter output;

    public ConsoleGame(GameService service, BufferedReader input, PrintWriter output) {
        this.service = Objects.requireNonNull(service, "Сервис не должен быть null");
        this.input = Objects.requireNonNull(input, "Ввод не должен быть null");
        this.output = Objects.requireNonNull(output, "Вывод не должен быть null");
        this.resultPrinter = new ConsoleResultPrinter(this.output);
    }

    public void play(GameSession session, long seed) throws IOException {
        Objects.requireNonNull(session, "Сессия не должна быть null");
        output.println("Игра 5 букв");
        output.println("Seed: " + seed);
        output.println("Угадайте слово из пяти букв за %d попыток.".formatted(session.maxAttempts()));

        while (session.status() == GameStatus.IN_PROGRESS) {
            output.println("Введите слово:");
            output.flush();
            String guess = input.readLine();

            if (guess == null) {
                output.println("Ввод завершён. Партия прервана.");
                output.flush();
                return;
            }
            GuessResult result = service.applyGuess(session, guess);
            resultPrinter.printGuessResult(result);
        }

        resultPrinter.printOutcome(session.status(), session.attemptsUsed(), session.answer());
    }
}
