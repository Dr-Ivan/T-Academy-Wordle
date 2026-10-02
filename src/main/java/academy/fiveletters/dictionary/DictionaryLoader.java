package academy.fiveletters.dictionary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

/** Загружает словарь из ресурсов. */
public final class DictionaryLoader {

    /**
     * Загружает словарь по абсолютному пути ресурса, например /dictionary.txt.
     *
     * <p>Пустые строки и строки, начинающиеся после удаления внешних пробелов с символа #, пропускаются.
     */
    public WordDictionary loadResource(String resourcePath) {
        Objects.requireNonNull(resourcePath, "Путь ресурса не должен быть null");
        if (!resourcePath.startsWith("/")) {
            throw new IllegalArgumentException("Путь ресурса должен начинаться с /: " + resourcePath);
        }

        InputStream stream = DictionaryLoader.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            throw new IllegalArgumentException("Ресурс словаря не найден: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            ArrayList<String> words = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.strip();
                if (!word.isEmpty() && !word.startsWith("#")) {
                    words.add(word);
                }
            }

            return new WordDictionary(words);

        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать словарь: " + resourcePath, e);
        }
    }
}
