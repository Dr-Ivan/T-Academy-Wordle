package academy.fiveletters;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.support.CliRunner;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Проверка окружения. Если эти тесты упали — дело в настройке, а не в задании, см. README. */
@DisplayName("Проверка окружения")
class TemplateSmokeTest {

    private static final int MINIMUM_DICTIONARY_SIZE = 50;
    private static final int WORD_LENGTH = 5;

    @Test
    @DisplayName("Приложение запускается и завершается с кодом 0")
    void applicationStartsAndExitsCleanly() {
        var result = CliRunner.run();

        assertThat(result.exitCode())
                .as("Программа завершилась с ненулевым кодом.%n%s", result)
                .isZero();
        assertThat(result.stdout()).as("Программа ничего не напечатала").isNotBlank();
    }

    @Test
    @DisplayName("Словарь из ресурсов читается и состоит из слов ровно в 5 букв")
    void bundledDictionaryIsValid() {
        var words = readDictionary();

        assertThat(words)
                .as("В файле src/main/resources/dictionary.txt должно быть не меньше %d слов", MINIMUM_DICTIONARY_SIZE)
                .hasSizeGreaterThanOrEqualTo(MINIMUM_DICTIONARY_SIZE);

        assertThat(words)
                .as(
                        "Все слова должны состоять ровно из %d русских букв в нижнем регистре. "
                                + "Если тест упал на первом же слове — файл прочитан не в UTF-8",
                        WORD_LENGTH)
                .allSatisfy(word -> assertThat(word).hasSize(WORD_LENGTH).matches("[а-я]{%d}".formatted(WORD_LENGTH)));
    }

    private static List<String> readDictionary() {
        var stream = TemplateSmokeTest.class.getResourceAsStream("/dictionary.txt");
        assertThat(stream)
                .as("Файл src/main/resources/dictionary.txt не найден")
                .isNotNull();

        try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(String::strip)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать словарь", e);
        }
    }
}
