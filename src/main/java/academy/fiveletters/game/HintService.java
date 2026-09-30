package academy.fiveletters.game;

import academy.fiveletters.word.WordRules;
import java.util.Objects;

/** Выдаёт одну дополнительную подсказку за партию. */
public final class HintService {

    public HintResult requestHint(GameSession session) {
        Objects.requireNonNull(session, "Сессия не должна быть null");
        if (session.status() != GameStatus.IN_PROGRESS) {
            return new HintResult.Rejected(HintRejectionReason.GAME_FINISHED);
        }
        if (session.hintUsed()) {
            return new HintResult.Rejected(HintRejectionReason.ALREADY_USED);
        }
        String answer = session.answer();
        boolean[] revealedPositions = new boolean[WordRules.length()];

        for (String guess : session.attemptsHistory()) {
            for (int index = 0; index < revealedPositions.length; index++) {
                if (guess.charAt(index) == answer.charAt(index)) {
                    revealedPositions[index] = true;
                }
            }
        }

        for (int index = 0; index < revealedPositions.length; index++) {
            if (!revealedPositions[index]) {
                var result = new HintResult.Revealed(index + 1, answer.charAt(index));
                session.markHintUsed();
                return result;
            }
        }
        return new HintResult.Rejected(HintRejectionReason.NO_HIDDEN_POSITIONS);
    }
}
