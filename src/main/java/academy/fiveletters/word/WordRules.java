package academy.fiveletters.word;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Общие правила формата пятибуквенных слов. */
public final class WordRules {

    private static final int WORD_LENGTH = 5;
    private static final Pattern WORD_PATTERN = Pattern.compile("[а-я]{" + WORD_LENGTH + "}");

    private WordRules() {}

    public static int length() {
        return WORD_LENGTH;
    }

    public static String normalize(String input) {
        Objects.requireNonNull(input, "Ввод не должен быть null");
        return input.strip().toLowerCase(Locale.ROOT);
    }

    public static boolean isCanonical(String word) {
        Objects.requireNonNull(word, "Слово не должно быть null");
        return WORD_PATTERN.matcher(word).matches();
    }

    public static void requireCanonical(String word, String argumentName) {
        Objects.requireNonNull(argumentName, "Имя аргумента не должно быть null");
        Objects.requireNonNull(word, argumentName + " не должен быть null");

        if (!isCanonical(word)) {
            throw new IllegalArgumentException(
                    "%s: '%s'. Ожидаются пять русских букв в нижнем регистре без ё.".formatted(argumentName, word));
        }
    }
}
