package academy.fiveletters.dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Содержимое и неизменяемость словаря")
class WordDictionaryTest {

    @Test
    @DisplayName("Словарь удаляет повторы и хранит слова в отсортированном порядке")
    void storesUniqueWordsInStableOrder() {
        var dictionary = new WordDictionary(List.of("сорок", "арбуз", "озеро", "арбуз"));

        assertThat(dictionary.words()).containsExactly("арбуз", "озеро", "сорок");
        assertThat(dictionary.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("Поиск возвращает true для присутствующего слова и false для отсутствующего")
    void checksWordMembership() {
        var dictionary = new WordDictionary(List.of("озеро", "сорок"));

        assertThat(dictionary.contains("озеро")).isTrue();
        assertThat(dictionary.contains("арбуз")).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "дом", "домики", "дом12", "дом!!", "до ма", "apple", "озёро", " озеро "})
    @DisplayName("Словарь отклоняет записи с неверной длиной, недопустимыми символами или внешними пробелами")
    void rejectsInvalidDictionaryEntry(String word) {
        assertThatIllegalArgumentException().isThrownBy(() -> new WordDictionary(List.of(word)));
    }

    @Test
    @DisplayName("Изменение исходной коллекции не изменяет содержимое словаря и результаты поиска")
    void doesNotDependOnLaterChangesToSource() {
        var source = new ArrayList<>(List.of("озеро", "сорок"));
        var dictionary = new WordDictionary(source);

        source.clear();
        source.add("арбуз");

        assertThat(dictionary.words()).containsExactly("озеро", "сорок");
        assertThat(dictionary.contains("озеро")).isTrue();
        assertThat(dictionary.contains("арбуз")).isFalse();
    }

    @Test
    @DisplayName("Возвращённый список слов запрещает изменение содержимого")
    void doesNotAllowChangingExposedWords() {
        var dictionary = new WordDictionary(List.of("озеро"));

        assertThatThrownBy(() -> dictionary.words().add("арбуз")).isInstanceOf(UnsupportedOperationException.class);

        assertThat(dictionary.words()).containsExactly("озеро");
    }

    @ParameterizedTest
    @ValueSource(strings = {"озеро", "ОЗЕРО", "ОзЕрО"})
    @DisplayName("Слова в нижнем, верхнем и смешанном регистре сохраняются в нижнем регистре")
    void normalizesDictionaryEntries(String word) {
        var dictionary = new WordDictionary(List.of(word));
        assertThat(dictionary.words()).containsExactly("озеро");
    }

    @Test
    @DisplayName("Варианты одного слова в разном регистре объединяются в одну запись")
    void removesDuplicatesAfterCaseNormalization() {
        var dictionary = new WordDictionary(List.of("озеро", "ОЗЕРО", "ОзЕрО"));
        assertThat(dictionary.size()).isEqualTo(1);
        assertThat(dictionary.words()).containsExactly("озеро");
    }

    @ParameterizedTest
    @ValueSource(strings = {"озеро", "ОЗЕРО", "ОзЕрО"})
    @DisplayName("Поиск находит слово независимо от регистра запроса")
    void findsWordsRegardlessOfCase(String word) {
        var dictionary = new WordDictionary(List.of("озеро"));
        assertThat(dictionary.contains(word)).isTrue();
    }
}
