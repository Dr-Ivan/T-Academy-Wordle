package academy.fiveletters.cli;

import academy.fiveletters.game.GameActionResult;
import academy.fiveletters.game.GameActionService;
import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GameSession;
import academy.fiveletters.game.GameStatus;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

public final class ConsoleGame {

    private final GameActionService actionService;
    private final ConsoleResultPrinter resultPrinter;
    private final BufferedReader input;
    private final PrintWriter output;

    public ConsoleGame(GameService service, BufferedReader input, PrintWriter output) {
        Objects.requireNonNull(service, "Сервис не должен быть null");
        this.input = Objects.requireNonNull(input, "Ввод не должен быть null");
        this.output = Objects.requireNonNull(output, "Вывод не должен быть null");
        this.resultPrinter = new ConsoleResultPrinter(this.output);
        this.actionService = new GameActionService(service);
    }

    public void play(GameSession session, long seed) throws IOException {
        Objects.requireNonNull(session, "Сессия не должна быть null");
        output.println("Игра 5 букв");
        output.println("Seed: " + seed);
        output.println("Угадайте слово из пяти букв за %d попыток.".formatted(session.maxAttempts()));
        output.println("Для подсказки введите :hint. Доступна одна подсказка за партию.");

        while (session.status() == GameStatus.IN_PROGRESS) {
            output.println("Введите слово:");
            output.flush();
            String guess = input.readLine();

            if (guess == null) {
                output.println("Ввод завершён. Партия прервана.");
                output.flush();
                return;
            }
            GameActionResult result = actionService.apply(session, guess);
            resultPrinter.printActionResult(result);
        }

        resultPrinter.printOutcome(session.status(), session.attemptsUsed(), session.answer());
    }
}
