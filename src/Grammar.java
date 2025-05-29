import java.util.*;

class GrammarSeed {
    public enum NounCaseType { NOM, ACC, GEN, DAT }
    public record NounCase(NounCaseType type, String vowel_prefixed, String consonant_prefixed) {}
    public enum VerbTenseType { PRESENT, PAST }
    public record VerbTense(VerbTenseType type, String vowel_prefixed, String consonant_prefixed) {}
    public enum WordOrder { SOV, SVO, VSO, VOS, OSV, OVS }

    @FunctionalInterface
    public interface StructureRecord<MorphemeType, VowelPrefixed, ConsonantPrefixed, Morpheme> {
        Morpheme apply(MorphemeType type, VowelPrefixed vowel_prefixed, ConsonantPrefixed consonant_prefixed);
    }

    public static <Type, Morpheme> Map<Type, Morpheme> generateStructure(
            Vocabulary vocabulary, Random random, Type[] possible_morphemes,
            StructureRecord<Type, String, String, Morpheme> morpheme_factory) {

        List<VocabularySeed.Consonant> consonant_values = vocabulary.getConsonantsValues();
        ArrayList<String> vowels = vocabulary.getVowels();

        Map<Type, Morpheme> structure = new HashMap<>();
        for (Type type : possible_morphemes) {
            VocabularySeed.Consonant consonant = consonant_values.get(random.nextInt(consonant_values.size()));
            String vowel = vowels.get(random.nextInt(vowels.size()));

            String ending_vowel_prefixed = consonant.symbol();
            String ending_consonant_prefixed = vowel.concat(consonant.symbol());
            Morpheme new_morpheme = morpheme_factory.apply(type, ending_vowel_prefixed, ending_consonant_prefixed);

            structure.put(type, new_morpheme);
        }

        return structure;
    }

    public static Map<NounCaseType, NounCase> generateNounCases(Vocabulary vocabulary, Random random) {
        NounCaseType[] possible_noun_cases = {NounCaseType.NOM, NounCaseType.ACC, NounCaseType.GEN, NounCaseType.DAT};
        return generateStructure(vocabulary, random, possible_noun_cases, NounCase::new);
    }

    public static Map<VerbTenseType, VerbTense> generateVerbTenses(Vocabulary vocabulary, Random random) {
        VerbTenseType[] possible_verb_tenses = {VerbTenseType.PRESENT, VerbTenseType.PAST};
        return generateStructure(vocabulary, random, possible_verb_tenses, VerbTense::new);
    }

    public static WordOrder generateWordOrder(Random random) {
        return WordOrder.values()[random.nextInt(WordOrder.values().length)];
    }
}

public class Grammar extends GrammarSeed {
    private Map<NounCaseType, NounCase> noun_cases;
    private Map<VerbTenseType, VerbTense> verb_tenses;
    private WordOrder word_order;

    public Grammar(boolean debug, Random random, Vocabulary vocabulary) {
        this.noun_cases = generateNounCases(vocabulary, random);
        this.verb_tenses = generateVerbTenses(vocabulary, random);
        this.word_order = generateWordOrder(random);

        if (debug) {
            System.out.println("*** GRAMMAR ***");

            Collection<NounCase> noun_cases_values = noun_cases.values();
            System.out.println("Noun Cases: ");
            for (NounCase noun_case : noun_cases_values) {
                System.out.println(
                        "\t*" +
                        noun_case.type().toString() + ": " +
                        noun_case.vowel_prefixed() + "/" +
                        noun_case.consonant_prefixed());
            }

            System.out.println("Verb Tenses: ");
            Collection<VerbTense> verb_tenses_values = verb_tenses.values();
            for (VerbTense tense: verb_tenses_values) {
                System.out.println(
                        "\t*" +
                        tense.type().toString() + ": " +
                        tense.vowel_prefixed() + "/" +
                        tense.consonant_prefixed());
            }

            System.out.println("Word Order: " + word_order.toString());
        }
    }
}