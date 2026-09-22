package academy.fiveletters.cli;

import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GameSession;
import academy.fiveletters.game.GameStatus;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Random;

public final class ConsoleMenu {

    private final GameService service;
    private final WordDictionary dictionary;
    private final int maxAttempts;
    private final BufferedReader input;
    private final PrintWriter output;
    private final ConsoleGame game;

    public ConsoleMenu(
            GameService service, WordDictionary dictionary, int maxAttempts, BufferedReader input, PrintWriter output) {
        this.service = Objects.requireNonNull(service, "Сервис не должен быть null");
        this.dictionary = Objects.requireNonNull(dictionary, "Словарь не должен быть null");

        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("Количество попыток должно быть положительным");
        }

        this.maxAttempts = maxAttempts;
        this.input = Objects.requireNonNull(input, "Ввод не должен быть null");
        this.output = Objects.requireNonNull(output, "Вывод не должен быть null");
        this.game = new ConsoleGame(this.service, this.input, this.output);
    }

    public void run(long initialSeed) throws IOException {
        Random seedGenerator = new Random(initialSeed);
        long nextSeed = initialSeed;

        while (true) {
            printMenu();
            String choice = input.readLine();
            if (choice == null) {
                output.println("Ввод завершён. Программа закрыта.");
                output.flush();
                return;
            }

            switch (choice.strip()) {
                case "1" -> {
                    GameSession session = service.startGame(dictionary, maxAttempts, nextSeed);
                    game.play(session, nextSeed);
                    if (session.status() == GameStatus.IN_PROGRESS) {
                        return;
                    }
                    nextSeed = seedGenerator.nextLong();
                }
                case "0" -> {
                    output.println("До свидания!");
                    output.flush();
                    return;
                }
                default -> {
                    output.println("Неизвестный пункт меню. Введите 1 или 0.");
                    output.flush();
                }
            }
        }
    }

    private void printMenu() {
        output.println("Главное меню");
        output.println("1. Новая игра");
        output.println("0. Выход");
        output.println("Выберите пункт:");
        output.flush();
    }
}
