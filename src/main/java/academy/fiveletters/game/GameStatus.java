package academy.fiveletters.game;

public enum GameStatus {
    IN_PROGRESS("Партия не завершена."),
    WIN("Победа! Слово угадано за %1$d %3$s"),
    LOSE("Неудача. Загаданное слово: %2$s");

    private final String messageTemplate;

    GameStatus(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String messageTemplate() {
        return messageTemplate;
    }
}
