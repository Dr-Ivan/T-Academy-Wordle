package academy.fiveletters.cli;

import academy.fiveletters.statistics.StatisticsSnapshot;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Objects;

public final class ConsoleStatisticsPrinter {

    private final PrintWriter output;

    public ConsoleStatisticsPrinter(PrintWriter output) {
        this.output = Objects.requireNonNull(output, "Вывод не должен быть null");
    }

    public void print(StatisticsSnapshot statistics) {
        Objects.requireNonNull(statistics, "Статистика не должна быть null");

        output.println("Статистика за запуск");
        output.println("Начато партий: " + statistics.started());
        output.println("Побед: " + statistics.wins());
        output.println("Поражений: " + statistics.losses());
        output.println("Прервано партий: " + statistics.interrupted());
        output.println("Партий с подсказкой: " + statistics.gamesWithHints());

        output.printf(Locale.ROOT, "Процент побед: %.1f%%%n", statistics.winRate());
        output.printf(Locale.ROOT, "Среднее число попыток при победе: %.1f%n", statistics.averageWinningAttempts());

        output.flush();
    }
}
