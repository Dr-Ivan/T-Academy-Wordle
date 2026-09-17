package academy.fiveletters;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.game.GameService;
import java.io.UncheckedIOException;
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
        MR1: создаётся сессия и выводятся её параметры, включая ответ.
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

        try {
            var dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
            var session = new GameService().startGame(dictionary, DEFAULT_MAX_ATTEMPTS, seed);

            System.out.println("«5 букв»: демонстрация MR1");
            System.out.println("Seed: " + seed);
            System.out.println("Слов в словаре: " + dictionary.size());
            System.out.println("Ответ (демонстрация MR1): " + session.answer());
            System.out.println("Лимит попыток: " + session.maxAttempts());
            System.out.println("Использовано попыток: " + session.attemptsUsed());
            System.out.println("Статус: " + session.status());

            return EXIT_SUCCESS;
        } catch (IllegalArgumentException | UncheckedIOException e) {
            System.err.println("Не удалось создать игру: " + e.getMessage());
            LOG.debug("Подробности ошибки создания игры", e);
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
