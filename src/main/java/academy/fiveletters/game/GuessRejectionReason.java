package academy.fiveletters.game;

public enum GuessRejectionReason {
    INVALID_LENGTH("Введите слово из пяти букв."),
    INVALID_CHARACTERS("Допустимы только русские буквы без ё."),
    WORD_NOT_IN_DICTIONARY("Такого слова нет в словаре."),
    GAME_FINISHED("Партия уже завершена.");

    private final String message;

    GuessRejectionReason(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
