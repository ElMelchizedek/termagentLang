import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.ArrayList;

public class Language {
    public enum Sound { V, C }
    public enum Manner {Nasal, Plosive, Affricate, Fricative, Liquid, Glide}

    public record Consonant(Manner manner, boolean voiced) {}

    static public final String[] consonant_symbols = {
            "p", "t", "k", "'", "b", "d", "g", "f", "s", "sh", "th", "h", "v", "z", "zh", "dh", "ch", "ts", "j", "dz",
            "m", "n", "ng", "ny", "hr", "l", "r"
    };
    static public final Map<String, Consonant> possible_consonants = Map.ofEntries(
            Map.entry("p", new Consonant(Manner.Plosive, false)),
            Map.entry("t", new Consonant(Manner.Plosive, false)),
            Map.entry("k", new Consonant(Manner.Plosive, false)),
            Map.entry("'", new Consonant(Manner.Plosive, false)),

            Map.entry("b", new Consonant(Manner.Plosive, true)),
            Map.entry("d", new Consonant(Manner.Plosive, true)),
            Map.entry("g", new Consonant(Manner.Plosive, true)),

            Map.entry("f", new Consonant(Manner.Fricative, false)),
            Map.entry("s", new Consonant(Manner.Fricative, false)),
            Map.entry("sh", new Consonant(Manner.Fricative, false)),
            Map.entry("th", new Consonant(Manner.Fricative, false)),
            Map.entry("h", new Consonant(Manner.Fricative, false)),

            Map.entry("v", new Consonant(Manner.Fricative, true)),
            Map.entry("z", new Consonant(Manner.Fricative, true)),
            Map.entry("zh", new Consonant(Manner.Fricative, true)),
            Map.entry("dh", new Consonant(Manner.Fricative, true)),

            Map.entry("ch", new Consonant(Manner.Affricate, false)),
            Map.entry("ts", new Consonant(Manner.Affricate, false)),

            Map.entry("j", new Consonant(Manner.Affricate, true)),
            Map.entry("dz", new Consonant(Manner.Affricate, true)),

            Map.entry("m", new Consonant(Manner.Nasal, true)),
            Map.entry("n", new Consonant(Manner.Nasal, true)),
            Map.entry("ng", new Consonant(Manner.Nasal, true)),
            Map.entry("ny", new Consonant(Manner.Nasal, true)),

            Map.entry("hr", new Consonant(Manner.Liquid, false)),

            Map.entry("l", new Consonant(Manner.Liquid, true)),
            Map.entry("r", new Consonant(Manner.Liquid, true))
    );

    static public String[] possible_vowels = {
            "a", "e", "i", "o", "u", "ä", "ë", "ï", "ö", "ü", "ai", "ae", "au", "ao", "ea", "ei", "eo", "eu",
            "ia", "ie", "io", "iu", "ua", "ue", "ui", "uo"
    };
    static public Sound[][] templates = {
            {Sound.C, Sound.V}, {Sound.C, Sound.V, Sound.C}, {Sound.V}, {Sound.V, Sound.C}, {Sound.C, Sound.V, Sound.V},
            {Sound.C, Sound.C, Sound.V}, {Sound.C, Sound.V, Sound.V, Sound.C}
    };

    static private final String[] starter_nouns = {
            "water", "fire", "sun", "earth", "man", "woman", "food", "drink", "hand", "self"
    };



    public static boolean isValidSyllable(ArrayList<Object> syllable) {
        ArrayList<Object[]> clustered_syllable = new ArrayList<>();
        Object[] next_cluster = new Object[2];

        for (int i = 0; i < syllable.size(); i++) {
            Object phoneme = syllable.get(i);

            assert next_cluster != null;
            if (next_cluster[0] == null) {
                if (i == syllable.size() - 1) {
                    next_cluster[0] = phoneme;
                    clustered_syllable.add(next_cluster);
                    break;
                } else {
                    next_cluster[0] = phoneme;
                }
            } else if (next_cluster[1] == null) {
                if (i == syllable.size() - 1) {
                    next_cluster[1] = phoneme;
                    clustered_syllable.add(next_cluster);
                    break;
                } else {
                    next_cluster[1] = phoneme;
                }
            }
            else {
                clustered_syllable.add(next_cluster);
                next_cluster = null;
            }
        }

        for (Object[] cluster: clustered_syllable) {
            if (cluster[0] instanceof Consonant && cluster[1] instanceof Consonant) {
                if (
                        ((Consonant) cluster[0]).manner == ((Consonant) cluster[1]).manner &&
                        ((Consonant) cluster[0]).voiced == ((Consonant) cluster[1]).voiced)
                    return false;
                if (
                        ((Consonant) cluster[0]).manner == Manner.Nasal &&
                        ((Consonant) cluster[0]).voiced != ((Consonant) cluster[1]).voiced)
                    return false;
            }
        }

        return true;
    }

    public static String generateWord(Random random,
                                       ArrayList<Sound[]> permitted_templates,
                                       Map<String, Consonant> permitted_consonants,
                                       ArrayList<String> permitted_vowels) {
        ArrayList<ArrayList<Object>> word = new ArrayList<>();
        int target_length = 3;

        while(word.size() < target_length) {
            Sound[] selected_template = permitted_templates.get(random.nextInt(permitted_templates.size()));
            ArrayList<Object> syllable = new ArrayList<>();

            for (Sound sound : selected_template) {
                if (sound == Sound.C) syllable.add(
                        permitted_consonants.get(
                                consonant_symbols[random.nextInt(consonant_symbols.length)]
                        )
                );
                else syllable.add(permitted_vowels.get(random.nextInt(permitted_vowels.size())));
            }

            if (isValidSyllable(syllable)) word.add(syllable);
        }

        return word.toString();
    }

    public static void generateSpecifications(Random random, Map<String, Consonant> permitted_consonants,
                                              ArrayList<String> permitted_vowels, ArrayList<Sound[]> permitted_templates) {

        for (String symbol : consonant_symbols) {
           if (random.nextBoolean()) permitted_consonants.put(symbol, possible_consonants.get(symbol));
        }
        for (String vowel : possible_vowels) {
            if (random.nextBoolean()) permitted_vowels.add(vowel);
        }
        for (Sound[] template: templates) {
            if (random.nextBoolean()) permitted_templates.add(template);
        }

        if (permitted_consonants.isEmpty()) {
            String random_symbol = consonant_symbols[random.nextInt()];
            permitted_consonants.put(random_symbol, possible_consonants.get(random_symbol));
        }
        if (permitted_vowels.isEmpty()) {
            permitted_vowels.add(possible_vowels[random.nextInt(possible_vowels.length)]);
        }
        if (permitted_templates.isEmpty()) {
            permitted_templates.add(templates[random.nextInt(templates.length)]);
        }
    }

    public static void main(String[] args) {
        Random random = new Random();

        Map<String, Consonant> permitted_constants = new HashMap<>();
        ArrayList<String> permitted_vowels = new ArrayList<>();
        ArrayList<Sound[]> permitted_templates = new ArrayList<>();
        generateSpecifications(random, permitted_constants, permitted_vowels, permitted_templates);

        String[] vocabulary = new String[starter_nouns.length];
        for (int i = 0; i < starter_nouns.length; i++) {
            String word;
            word = generateWord(random, permitted_templates, permitted_constants, permitted_vowels);
            vocabulary[i] = word;
        }

        System.out.println("*** STARTER VOCABULARY ***");
        for (int i = 0; i < vocabulary.length; i++) {
            System.out.println(starter_nouns[i] + ": " + vocabulary[i]);
        }

    }
}
