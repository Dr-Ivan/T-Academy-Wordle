package academy.fiveletters.cli;

public enum ColorMode {
    NEVER("Выключен"),
    ALWAYS("Включён");

    private final String title;

    ColorMode(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
