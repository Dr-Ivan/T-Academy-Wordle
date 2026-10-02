package academy.fiveletters.dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Загрузка словаря из ресурсов")
class DictionaryLoaderTest {

    private final DictionaryLoader loader = new DictionaryLoader();

    @Test
    @DisplayName("Словарь читается в UTF-8, внешние пробелы удаляются, комментарии и пустые строки пропускаются")
    void loadsUtf8WordsAndSkipsCommentsAndBlankLines() {
        var dictionary = loader.loadResource("/dictionaries/with-comments.txt");
        assertThat(dictionary.words()).containsExactly("арбуз", "озеро", "сорок");
    }

    @Test
    @DisplayName("Отсутствующий ресурс словаря вызывает ошибку с указанием пути")
    void rejectsMissingResource() {
        var resourcePath = "/dictionaries/missing.txt";
        assertThatIllegalArgumentException()
                .isThrownBy(() -> loader.loadResource(resourcePath))
                .withMessageContaining(resourcePath);
    }

    @Test
    @DisplayName("Относительный путь ресурса словаря отклоняется")
    void rejectsRelativeResourcePath() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> loader.loadResource("dictionary.txt"))
                .withMessageContaining("должен начинаться с /");
    }

    @Test
    @DisplayName("Ресурс только с комментариями и пустыми строками отклоняется")
    void rejectsResourceWithoutWords() {
        assertThatIllegalArgumentException().isThrownBy(() -> loader.loadResource("/dictionaries/comments-only.txt"));
    }

    @Test
    @DisplayName("Некорректное слово в ресурсе вызывает ошибку с указанием слова")
    void rejectsResourceContainingInvalidWord() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> loader.loadResource("/dictionaries/invalid-word.txt"))
                .withMessageContaining("дом12");
    }
}
