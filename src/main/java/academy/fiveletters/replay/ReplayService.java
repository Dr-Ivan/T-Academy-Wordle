package academy.fiveletters.replay;

import academy.fiveletters.dictionary.WordDictionary;
import academy.fiveletters.game.GameActionResult;
import academy.fiveletters.game.GameActionService;
import academy.fiveletters.game.GameService;
import academy.fiveletters.game.GameSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ReplayService {

    private final GameService gameService;
    private final GameActionService actionService;

    public ReplayService(GameService gameService) {
        this.gameService = Objects.requireNonNull(gameService, "Игровой сервис не должен быть null");
        this.actionService = new GameActionService(this.gameService);
    }

    public ReplayResult replay(WordDictionary dictionary, int maxAttempts, long seed, List<String> guesses) {
        Objects.requireNonNull(guesses, "Сценарий не должен быть null");
        List<String> inputs = List.copyOf(guesses);

        GameSession session = gameService.startGame(dictionary, maxAttempts, seed);
        List<ReplayStep> steps = new ArrayList<>();

        for (String input : inputs) {
            GameActionResult result = actionService.apply(session, input);
            steps.add(new ReplayStep(input, result));
        }

        return new ReplayResult(session, steps);
    }
}
