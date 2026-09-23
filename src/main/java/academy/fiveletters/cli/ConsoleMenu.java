package academy.fiveletters.cli;

import academy.fiveletters.dictionary.DictionaryCatalog;
import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GameSession;
import academy.fiveletters.game.GameStatus;
import academy.fiveletters.settings.GameSettings;
import academy.fiveletters.statistics.PlayerStatistics;
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
    private final ConsoleStatisticsPrinter statisticsPrinter;

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
        this.statisticsPrinter = new ConsoleStatisticsPrinter(this.output);
    }

    public void run(long initialSeed, GameSettings initialSettings) throws IOException {
        Objects.requireNonNull(initialSettings, "Начальные настройки не должны быть null");
        var statistics = new PlayerStatistics();
        try {
            runLoop(initialSeed, initialSettings, statistics);
        } finally {
            statisticsPrinter.print(statistics.snapshot());
        }
    }

    private void runLoop(long initialSeed, GameSettings initialSettings, PlayerStatistics statistics)
            throws IOException {
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
                    if (!playGame(settings, nextSeed, statistics)) {
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
                case "3" -> statisticsPrinter.print(statistics.snapshot());
                case "0" -> {
                    output.println("До свидания!");
                    output.flush();
                    return;
                }
                default -> {
                    output.println("Неизвестный пункт меню. Введите 1, 2, 3 или 0.");
                    output.flush();
                }
            }
        }
    }

    private boolean playGame(GameSettings settings, long seed, PlayerStatistics statistics) throws IOException {
        WordDictionary dictionary = catalog.select(settings);
        GameSession session = service.startGame(dictionary, maxAttempts, seed);
        statistics.startGame(session);
        try {
            game.play(session, seed, settings);
        } finally {
            statistics.finishGame();
        }
        return session.status() != GameStatus.IN_PROGRESS;
    }

    private void printMenu(GameSettings settings) {
        output.println("Главное меню");
        output.println("Сложность: " + settings.difficulty().title());
        output.println("Категория: " + settings.category().title());
        output.println("1. Новая игра");
        output.println("2. Настройки");
        output.println("3. Статистика");
        output.println("0. Выход");
        output.println("Выберите пункт:");
        output.flush();
    }

    private void printEndOfInput() {
        output.println("Ввод завершён. Программа закрыта.");
        output.flush();
    }
}
