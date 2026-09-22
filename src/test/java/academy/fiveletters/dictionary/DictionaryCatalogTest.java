package academy.fiveletters.dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import academy.fiveletters.settings.Difficulty;
import academy.fiveletters.settings.GameSettings;
import academy.fiveletters.settings.WordCategory;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Выбор словаря по сложности и категории")
class DictionaryCatalogTest {

    private final WordDictionary standard =
            new WordDictionary(List.of("арбуз", "банан", "диван", "лампа", "озеро", "тайга"));
    private final WordDictionary easy = new WordDictionary(List.of("арбуз", "диван", "озеро"));
    private final WordDictionary nature = new WordDictionary(List.of("арбуз", "банан", "озеро", "тайга"));
    private final WordDictionary everyday = new WordDictionary(List.of("диван", "лампа"));
    private final DictionaryCatalog catalog = new DictionaryCatalog(standard, easy, nature, everyday);

    @Test
    @DisplayName("Настройки по умолчанию сохраняют полный исходный словарь")
    void defaultSettingsPreserveOriginalDictionary() {
        var selected = catalog.select(GameSettings.DEFAULT);
        assertThat(selected.words()).containsExactlyElementsOf(standard.words());
    }

    @ParameterizedTest
    @CsvSource({
        "STANDARD, ALL, арбуз банан диван лампа озеро тайга",
        "STANDARD, NATURE, арбуз банан озеро тайга",
        "STANDARD, EVERYDAY, диван лампа",
        "EASY, ALL, арбуз диван озеро",
        "EASY, NATURE, арбуз озеро",
        "EASY, EVERYDAY, диван"
    })
    @DisplayName("Выбранный словарь содержит пересечение сложности и категории")
    void selectsIntersection(Difficulty difficulty, WordCategory category, String expectedWords) {
        var selected = catalog.select(new GameSettings(difficulty, category));
        assertThat(selected.words()).containsExactly(expectedWords.split(" "));
    }

    @Test
    @DisplayName("Выбор другой комбинации не изменяет ранее выбранный и исходный словари")
    void selectionsDoNotChangeExistingDictionaries() {
        var first = catalog.select(new GameSettings(Difficulty.EASY, WordCategory.NATURE));
        catalog.select(new GameSettings(Difficulty.STANDARD, WordCategory.EVERYDAY));

        assertThat(first.words()).containsExactly("арбуз", "озеро");
        assertThat(standard.words()).containsExactly("арбуз", "банан", "диван", "лампа", "озеро", "тайга");
        assertThat(easy.words()).containsExactly("арбуз", "диван", "озеро");
    }

    @Test
    @DisplayName("Слово вне основного словаря отклоняется в любом дополнительном наборе")
    void rejectsWordsOutsideOriginalDictionary() {
        var unknownWords = new WordDictionary(List.of("сосна"));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DictionaryCatalog(standard, unknownWords, nature, everyday))
                .withMessageContaining("сосна");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DictionaryCatalog(standard, easy, unknownWords, everyday))
                .withMessageContaining("сосна");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> new DictionaryCatalog(standard, easy, nature, unknownWords))
                .withMessageContaining("сосна");
    }

    @Test
    @DisplayName("Комбинация без общих слов отклоняется с указанием сложности и категории")
    void rejectsEmptyIntersection() {
        var natureOnlyEasy = new WordDictionary(List.of("арбуз", "озеро"));
        var restrictedCatalog = new DictionaryCatalog(standard, natureOnlyEasy, nature, everyday);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> restrictedCatalog.select(new GameSettings(Difficulty.EASY, WordCategory.EVERYDAY)))
                .withMessageContaining("Лёгкая", "Быт");
    }

    @Test
    @DisplayName("Выбор словаря без настроек отклоняется")
    void rejectsNullSettings() {
        assertThatNullPointerException().isThrownBy(() -> catalog.select(null));
    }
}
