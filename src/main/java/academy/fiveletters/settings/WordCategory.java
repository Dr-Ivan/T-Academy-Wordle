package academy.fiveletters.settings;

public enum WordCategory {
    ALL("Все слова"),
    NATURE("Природа"),
    EVERYDAY("Быт");

    private final String title;

    WordCategory(String title) {
        this.title = title;
    }

    public String title() {
        return title;
    }
}
