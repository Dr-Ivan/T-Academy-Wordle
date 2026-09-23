package academy.fiveletters.cli;

import academy.fiveletters.settings.GameSettings;
import java.util.List;
import java.util.Objects;

public sealed interface LaunchOptions permits LaunchOptions.Menu, LaunchOptions.Play, LaunchOptions.Replay {

    record Menu(long seed, GameSettings settings) implements LaunchOptions {

        public Menu {
            Objects.requireNonNull(settings, "Настройки не должны быть null");
        }
    }

    record Play(long seed, GameSettings settings) implements LaunchOptions {

        public Play {
            Objects.requireNonNull(settings, "Настройки не должны быть null");
        }
    }

    record Replay(long seed, GameSettings settings, List<String> guesses) implements LaunchOptions {

        public Replay {
            Objects.requireNonNull(settings, "Настройки не должны быть null");
            Objects.requireNonNull(guesses, "Сценарий не должен быть null");
            guesses = List.copyOf(guesses);
        }
    }
}
