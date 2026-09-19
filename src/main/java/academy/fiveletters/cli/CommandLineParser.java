package academy.fiveletters.cli;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class CommandLineParser {

    private static final int GUESSES_OPTION_INDEX = 3;
    private static final int FIRST_GUESS_INDEX = 4;

    public LaunchOptions parse(String[] args) {
        Objects.requireNonNull(args, "Аргументы не должны быть null");

        if (args.length == 0) {
            return new LaunchOptions.Play(new Random().nextLong());
        }

        if ("replay".equals(args[0])) {
            return parseReplay(args);
        }

        if (args.length == 2 && "--seed".equals(args[0])) {
            return new LaunchOptions.Play(parseSeed(args[1]));
        }

        throw new IllegalArgumentException("Ожидается --seed <целое число>, команда replay либо запуск без аргументов");
    }

    private LaunchOptions.Replay parseReplay(String[] args) {
        if (args.length < FIRST_GUESS_INDEX
                || !"--seed".equals(args[1])
                || !"--guesses".equals(args[GUESSES_OPTION_INDEX])) {
            throw new IllegalArgumentException("Ожидается replay --seed <целое число> --guesses [слова...]");
        }

        long seed = parseSeed(args[2]);
        List<String> guesses = Arrays.asList(Arrays.copyOfRange(args, FIRST_GUESS_INDEX, args.length));

        return new LaunchOptions.Replay(seed, guesses);
    }

    private long parseSeed(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Seed должен быть целым числом от %d до %d".formatted(Long.MIN_VALUE, Long.MAX_VALUE), e);
        }
    }
}
