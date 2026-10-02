package academy.fiveletters.game;

/** Результат действия игрока: попытки или запроса подсказки. */
public sealed interface GameActionResult permits GuessResult, HintResult {}
