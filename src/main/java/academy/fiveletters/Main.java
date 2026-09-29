package academy.fiveletters;

import academy.fiveletters.cli.ColorMode;
import academy.fiveletters.cli.CommandLineParser;
import academy.fiveletters.cli.ConsoleGame;
import academy.fiveletters.cli.ConsoleMenu;
import academy.fiveletters.cli.ConsoleReplay;
import academy.fiveletters.cli.ConsoleStatisticsPrinter;
import academy.fiveletters.cli.LaunchOptions;
import academy.fiveletters.dictionary.DictionaryCatalog;
import academy.fiveletters.dictionary.DictionaryCatalogLoader;
import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GameSession;
import academy.fiveletters.replay.ReplayResult;
import academy.fiveletters.replay.ReplayService;
import academy.fiveletters.settings.GameSettings;
import academy.fiveletters.statistics.PlayerStatistics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Точка входа в игру «5 букв». */
public final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final int DEFAULT_MAX_ATTEMPTS = 6;
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_APPLICATION_ERROR = 1;
    private static final int EXIT_USAGE_ERROR = 2;

    private static final String USAGE = """
        Использование: five-letters
                       five-letters --seed <число> [настройки]
                       five-letters menu [--seed <число>] [настройки]
                       five-letters replay --seed <число> [настройки] --guesses [ввод...]
                       five-letters --help

        Настройки:
          --difficulty standard|easy
          --category all|nature|everyday
          --color never|always

        По умолчанию: standard, all, цвет выключен.
        Без аргументов открывается меню.
        Для одной партии и replay seed обязателен.
        Параметры до --guesses можно передавать в любом порядке.
        После --guesses каждый аргумент считается вводом игрока, включая :hint.
        """;

    private Main() {}

    public static void main(String[] args) {
        int exitCode = run(args);

        if (exitCode != EXIT_SUCCESS) {
            System.exit(exitCode);
        }
    }

    private static int run(String[] args) {
        if (args.length == 1 && "--help".equals(args[0])) {
            System.out.print(USAGE);
            return EXIT_SUCCESS;
        }

        LaunchOptions options;
        try {
            options = new CommandLineParser().parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка аргументов: " + e.getMessage());
            System.err.print(USAGE);
            return EXIT_USAGE_ERROR;
        }

        try {
            var catalog = new DictionaryCatalogLoader().load();
            var service = new GameService();
            var output = new PrintWriter(System.out, true, StandardCharsets.UTF_8);

            switch (options) {
                case LaunchOptions.Play play ->
                    playGame(
                            catalog.select(play.settings()),
                            service,
                            output,
                            play.seed(),
                            play.settings(),
                            play.colorMode());

                case LaunchOptions.Replay replay -> {
                    ReplayResult result = new ReplayService(service)
                            .replay(
                                    catalog.select(replay.settings()),
                                    DEFAULT_MAX_ATTEMPTS,
                                    replay.seed(),
                                    replay.guesses());

                    new ConsoleReplay(output, replay.colorMode()).print(replay.seed(), replay.settings(), result);
                }

                case LaunchOptions.Menu menu ->
                    runMenu(catalog, service, output, menu.seed(), menu.settings(), menu.colorMode());
            }

            return EXIT_SUCCESS;
        } catch (IOException | UncheckedIOException | IllegalArgumentException e) {
            System.err.println("Не удалось выполнить игру: " + e.getMessage());
            LOG.debug("Подробности ошибки выполнения игры", e);
            return EXIT_APPLICATION_ERROR;
        }
    }

    private static void playGame(
            WordDictionary dictionary,
            GameService service,
            PrintWriter output,
            long seed,
            GameSettings settings,
            ColorMode colorMode)
            throws IOException {
        GameSession session = service.startGame(dictionary, DEFAULT_MAX_ATTEMPTS, seed);
        var statistics = new PlayerStatistics();

        try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            statistics.startGame(session);
            try {
                new ConsoleGame(service, input, output, colorMode).play(session, seed, settings);
            } finally {
                statistics.finishGame();
                new ConsoleStatisticsPrinter(output).print(statistics.snapshot());
            }
        }
    }

    private static void runMenu(
            DictionaryCatalog catalog,
            GameService service,
            PrintWriter output,
            long seed,
            GameSettings settings,
            ColorMode colorMode)
            throws IOException {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            new ConsoleMenu(service, catalog, DEFAULT_MAX_ATTEMPTS, input, output).run(seed, settings, colorMode);
        }
    }
}
