package academy.fiveletters.game;

import academy.fiveletters.word.WordRules;
import java.util.Objects;

public sealed interface HintResult extends GameActionResult permits HintResult.Revealed, HintResult.Rejected {

    record Revealed(int position, char letter) implements HintResult {

        public Revealed {
            if (position < 1 || position > WordRules.length()) {
                throw new IllegalArgumentException("Позиция подсказки должна быть от 1 до 5");
            }
            if (letter < 'а' || letter > 'я') {
                throw new IllegalArgumentException("Подсказка должна содержать русскую строчную букву без ё");
            }
        }
    }

    record Rejected(HintRejectionReason reason) implements HintResult {

        public Rejected {
            Objects.requireNonNull(reason, "Причина отказа не должна быть null");
        }
    }
}
