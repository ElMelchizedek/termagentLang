import java.util.Random;

interface InterfaceLanguage {
    enum Sound { V, C }
    record Consonant(String symbol, SeedVocabulary.Manner manner, boolean voiced) {}
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

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    public void setVocabulary(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;
    }

    public Grammar getGrammar() {
        return grammar;
    }

    public void setGrammar(Grammar grammar) {
        this.grammar = grammar;
    }
}
