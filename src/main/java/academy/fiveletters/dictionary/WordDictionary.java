package academy.fiveletters.dictionary;

import academy.fiveletters.word.WordRules;
import java.util.Collection;
import java.util.List;
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
            WordRules.requireCanonical(word, "Слово словаря");
            uniqueWords.add(word);
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
        return membership.contains(word);
    }

    public List<String> words() {
        return words;
    }
}
