package academy.fiveletters.mandatory.mr1;

import static org.assertj.core.api.Assertions.assertThat;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Обязательные тесты: одинаковый seed обязан давать одинаковое загаданное слово. */
@DisplayName("MR1. Воспроизводимость по seed")
class SeedTest {

    private final WordDictionary dictionary = new DictionaryLoader().loadResource("/dictionary.txt");

    @Test
    @DisplayName("Одинаковый seed даёт одинаковое загаданное слово")
    void sameSeedProducesSameAnswer() {
        var service = new GameService();
        var first = service.startGame(dictionary, 6, 42L);

        for (long seed = 0; seed < 10; seed++) {
            var intermediate = service.startGame(dictionary, 6, seed);
            assertThat(intermediate.answer()).isIn(dictionary.words());
        }

        var repeated = service.startGame(dictionary, 6, 42L);
        var fromAnotherService = new GameService().startGame(dictionary, 6, 42L);

        assertThat(repeated.answer()).isEqualTo(first.answer());
        assertThat(fromAnotherService.answer()).isEqualTo(first.answer());
    }

    @Test
    @DisplayName("Разные seed'ы дают разные слова хотя бы иногда")
    void differentSeedsProduceDifferentAnswers() {
        var service = new GameService();
        Set<String> answers = new HashSet<>();

        for (long seed = 0; seed < 100; seed++) {
            var session = service.startGame(dictionary, 6, seed);
            answers.add(session.answer());
        }

        assertThat(answers.size()).isGreaterThan(1);
    }
}
