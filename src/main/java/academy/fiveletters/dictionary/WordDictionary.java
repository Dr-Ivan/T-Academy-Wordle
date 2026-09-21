package academy.fiveletters.dictionary;

import academy.fiveletters.word.WordRules;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/** Неизменяемый словарь пятибуквенных слов. */
public final class WordDictionary {

    private final List<String> words;
    private final Set<String> membership;

    public WordDictionary(Collection<String> source) {
        Objects.requireNonNull(source, "Коллекция слов не должна быть null");

        Set<String> uniqueWords = new TreeSet<>();

        for (String word : source) {
            Objects.requireNonNull(word, "Слово не должно быть null");
            String normalizedWord = word.toLowerCase(Locale.ROOT);

            WordRules.requireCanonical(normalizedWord, "Слово словаря");
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
