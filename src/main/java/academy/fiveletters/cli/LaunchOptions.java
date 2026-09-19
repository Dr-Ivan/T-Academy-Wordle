package academy.fiveletters.cli;

import java.util.List;
import java.util.Objects;

public sealed interface LaunchOptions permits LaunchOptions.Play, LaunchOptions.Replay {

    record Play(long seed) implements LaunchOptions {}

    record Replay(long seed, List<String> guesses) implements LaunchOptions {

        public Replay {
            Objects.requireNonNull(guesses, "Сценарий не должен быть null");
            guesses = List.copyOf(guesses);
        }
    }
}
