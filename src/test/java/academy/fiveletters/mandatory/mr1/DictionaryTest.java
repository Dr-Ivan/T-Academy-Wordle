package academy.fiveletters.mandatory.mr1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.dictionary.WordDictionary;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: словарь. */
@DisplayName("MR1. Словарь")
class DictionaryTest {

    @Test
    @DisplayName("Словарь содержит не меньше 50 слов")
    void dictionaryContainsAtLeastFiftyWords() {
        var dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
        assertThat(dictionary.size()).isGreaterThanOrEqualTo(50);
    }

    @Test
    @DisplayName("Все слова словаря состоят ровно из 5 букв")
    void allWordsAreExactlyFiveLettersLong() {
        var dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
        assertThat(dictionary.words())
                .isNotEmpty()
                .allSatisfy(word -> assertThat(word).hasSize(5).matches("[а-я]{5}"));
    }

    @Test
    @DisplayName("Пустой словарь приводит к ошибке, а не к запуску игры без слова")
    void emptyDictionaryIsRejected() {
        assertThatIllegalArgumentException().isThrownBy(() -> new WordDictionary(List.of()));
    }
}
