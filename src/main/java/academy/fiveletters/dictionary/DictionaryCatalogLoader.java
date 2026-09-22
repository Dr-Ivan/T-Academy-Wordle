package academy.fiveletters.dictionary;

public final class DictionaryCatalogLoader {

    public DictionaryCatalog load() {
        var loader = new DictionaryLoader();

        WordDictionary standard = loader.loadResource("/dictionary.txt");
        WordDictionary easy = loader.loadResource("/dictionaries/easy.txt");
        WordDictionary nature = loader.loadResource("/dictionaries/nature.txt");
        WordDictionary everyday = loader.loadResource("/dictionaries/everyday.txt");

        return new DictionaryCatalog(standard, easy, nature, everyday);
    }
}
