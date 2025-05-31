import java.util.*;

class GrammarSeed implements InterfaceLanguage {

    public sealed interface Feature<T> permits NounCase, VerbTense {
        ArrayList<String> phonemes();
    }

    public enum NounCaseType { NOM, ACC, GEN, DAT }
    public record NounCase(NounCaseType type, ArrayList<String> phonemes)
        implements Feature<NounCaseType> {}

    public enum VerbTenseType { PRESENT, PAST }
    public record VerbTense(VerbTenseType type, ArrayList<String> phonemes)
        implements Feature<VerbTenseType> {}

    public enum WordOrder { SOV, SVO, VSO, VOS, OSV, OVS }

    public enum PronounType {FIRST, SECOND, THIRD}
    public record Pronoun(PronounType type, String phoneme) {}

    @FunctionalInterface
    private interface StructureRecord<MorphemeType, Phonemes, Morpheme> {
        Morpheme apply(MorphemeType type, Phonemes phonemes);
    }

    private static <Type, Morpheme> Map<Type, Morpheme> generateStructure(
            Vocabulary vocabulary, Random random, Type[] possible_morphemes, ArrayList<ArrayList<Sound>> phonemes_template,
            StructureRecord<Type, ArrayList<String>, Morpheme> morpheme_factory) {

        List<Consonant> consonant_values = vocabulary.getConsonantsValues();
        ArrayList<String> vowels = vocabulary.getVowels();

        Map<Type, Morpheme> structure = new HashMap<>();
        for (Type type : possible_morphemes) {
            ArrayList<String> filled_phonemes_list = new ArrayList<>();

            for (ArrayList<Sound> template: phonemes_template) {
                String filled_phoneme_string = "";

                for (Sound sound : template) {
                    String singular_phoneme_string = "";

                    if (sound == Sound.C) {
                        Consonant consonant = consonant_values.get(random.nextInt(consonant_values.size()));
                        singular_phoneme_string = consonant.symbol();
                    } else if (sound == Sound.V) {
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

    public static <T extends Feature> Feature fixFeature(Feature feature) {
        String vowel_prefixed = feature.phonemes().getFirst().toString();
        char symbol_vowel_prefixed = vowel_prefixed.charAt(0);
        String consonant_prefixed = feature.phonemes().getLast().toString();

        String fixed_consonant_prefixed = consonant_prefixed.substring(0, consonant_prefixed.length() - 1) + symbol_vowel_prefixed;
        ArrayList<String> new_phonemes = new ArrayList<>();
        new_phonemes.add(vowel_prefixed);
        new_phonemes.add(fixed_consonant_prefixed);

       // I could do this better and more type-safety, but idgaf tbh.
       if (feature instanceof NounCase noun_case) {
           return new NounCase(noun_case.type(), new_phonemes);
       } else if (feature instanceof VerbTense verb_tense) {
           return new VerbTense(verb_tense.type(), new_phonemes);
       }

       return null;
    }


    public static Map<NounCaseType, NounCase> generateNounCases(Vocabulary vocabulary, Random random) {
        NounCaseType[] possible_noun_cases = {NounCaseType.NOM, NounCaseType.ACC, NounCaseType.GEN, NounCaseType.DAT};

        ArrayList<Sound> ending_vowel_prefixed = new ArrayList<>(List.of(
                Sound.C
        ));
        ArrayList<Sound> ending_consonant_prefixed = new ArrayList<>(Arrays.asList(
                Sound.V, Sound.C
        ));
        ArrayList<ArrayList<Sound>> desired_phonemes = new ArrayList<>(Arrays.asList(
                ending_vowel_prefixed, ending_consonant_prefixed
        ));

        Map<NounCaseType, NounCase> raw_noun_cases = generateStructure(vocabulary, random, possible_noun_cases, desired_phonemes, NounCase::new);
        Collection<NounCase> noun_cases_values = raw_noun_cases.values();
        Map<NounCaseType, NounCase> corrected_noun_cases = new HashMap<>();

        int iterator_possible_noun_cases = 0;
        for (NounCase noun_case : noun_cases_values) {
            NounCase new_noun_case = (NounCase) fixFeature(noun_case);
            corrected_noun_cases.put(possible_noun_cases[iterator_possible_noun_cases], new_noun_case);
            iterator_possible_noun_cases++;
        }

        return corrected_noun_cases;
    }

    public static Map<VerbTenseType, VerbTense> generateVerbTenses(Vocabulary vocabulary, Random random) {
        VerbTenseType[] possible_verb_tenses = {VerbTenseType.PRESENT, VerbTenseType.PAST};

        ArrayList<Sound> ending_vowel_prefixed = new ArrayList<>(List.of(
                Sound.C
        ));
        ArrayList<Sound> ending_consonant_prefixed = new ArrayList<>(Arrays.asList(
                Sound.V, Sound.C
        ));
        ArrayList<ArrayList<Sound>> desired_phonemes = new ArrayList<>(Arrays.asList(
                ending_vowel_prefixed, ending_consonant_prefixed
        ));

        Map<VerbTenseType, VerbTense> raw_verb_tenses = generateStructure(vocabulary, random, possible_verb_tenses, desired_phonemes, VerbTense::new);
        Collection<VerbTense> verb_tenses_values = raw_verb_tenses.values();
        Map<VerbTenseType, VerbTense> corrected_verb_tenses = new HashMap<>();

        int iterator_possible_verb_tenses = 0;
        for (VerbTense tense : verb_tenses_values) {
            VerbTense new_verb_tense = (VerbTense) fixFeature(tense);
            corrected_verb_tenses.put(possible_verb_tenses[iterator_possible_verb_tenses], new_verb_tense);
            iterator_possible_verb_tenses++;
        }

        return corrected_verb_tenses;

    }

    public static WordOrder generateWordOrder(Random random) {
        return WordOrder.values()[random.nextInt(WordOrder.values().length)];
    }

    public static Map<PronounType, Pronoun> generatePronouns(Random random, Vocabulary vocabulary) {
        PronounType[] possible_pronoun_types = {PronounType.FIRST, PronounType.SECOND, PronounType.THIRD};
        Map<PronounType, Pronoun> pronouns = new HashMap<>();

        for (PronounType type : possible_pronoun_types) {
            String new_word = vocabulary.generateWord(random);
            Pronoun new_pronoun = new Pronoun(type, new_word);
            pronouns.put(type, new_pronoun);
        }

        return pronouns;
    }
}

public class Grammar extends GrammarSeed {
    private final Map<NounCaseType, NounCase> noun_cases;
    private final Map<VerbTenseType, VerbTense> verb_tenses;
    private final WordOrder word_order;
    private final Map<PronounType, Pronoun> pronouns;

    public Grammar(boolean debug, Random random, Vocabulary vocabulary) {
        this.noun_cases = generateNounCases(vocabulary, random);
        this.verb_tenses = generateVerbTenses(vocabulary, random);
        this.word_order = generateWordOrder(random);
        this.pronouns = generatePronouns(random, vocabulary);

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

    public Map<NounCaseType, NounCase> getNounCases() {
        return noun_cases;
    }

    public Map<VerbTenseType, VerbTense> getVerbTenses() {
        return verb_tenses;
    }

    public WordOrder getWordOrder() {
        return word_order;
    }

    public Map<PronounType, Pronoun> getPronouns() {
        return pronouns;
    }
}