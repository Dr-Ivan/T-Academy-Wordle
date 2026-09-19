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
    private final ConsoleFormatter formatter;
    private final BufferedReader input;
    private final PrintWriter output;

    public ConsoleGame(GameService service, BufferedReader input, PrintWriter output) {
        this.service = Objects.requireNonNull(service, "Сервис не должен быть null");
        this.input = Objects.requireNonNull(input, "Ввод не должен быть null");
        this.output = Objects.requireNonNull(output, "Вывод не должен быть null");
        this.formatter = new ConsoleFormatter();
    }

    public void play(GameSession session) throws IOException {
        Objects.requireNonNull(session, "Сессия не должна быть null");

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

            switch (result) {
                case GuessResult.Accepted accepted -> {
                    output.println(formatter.formatGuess(accepted));
                    output.println("Осталось попыток: " + accepted.attemptsRemaining());
                }
                case GuessResult.Rejected rejected -> output.println(formatter.formatRejection(rejected.reason()));
            }
            output.flush();
        }
        printOutcome(session);
        output.flush();
    }

    private void printOutcome(GameSession session) {
        switch (session.status()) {
            case WIN -> output.println("Победа! Слово угадано за %d попыток".formatted(session.attemptsUsed()));
            case LOSE -> output.println("Неудача. Загаданное слово: " + session.answer());
            case IN_PROGRESS -> throw new IllegalStateException("Партия ещё не завершена");
        }
    }
}
