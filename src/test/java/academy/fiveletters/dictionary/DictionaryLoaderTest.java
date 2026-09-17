package academy.fiveletters.dictionary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class DictionaryLoaderTest {

    private final DictionaryLoader loader = new DictionaryLoader();

    @Test
    void loadsUtf8WordsAndSkipsCommentsAndBlankLines() {
        var dictionary = loader.loadResource("/dictionaries/with-comments.txt");
        assertThat(dictionary.words()).containsExactly("арбуз", "озеро", "сорок");
    }

    @Test
    void rejectsMissingResource() {
        var resourcePath = "/dictionaries/missing.txt";
        assertThatIllegalArgumentException()
                .isThrownBy(() -> loader.loadResource(resourcePath))
                .withMessageContaining(resourcePath);
    }

    @Test
    void rejectsRelativeResourcePath() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> loader.loadResource("dictionary.txt"))
                .withMessageContaining("должен начинаться с /");
    }

    @Test
    void rejectsResourceWithoutWords() {
        assertThatIllegalArgumentException().isThrownBy(() -> loader.loadResource("/dictionaries/comments-only.txt"));
    }

    @Test
    void rejectsResourceContainingInvalidWord() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> loader.loadResource("/dictionaries/invalid-word.txt"))
                .withMessageContaining("дом12");
    }
}
