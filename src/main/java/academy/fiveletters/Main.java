package academy.fiveletters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Точка входа в игру «5 букв». */
public final class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() {}

    public static void main(String[] args) {
        LOG.debug("Аргументы запуска: {}", String.join(" ", args));

        // TODO: запустить игру.
        System.out.println("«5 букв» — шаблон проекта Т-Академии.");
        System.out.println("Игра ещё не реализована. Начни с README.");
    }
}
