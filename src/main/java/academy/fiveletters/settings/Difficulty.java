package academy.fiveletters.settings;

public enum Difficulty {
    STANDARD("Обычная"),
    EASY("Лёгкая");

    private final String title;

    Difficulty(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
