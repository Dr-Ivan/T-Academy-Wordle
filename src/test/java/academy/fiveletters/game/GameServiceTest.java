package academy.fiveletters.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import academy.fiveletters.dictionary.DictionaryLoader;
import academy.fiveletters.dictionary.WordDictionary;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class GameServiceTest {

    private final WordDictionary dictionary = new DictionaryLoader().loadResource("/dictionary.txt");
    private final GameService service = new GameService();

    @Test
    void startsGameWithExactlyFiftyWords() {
        var smallDictionary = new WordDictionary(dictionary.words().subList(0, 50));

        var session = service.startGame(smallDictionary, 6, 42L);

        assertThat(session.answer()).isIn(smallDictionary.words());
        assertThat(session.maxAttempts()).isEqualTo(6);
        assertThat(session.attemptsUsed()).isZero();
        assertThat(session.attemptsHistory()).isEmpty();
        assertThat(session.status()).isEqualTo(GameStatus.IN_PROGRESS);
    }

    @Test
    void rejectsDictionaryWithFortyNineWords() {
        var smallDictionary = new WordDictionary(dictionary.words().subList(0, 49));

        assertThatIllegalArgumentException()
                .isThrownBy(() -> service.startGame(smallDictionary, 6, 42L))
                .withMessageContaining("50")
                .withMessageContaining("49");
    }

    @Test
    void duplicateEntriesDoNotSatisfyMinimumSize() {
        var words = new ArrayList<>(dictionary.words().subList(0, 49));
        words.add(words.getFirst());
        var smallDictionary = new WordDictionary(words);

        assertThatIllegalArgumentException().isThrownBy(() -> service.startGame(smallDictionary, 6, 42L));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void rejectsNonPositiveAttemptLimit(int maxAttempts) {
        assertThatIllegalArgumentException().isThrownBy(() -> service.startGame(dictionary, maxAttempts, 42L));
    }

    @Test
    void preservesCustomAttemptLimit() {
        var session = service.startGame(dictionary, 3, 42L);
        assertThat(session.maxAttempts()).isEqualTo(3);
    }

    @Test
    void inputWordOrderDoesNotAffectAnswer() {
        var reversedWords = new ArrayList<>(dictionary.words());
        Collections.reverse(reversedWords);
        var reorderedDictionary = new WordDictionary(reversedWords);

        var first = service.startGame(dictionary, 6, 42L);
        var second = service.startGame(reorderedDictionary, 6, 42L);

        assertThat(second.answer()).isEqualTo(first.answer());
    }

    @Test
    void createsIndependentSessions() {
        var first = service.startGame(dictionary, 6, 42L);
        var second = service.startGame(dictionary, 6, 42L);

        first.recordGuess(first.answer());

        assertThat(second).isNotSameAs(first);
        assertThat(first.status()).isEqualTo(GameStatus.WIN);
        assertThat(second.status()).isEqualTo(GameStatus.IN_PROGRESS);
        assertThat(second.attemptsUsed()).isZero();
        assertThat(second.attemptsHistory()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(longs = {1L, 0L, -1L, Long.MIN_VALUE, Long.MAX_VALUE})
    void acceptsAnyLongSeed(long seed) {
        var first = service.startGame(dictionary, 6, seed);
        var second = service.startGame(dictionary, 6, seed);

        assertThat(first.answer()).isIn(dictionary.words());
        assertThat(second.answer()).isEqualTo(first.answer());
    }
}
