package academy.fiveletters.cli;

import academy.fiveletters.dictionary.DictionaryCatalog;
import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GameSession;
import academy.fiveletters.game.GameStatus;
import academy.fiveletters.settings.GameSettings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Random;

public final class ConsoleMenu {

    private final GameService service;
    private final DictionaryCatalog catalog;
    private final int maxAttempts;
    private final BufferedReader input;
    private final PrintWriter output;
    private final ConsoleGame game;
    private final ConsoleSettingsMenu settingsMenu;

    public ConsoleMenu(
            GameService service, DictionaryCatalog catalog, int maxAttempts, BufferedReader input, PrintWriter output) {
        this.service = Objects.requireNonNull(service, "Сервис не должен быть null");
        this.catalog = Objects.requireNonNull(catalog, "Каталог не должен быть null");

        if (maxAttempts <= 0) {
            throw new IllegalArgumentException("Количество попыток должно быть положительным");
        }

        this.maxAttempts = maxAttempts;
        this.input = Objects.requireNonNull(input, "Ввод не должен быть null");
        this.output = Objects.requireNonNull(output, "Вывод не должен быть null");
        this.game = new ConsoleGame(this.service, this.input, this.output);
        this.settingsMenu = new ConsoleSettingsMenu(this.input, this.output);
    }

    public void run(long initialSeed, GameSettings initialSettings) throws IOException {
        Random seedGenerator = new Random(initialSeed);
        long nextSeed = initialSeed;
        GameSettings settings = Objects.requireNonNull(initialSettings, "Начальные настройки не должны быть null");

        while (true) {
            printMenu(settings);
            String choice = input.readLine();
            if (choice == null) {
                printEndOfInput();
                return;
            }

            switch (choice.strip()) {
                case "1" -> {
                    WordDictionary dictionary = catalog.select(settings);
                    GameSession session = service.startGame(dictionary, maxAttempts, nextSeed);
                    game.play(session, nextSeed, settings);
                    if (session.status() == GameStatus.IN_PROGRESS) {
                        return;
                    }
                    nextSeed = seedGenerator.nextLong();
                }
                case "2" -> {
                    var editedSettings = settingsMenu.edit(settings);

                    if (editedSettings.isEmpty()) {
                        printEndOfInput();
                        return;
                    }

                    settings = editedSettings.orElseThrow();
                }
                case "0" -> {
                    output.println("До свидания!");
                    output.flush();
                    return;
                }
                default -> {
                    output.println("Неизвестный пункт меню. Введите 1, 2 или 0.");
                    output.flush();
                }
            }
        }
    }

    private void printMenu(GameSettings settings) {
        output.println("Главное меню");
        output.println("Сложность: " + settings.difficulty().title());
        output.println("Категория: " + settings.category().title());
        output.println("1. Новая игра");
        output.println("2. Настройки");
        output.println("0. Выход");
        output.println("Выберите пункт:");
        output.flush();
    }

    private void printEndOfInput() {
        output.println("Ввод завершён. Программа закрыта.");
        output.flush();
    }
}
