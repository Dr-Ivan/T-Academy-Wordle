package academy.fiveletters.dictionary;

import academy.fiveletters.settings.Difficulty;
import academy.fiveletters.settings.GameSettings;
import academy.fiveletters.settings.WordCategory;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Выбирает словарь по сложности и категории. */
public final class DictionaryCatalog {

    private final WordDictionary standard;
    private final Map<Difficulty, WordDictionary> difficulties;
    private final Map<WordCategory, WordDictionary> categories;

    public DictionaryCatalog(
            WordDictionary standard, WordDictionary easy, WordDictionary nature, WordDictionary everyday) {
        this.standard = Objects.requireNonNull(standard, "Основной словарь не должен быть null");
        Objects.requireNonNull(easy, "Словарь лёгкого уровня не должен быть null");
        Objects.requireNonNull(nature, "Словарь природы не должен быть null");
        Objects.requireNonNull(everyday, "Словарь быта не должен быть null");

        requireSubset(easy, "Лёгкий уровень");
        requireSubset(nature, "Природа");
        requireSubset(everyday, "Быт");

        this.difficulties = Map.of(
                Difficulty.STANDARD, standard,
                Difficulty.EASY, easy);

        this.categories = Map.of(
                WordCategory.ALL, standard,
                WordCategory.NATURE, nature,
                WordCategory.EVERYDAY, everyday);
    }

    public WordDictionary select(GameSettings settings) {
        Objects.requireNonNull(settings, "Настройки не должны быть null");

        WordDictionary difficultyDictionary = Objects.requireNonNull(
                difficulties.get(settings.difficulty()), "Не настроен словарь для сложности: " + settings.difficulty());

        WordDictionary categoryDictionary = Objects.requireNonNull(
                categories.get(settings.category()), "Не настроен словарь для категории: " + settings.category());

        if (settings.category() == WordCategory.ALL) {
            return difficultyDictionary;
        }

        List<String> selectedWords = difficultyDictionary.words().stream()
                .filter(categoryDictionary::contains)
                .toList();

        if (selectedWords.isEmpty()) {
            throw new IllegalArgumentException("Нет слов для сложности '%s' и категории '%s'"
                    .formatted(
                            settings.difficulty().title(), settings.category().title()));
        }
        return new WordDictionary(selectedWords);
    }

    private void requireSubset(WordDictionary dictionary, String name) {
        for (String word : dictionary.words()) {
            if (!standard.contains(word)) {
                throw new IllegalArgumentException(
                        "Набор '%s' содержит слово вне основного словаря: '%s'".formatted(name, word));
            }
        }
    }
}
