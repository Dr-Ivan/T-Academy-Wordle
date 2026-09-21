package academy.fiveletters.dictionary;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/** Неизменяемый словарь пятибуквенных слов. */
public final class WordDictionary {

    private static final int WORD_LENGTH = 5;
    private static final Pattern WORD_PATTERN = Pattern.compile("[а-я]{" + WORD_LENGTH + "}");

    private final List<String> words;
    private final Set<String> membership;

    public WordDictionary(Collection<String> source) {
        Objects.requireNonNull(source, "Коллекция слов не должна быть null");

        Set<String> uniqueWords = new TreeSet<>();

        for (String word : source) {
            Objects.requireNonNull(word, "Слово не должно быть null");
            String normalizedWord = word.toLowerCase(Locale.ROOT);
            if (!WORD_PATTERN.matcher(normalizedWord).matches()) {
                throw new IllegalArgumentException(
                        "Недопустимое слово словаря: '%s'. ".formatted(word) + "Ожидаются пять русских букв без ё.");
            }
            uniqueWords.add(normalizedWord);
        }

        if (uniqueWords.isEmpty()) {
            throw new IllegalArgumentException("Словарь не должен быть пустым");
        }

        this.words = List.copyOf(uniqueWords);
        this.membership = Set.copyOf(uniqueWords);
    }

    public int size() {
        return words.size();
    }

    public boolean contains(String word) {
        Objects.requireNonNull(word, "Слово не должно быть null");
        return membership.contains(word.toLowerCase(Locale.ROOT));
    }

    public List<String> words() {
        return words;
    }
}
