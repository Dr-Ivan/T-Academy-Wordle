package academy.fiveletters.game;

import java.util.Objects;

/** Направляет ввод игрока в обработку попытки или подсказки. */
public final class GameActionService {

    private final GameService gameService;
    private final HintService hintService = new HintService();

    public GameActionService(GameService gameService) {
        this.gameService = Objects.requireNonNull(gameService, "Сервис игры не должен быть null");
    }

    public GameActionResult apply(GameSession session, String input) {
        Objects.requireNonNull(session, "Сессия не должна быть null");
        Objects.requireNonNull(input, "Ввод не должен быть null");

        if (":hint".equalsIgnoreCase(input.strip())) {
            return hintService.requestHint(session);
        }

        return gameService.applyGuess(session, input);
    }
}
