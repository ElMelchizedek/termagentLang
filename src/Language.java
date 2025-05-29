import java.util.Random;

interface InterfaceLanguage {
    enum Sound { V, C }
    record Consonant(String symbol, VocabularySeed.Manner manner, boolean voiced) {}
}

public class Language {
    private Vocabulary vocabulary;
    private Grammar grammar;

    public Language(boolean debug) {
        Random random = new Random();
        this.vocabulary = new Vocabulary(debug, random);
        this.grammar = new Grammar(debug, random, vocabulary);
    }

    public static void main(String[] args) {
        System.out.println("*** STARTER VOCABULARY ***");
        Language language = new Language(true);
    }
}
