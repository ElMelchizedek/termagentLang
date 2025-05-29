import java.util.*;

class GrammarSeed {
    public enum NounCaseType { NOM, ACC, GEN, DAT }
    public record NounCase(NounCaseType type, ArrayList<String> phonemes) {}
    public enum VerbTenseType { PRESENT, PAST }
    public record VerbTense(VerbTenseType type, ArrayList<String> phonemes) {}
    public enum WordOrder { SOV, SVO, VSO, VOS, OSV, OVS }
    public enum PronounType {FIRST, SECOND, THIRD}

    @FunctionalInterface
    public interface StructureRecord<MorphemeType, Phonemes, Morpheme> {
        Morpheme apply(MorphemeType type, Phonemes phonemes);
    }

    public static <Type, Morpheme> Map<Type, Morpheme> generateStructure(
            Vocabulary vocabulary, Random random, Type[] possible_morphemes, ArrayList<ArrayList<VocabularySeed.Sound>> phonemes_template,
            StructureRecord<Type, ArrayList<String>, Morpheme> morpheme_factory) {

        List<VocabularySeed.Consonant> consonant_values = vocabulary.getConsonantsValues();
        ArrayList<String> vowels = vocabulary.getVowels();

        Map<Type, Morpheme> structure = new HashMap<>();
        for (Type type : possible_morphemes) {
            ArrayList<String> filled_phonemes_list = new ArrayList<>();

            for (ArrayList<VocabularySeed.Sound> template: phonemes_template) {
                String filled_phoneme_string = "";

                for (VocabularySeed.Sound sound : template) {
                    String singular_phoneme_string = "";

                    if (sound == VocabularySeed.Sound.C) {
                        VocabularySeed.Consonant consonant = consonant_values.get(random.nextInt(consonant_values.size()));
                        singular_phoneme_string = consonant.symbol();
                    } else if (sound == VocabularySeed.Sound.V) {
                        String vowel = vowels.get(random.nextInt(vowels.size()));
                        singular_phoneme_string = vowel;
                    }

                    if (!singular_phoneme_string.isEmpty()) filled_phoneme_string = filled_phoneme_string.concat(singular_phoneme_string);

                }

                if (!filled_phoneme_string.isEmpty()) filled_phonemes_list.add(filled_phoneme_string);
            }

            Morpheme new_morpheme = morpheme_factory.apply(type, filled_phonemes_list);

            structure.put(type, new_morpheme);
        }

        return structure;
    }

    public static Map<NounCaseType, NounCase> generateNounCases(Vocabulary vocabulary, Random random) {
        NounCaseType[] possible_noun_cases = {NounCaseType.NOM, NounCaseType.ACC, NounCaseType.GEN, NounCaseType.DAT};

        ArrayList<VocabularySeed.Sound> ending_vowel_prefixed = new ArrayList<>(List.of(
                VocabularySeed.Sound.C
        ));
        ArrayList<VocabularySeed.Sound> ending_consonant_prefixed = new ArrayList<>(Arrays.asList(
                VocabularySeed.Sound.V, VocabularySeed.Sound.C
        ));
        ArrayList<ArrayList<VocabularySeed.Sound>> desired_phonemes = new ArrayList<>(Arrays.asList(
                ending_vowel_prefixed, ending_consonant_prefixed
        ));

        return generateStructure(vocabulary, random, possible_noun_cases, desired_phonemes, NounCase::new);
    }

    public static Map<VerbTenseType, VerbTense> generateVerbTenses(Vocabulary vocabulary, Random random) {
        VerbTenseType[] possible_verb_tenses = {VerbTenseType.PRESENT, VerbTenseType.PAST};

        ArrayList<VocabularySeed.Sound> ending_vowel_prefixed = new ArrayList<>(List.of(
                VocabularySeed.Sound.C
        ));
        ArrayList<VocabularySeed.Sound> ending_consonant_prefixed = new ArrayList<>(Arrays.asList(
                VocabularySeed.Sound.V, VocabularySeed.Sound.C
        ));
        ArrayList<ArrayList<VocabularySeed.Sound>> desired_phonemes = new ArrayList<>(Arrays.asList(
                ending_vowel_prefixed, ending_consonant_prefixed
        ));


        return generateStructure(vocabulary, random, possible_verb_tenses, desired_phonemes, VerbTense::new);
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
                        noun_case.phonemes().get(0) + "/" +
                        noun_case.phonemes().get(1));
            }

            System.out.println("Verb Tenses: ");
            Collection<VerbTense> verb_tenses_values = verb_tenses.values();
            for (VerbTense tense: verb_tenses_values) {
                System.out.println(
                        "\t*" +
                        tense.type().toString() + ": " +
                        tense.phonemes().get(0) + "/" +
                        tense.phonemes().get(1));
            }

            System.out.println("Word Order: " + word_order.toString());
        }
    }
}