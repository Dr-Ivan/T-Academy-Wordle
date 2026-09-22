package academy.fiveletters.cli;

import academy.fiveletters.settings.Difficulty;
import academy.fiveletters.settings.GameSettings;
import academy.fiveletters.settings.WordCategory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.Optional;

/** Редактирует настройки следующей партии. */
final class ConsoleSettingsMenu {

    private final BufferedReader input;
    private final PrintWriter output;

    ConsoleSettingsMenu(BufferedReader input, PrintWriter output) {
        this.input = Objects.requireNonNull(input, "Ввод не должен быть null");
        this.output = Objects.requireNonNull(output, "Вывод не должен быть null");
    }

    /** Возвращает сохранённые настройки или пустой результат при конце ввода. */
    Optional<GameSettings> edit(GameSettings initialSettings) throws IOException {
        GameSettings settings = Objects.requireNonNull(initialSettings, "Настройки не должны быть null");

        while (true) {
            printMenu(settings);
            String choice = input.readLine();
            if (choice == null) {
                return Optional.empty();
            }
            String normalizedChoice = choice.strip();

            if ("0".equals(normalizedChoice)) {
                return Optional.of(settings);
            }
            settings = switch (normalizedChoice) {
                case "1" -> new GameSettings(Difficulty.STANDARD, settings.category());
                case "2" -> new GameSettings(Difficulty.EASY, settings.category());
                case "3" -> new GameSettings(settings.difficulty(), WordCategory.ALL);
                case "4" -> new GameSettings(settings.difficulty(), WordCategory.NATURE);
                case "5" -> new GameSettings(settings.difficulty(), WordCategory.EVERYDAY);
                default -> {
                    output.println("Неизвестный пункт настроек. Введите число от 0 до 5.");
                    yield settings;
                }
            };
        }
    }

    private void printMenu(GameSettings settings) {
        output.println("Настройки");
        output.println("Сложность: " + settings.difficulty().title());
        output.println("Категория: " + settings.category().title());
        output.println("1. Сложность: " + Difficulty.STANDARD.title());
        output.println("2. Сложность: " + Difficulty.EASY.title());
        output.println("3. Категория: " + WordCategory.ALL.title());
        output.println("4. Категория: " + WordCategory.NATURE.title());
        output.println("5. Категория: " + WordCategory.EVERYDAY.title());
        output.println("0. Сохранить и вернуться");
        output.println("Выберите пункт:");
        output.flush();
    }
}
