package academy.fiveletters.cli;

import academy.fiveletters.settings.Difficulty;
import academy.fiveletters.settings.GameSettings;
import academy.fiveletters.settings.WordCategory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public final class CommandLineParser {

    private static final Set<String> VALUE_OPTIONS = Set.of("--seed", "--difficulty", "--category", "--color");

    public LaunchOptions parse(String[] args) {
        Objects.requireNonNull(args, "Аргументы не должны быть null");

        boolean menu = args.length == 0 || "menu".equals(args[0]);
        boolean replay = args.length > 0 && "replay".equals(args[0]);
        int startIndex = args.length > 0 && (menu || replay) ? 1 : 0;

        ParsedArguments parsed = parseArguments(args, startIndex, replay);

        var settings = new GameSettings(
                parseEnum(Difficulty.class, parsed.options().getOrDefault("--difficulty", "standard"), "сложность"),
                parseEnum(WordCategory.class, parsed.options().getOrDefault("--category", "all"), "категория"));
        ColorMode colorMode =
                parseEnum(ColorMode.class, parsed.options().getOrDefault("--color", "never"), "режим цвета");

        String seedValue = parsed.options().get("--seed");
        if (seedValue == null && !menu) {
            throw new IllegalArgumentException("Для этого режима обязателен параметр --seed");
        }

        long seed = seedValue == null ? new Random().nextLong() : parseSeed(seedValue);
        if (replay) {
            if (!parsed.guessesSpecified()) {
                throw new IllegalArgumentException("Для replay обязателен параметр --guesses");
            }

            return new LaunchOptions.Replay(seed, settings, colorMode, parsed.guesses());
        }

        if (menu) {
            return new LaunchOptions.Menu(seed, settings, colorMode);
        }

        return new LaunchOptions.Play(seed, settings, colorMode);
    }

    private ParsedArguments parseArguments(String[] args, int startIndex, boolean replay) {
        Map<String, String> options = new HashMap<>();
        int index = startIndex;

        while (index < args.length) {
            String option = Objects.requireNonNull(args[index++], "Аргумент не должен быть null");

            if ("--guesses".equals(option)) {
                if (!replay) {
                    throw new IllegalArgumentException("--guesses допустим только в режиме replay");
                }

                List<String> guesses = List.copyOf(Arrays.asList(Arrays.copyOfRange(args, index, args.length)));

                return new ParsedArguments(Map.copyOf(options), guesses, true);
            }

            if (!VALUE_OPTIONS.contains(option)) {
                throw new IllegalArgumentException("Неизвестный параметр: " + option);
            }

            if (options.containsKey(option)) {
                throw new IllegalArgumentException("Параметр указан повторно: " + option);
            }

            if (index >= args.length) {
                throw new IllegalArgumentException("Не задано значение параметра: " + option);
            }

            String value = Objects.requireNonNull(args[index++], "Значение параметра не должно быть null");

            if (value.startsWith("--")) {
                throw new IllegalArgumentException("Не задано значение параметра: " + option);
            }
            options.put(option, value);
        }

        return new ParsedArguments(Map.copyOf(options), List.of(), false);
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String description) {
        try {
            return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Недопустимое значение (%s): '%s'".formatted(description, value), e);
        }
    }

    private long parseSeed(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Seed должен быть целым числом от %d до %d".formatted(Long.MIN_VALUE, Long.MAX_VALUE), e);
        }
    }

    private record ParsedArguments(Map<String, String> options, List<String> guesses, boolean guessesSpecified) {}
}
