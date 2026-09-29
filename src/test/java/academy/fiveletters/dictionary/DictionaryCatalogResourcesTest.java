package academy.fiveletters.dictionary;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GameStatus;
import academy.fiveletters.settings.Difficulty;
import academy.fiveletters.settings.GameSettings;
import academy.fiveletters.settings.WordCategory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Встроенные словари сложности и категорий")
class DictionaryCatalogResourcesTest {

    private static final int MINIMUM_DICTIONARY_SIZE = 50;
    private static final int MAX_ATTEMPTS = 6;
    private static final long SEED = 42L;

    private final DictionaryCatalog catalog = new DictionaryCatalogLoader().load();

    @ParameterizedTest
    @MethodSource("settings")
    @DisplayName("Каждая комбинация настроек содержит минимум 50 слов и позволяет начать партию")
    void everyCombinationSupportsGame(Difficulty difficulty, WordCategory category) {
        var dictionary = catalog.select(new GameSettings(difficulty, category));

        assertThat(dictionary.size()).isGreaterThanOrEqualTo(MINIMUM_DICTIONARY_SIZE);

        var session = new GameService().startGame(dictionary, MAX_ATTEMPTS, SEED);

        assertThat(dictionary.contains(session.answer())).isTrue();
        assertThat(session.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(session.attemptsUsed()).isZero();
    }

    @ParameterizedTest
    @EnumSource(WordCategory.class)
    @DisplayName("Лёгкий уровень содержит подмножество слов обычного уровня в каждой категории")
    void easyLevelUsesSmallerDictionary(WordCategory category) {
        var standard = catalog.select(new GameSettings(Difficulty.STANDARD, category));
        var easy = catalog.select(new GameSettings(Difficulty.EASY, category));

        assertThat(standard.words()).containsAll(easy.words());
        assertThat(easy.size()).isLessThan(standard.size());
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    @DisplayName("Тематические словари различаются и содержат характерные слова")
    void categoriesHaveDifferentContents(Difficulty difficulty) {
        var nature = catalog.select(new GameSettings(difficulty, WordCategory.NATURE));
        var everyday = catalog.select(new GameSettings(difficulty, WordCategory.EVERYDAY));

        assertThat(nature.words()).contains("озеро", "сосна").doesNotContain("диван");
        assertThat(everyday.words()).contains("диван", "чашка").doesNotContain("озеро");
        assertThat(nature.words()).isNotEqualTo(everyday.words());
    }

    @Test
    @DisplayName("Настройки по умолчанию сохраняют исходный словарь и выбор ответа по seed")
    void defaultSettingsPreserveOriginalGame() {
        var original = new DictionaryLoader().loadResource("/dictionary.txt");
        var selected = catalog.select(GameSettings.DEFAULT);
        var service = new GameService();

        assertThat(selected.words()).containsExactlyElementsOf(original.words());

        var originalSession = service.startGame(original, MAX_ATTEMPTS, SEED);
        var selectedSession = service.startGame(selected, MAX_ATTEMPTS, SEED);

        assertThat(selectedSession.answer()).isEqualTo(originalSession.answer());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/dictionaries/easy.txt", "/dictionaries/nature.txt", "/dictionaries/everyday.txt"})
    @DisplayName("Дополнительный ресурс содержит корректные слова без повторяющихся строк")
    void resourceContainsCanonicalUniqueWords(String resourcePath) throws IOException {
        List<String> words = readWords(resourcePath);

        assertThat(words)
                .isNotEmpty()
                .doesNotHaveDuplicates()
                .allSatisfy(word -> assertThat(word).matches("[а-я]{5}"));
    }

    private static Stream<Arguments> settings() {
        return Arrays.stream(Difficulty.values())
                .flatMap(difficulty ->
                        Arrays.stream(WordCategory.values()).map(category -> Arguments.of(difficulty, category)));
    }

    private static List<String> readWords(String resourcePath) throws IOException {
        var stream = Objects.requireNonNull(
                DictionaryCatalogResourcesTest.class.getResourceAsStream(resourcePath),
                "Ресурс не найден: " + resourcePath);

        try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        }
    }
}
