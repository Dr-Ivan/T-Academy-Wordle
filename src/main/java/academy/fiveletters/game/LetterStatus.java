package academy.fiveletters.game;

/** Результат сопоставления одной буквы попытки с ответом. */
public enum LetterStatus {
    /** Буква совпала с ответом на той же позиции. */
    EXACT("✅", "\u001B[32m"),

    /** Буква совпала с ещё не использованной буквой на другой позиции. */
    PRESENT("🟡", "\u001B[33m"),

    /** Для буквы не осталось совпадений в ответе. */
    ABSENT("❌", "\u001B[90m");

    private final String symbol;
    private final String ansiColor;

    LetterStatus(String symbol, String ansiColor) {
        this.symbol = symbol;
        this.ansiColor = ansiColor;
    }

    public String symbol() {
        return symbol;
    }

    public String ansiColor() {
        return ansiColor;
    }
}
