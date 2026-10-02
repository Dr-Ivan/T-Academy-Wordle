package academy.fiveletters.cli;

import academy.fiveletters.settings.GameSettings;
import java.util.Objects;

/** Игровые настройки и оформление, выбранные в консольном меню. */
record ConsolePreferences(GameSettings gameSettings, ColorMode colorMode) {

    ConsolePreferences {
        Objects.requireNonNull(gameSettings, "Настройки игры не должны быть null");
        Objects.requireNonNull(colorMode, "Режим цвета не должен быть null");
    }

    ConsolePreferences withGameSettings(GameSettings settings) {
        return new ConsolePreferences(settings, colorMode);
    }

    ConsolePreferences withColorMode(ColorMode mode) {
        return new ConsolePreferences(gameSettings, mode);
    }
}
