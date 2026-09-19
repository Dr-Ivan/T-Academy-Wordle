package academy.fiveletters.replay;

import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GameSession;
import academy.fiveletters.game.GuessResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ReplayService {

    private final GameService gameService;

    public ReplayService(GameService gameService) {
        this.gameService = Objects.requireNonNull(gameService, "Игровой сервис не должен быть null");
    }

    public ReplayResult replay(WordDictionary dictionary, int maxAttempts, long seed, List<String> guesses) {
        Objects.requireNonNull(guesses, "Сценарий не должен быть null");
        List<String> inputs = List.copyOf(guesses);

        GameSession session = gameService.startGame(dictionary, maxAttempts, seed);
        List<ReplayStep> steps = new ArrayList<>();

        for (String input : inputs) {
            GuessResult result = gameService.applyGuess(session, input);
            steps.add(new ReplayStep(input, result));
        }

        return new ReplayResult(session, steps);
    }
}
