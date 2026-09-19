package academy.fiveletters;

import academy.fiveletters.cli.ConsoleGame;
import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.game.GameService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Точка входа в игру «5 букв». */
public final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final int DEFAULT_MAX_ATTEMPTS = 6;
    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_STARTUP_ERROR = 1;
    private static final int EXIT_USAGE_ERROR = 2;

    private static final String USAGE = """
        Использование: five-letters [--seed <целое число>]
                       five-letters --help

        Без --seed начальное значение выбирается автоматически.
        Угадайте слово из пяти букв за шесть попыток.
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

        long seed;
        try {
            seed = parseSeed(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка аргументов: " + e.getMessage());
            System.err.print(USAGE);
            return EXIT_USAGE_ERROR;
        }

        try (var input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            var dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
            var service = new GameService();
            var session = service.startGame(dictionary, DEFAULT_MAX_ATTEMPTS, seed);
            var output = new PrintWriter(System.out, true, StandardCharsets.UTF_8);

            output.println("Игра 5 букв");
            output.println("Seed: " + seed);
            output.println("Угадайте слово из пяти букв за %d попыток.".formatted(session.maxAttempts()));

            new ConsoleGame(service, input, output).play(session);

            return EXIT_SUCCESS;
        } catch (IOException | UncheckedIOException | IllegalArgumentException e) {
            System.err.println("Не удалось выполнить игру: " + e.getMessage());
            LOG.debug("Подробности ошибки выполнения игры", e);
            return EXIT_STARTUP_ERROR;
        }
    }

    private static long parseSeed(String[] args) {
        if (args.length == 0) {
            return new Random().nextLong();
        }

        if (args.length != 2 || !"--seed".equals(args[0])) {
            throw new IllegalArgumentException("Ожидается --seed <целое число> либо запуск без аргументов");
        }

        try {
            return Long.parseLong(args[1]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Seed должен быть целым числом от %d до %d".formatted(Long.MIN_VALUE, Long.MAX_VALUE), e);
        }
    }
}
