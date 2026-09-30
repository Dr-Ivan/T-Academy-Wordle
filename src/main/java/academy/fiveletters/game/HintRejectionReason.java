package academy.fiveletters.game;

public enum HintRejectionReason {
    ALREADY_USED("Подсказка уже использована."),
    GAME_FINISHED("Партия уже завершена."),
    NO_HIDDEN_POSITIONS("Все позиции уже раскрыты предыдущими попытками.");

    private final String message;

    HintRejectionReason(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
