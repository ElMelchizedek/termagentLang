import java.util.*;

public class Language {
    public enum Sound { V, C }
    public enum Manner {Nasal, Plosive, Affricate, Fricative, Liquid, Glide}

    public record Consonant(String symbol, Manner manner, boolean voiced) {}

    static public final String[] consonant_symbols = {
            "p", "t", "k", "b", "d", "g", "f", "s", "sh", "th", "h", "v", "z", "zh", "dh", "ch", "ts", "j", "dz",
            "m", "n", "ng", "ny", "hr", "l", "r"
    };
    static public final Map<String, Consonant> possible_consonants = Map.ofEntries(
            Map.entry("p", new Consonant("p", Manner.Plosive, false)),
            Map.entry("t", new Consonant("t", Manner.Plosive, false)),
            Map.entry("k", new Consonant("k", Manner.Plosive, false)),

            Map.entry("b", new Consonant("b", Manner.Plosive, true)),
            Map.entry("d", new Consonant("d", Manner.Plosive, true)),
            Map.entry("g", new Consonant("g", Manner.Plosive, true)),

            Map.entry("f", new Consonant("f", Manner.Fricative, false)),
            Map.entry("s", new Consonant("s", Manner.Fricative, false)),
            Map.entry("sh", new Consonant("š", Manner.Fricative, false)),
            Map.entry("th", new Consonant("þ", Manner.Fricative, false)),
            Map.entry("h", new Consonant("h", Manner.Fricative, false)),

            Map.entry("v", new Consonant("v", Manner.Fricative, true)),
            Map.entry("z", new Consonant("z", Manner.Fricative, true)),
            Map.entry("zh", new Consonant("ž", Manner.Fricative, true)),
            Map.entry("dh", new Consonant("ð", Manner.Fricative, true)),

            Map.entry("ch", new Consonant("č", Manner.Affricate, false)),
            Map.entry("ts", new Consonant("ʃ", Manner.Affricate, false)),

            Map.entry("j", new Consonant("j", Manner.Affricate, true)),
            Map.entry("dz", new Consonant("ʒ", Manner.Affricate, true)),

            Map.entry("m", new Consonant("m", Manner.Nasal, true)),
            Map.entry("n", new Consonant("n", Manner.Nasal, true)),
            Map.entry("ng", new Consonant("ŋ", Manner.Nasal, true)),
            Map.entry("ny", new Consonant("ñ", Manner.Nasal, true)),

            Map.entry("hr", new Consonant("ȟ", Manner.Liquid, false)),

            Map.entry("l", new Consonant("l", Manner.Liquid, true)),
            Map.entry("r", new Consonant("r", Manner.Liquid, true))
    );

    static public String[] possible_vowels = {
            "a", "e", "i", "o", "u", "ä", "ë", "ï", "ö", "ü", "ai", "ae", "au", "ao", "ea", "ei", "eo", "eu",
            "ia", "ie", "io", "iu", "ua", "ue", "ui", "uo"
    };
    static public String[] long_vowels = {
            "ä", "ë", "ï", "ö", "ü",
    };

    static public Sound[][] templates = {
            {Sound.C, Sound.V}, {Sound.C, Sound.V, Sound.C}, {Sound.V}, {Sound.V, Sound.C}, {Sound.C, Sound.V, Sound.V},
            {Sound.C, Sound.C, Sound.V}, {Sound.C, Sound.V, Sound.V, Sound.C}
    };

    static private final String[] starter_nouns = {
            "sun", "moon", "star", "sky", "cloud", "water", "fire", "earth", "stone", "mountain", "river", "rain",
            "wind", "ice", "tree", "leaf", "fruit", "seed", "head", "hand", "foot", "eye", "ear", "mouth", "bone",
            "blood", "heart", "hair", "man", "woman", "child", "mother", "father", "brother", "sister", "tribe",
            "name", "voice", "dog", "wolf", "fish", "bird", "snake", "bear", "deer", "worm", "knife", "spear", "house",
            "path", "bow", "nest", "food", "drink", "god", "spirit", "dream"
    };

    public static boolean isValidWord(ArrayList<Object> word) {
        for (int i = 0; i < (word.size() - 1); i++) {
            if ( (word.get(i) instanceof Consonant phonemeA) && (word.get(i + 1) instanceof Consonant phonemeB) ) {

                // + Universal Bans
                // No same voice and manner.
                if (phonemeA.manner == phonemeB.manner) return false;
                // No nasal and opposing voice.
                if ( (phonemeA.manner == Manner.Nasal) && (phonemeA.voiced != phonemeB.voiced)) return false;
                // No glottal and consonant.
                if (phonemeA.symbol.equals("'")) return false;

                // + Voicing-based restrictions.
                if ( (!phonemeA.voiced) && (phonemeB.voiced) ) {
                    if ( (phonemeA.manner == Manner.Plosive) && (phonemeB.manner == Manner.Nasal) ) return false;
                    if ( (phonemeA.manner == Manner.Affricate) && (phonemeB.manner == Manner.Liquid)) return false;
                }
            } else if ( (word.get(i) instanceof String phonemeA) && (word.get(i + 1) instanceof String phonemeB)) {

                // No chain of diphthongs.
                if ( (phonemeA.length() > 1) || (phonemeB.length() > 1) ) return false;
                // No chain of long vowels.
                if ( (Arrays.asList(long_vowels).contains(phonemeA)) && (Arrays.asList(long_vowels).contains(phonemeB))) return false;
                // No chain of short and long vowel.
                if ( ( (Arrays.asList(long_vowels).contains(phonemeA)) && !(Arrays.asList(long_vowels).contains(phonemeB)) ) ||
                    ( !(Arrays.asList(long_vowels).contains(phonemeA)) && (Arrays.asList(long_vowels).contains(phonemeB)))) return false;
            }
        }

        return true;
    }

    public static String generateWord(  Random random,
                                        ArrayList<Sound[]> permitted_templates,
                                        Map<String, Consonant> permitted_consonants,
                                        ArrayList<String> permitted_vowels,
                                        String[] vocabulary) {
        String realWord = "";
        ArrayList<Object> word;

        while (true) {
            word = new ArrayList<>();
            int target_length = 1;

            while (word.size() < target_length) {
                Sound[] selected_template = permitted_templates.get(random.nextInt(permitted_templates.size()));
                ArrayList<Object> syllable = new ArrayList<>();

                for (Sound sound : selected_template) {
                    List<Consonant> values = new ArrayList<>(permitted_consonants.values());
                    Consonant random_consonant = values.get(random.nextInt(values.size()));
                    String random_vowel = permitted_vowels.get(random.nextInt(permitted_vowels.size()));
                    if (sound == Sound.C) syllable.add(random_consonant);
                    else syllable.add(random_vowel);
                }

                word.addAll(syllable);
            }
            if (isValidWord(word)) break;

            for (Object phoneme : word) {
                if (phoneme instanceof Consonant) {
                    realWord = realWord.concat(((Consonant) phoneme).symbol);
                } else if (phoneme instanceof String) {
                    realWord = realWord.concat((String) phoneme);
                }
            }

            if (Arrays.asList(vocabulary).contains(realWord)) continue;
            if (!realWord.isEmpty()) break;
        }



        return realWord;
    }

    public static void generateSpecifications(Random random, Map<String, Consonant> permitted_consonants,
                                              ArrayList<String> permitted_vowels, ArrayList<Sound[]> permitted_templates) {

        for (String symbol : consonant_symbols) {
           if (random.nextBoolean()) permitted_consonants.put(symbol, possible_consonants.get(symbol));
        }
        for (String vowel : possible_vowels) {
            if (random.nextBoolean() && random.nextBoolean()) permitted_vowels.add(vowel);
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

    public static boolean generateInitialVocabulary() {
        Random random = new Random();

        Map<String, Consonant> permitted_constants = new HashMap<>();
        ArrayList<String> permitted_vowels = new ArrayList<>();
        ArrayList<Sound[]> permitted_templates = new ArrayList<>();
        generateSpecifications(random, permitted_constants, permitted_vowels, permitted_templates);


        String[] vocabulary = new String[starter_nouns.length];
        for (int i = 0; i < starter_nouns.length; i++) {
            String word;
            word = generateWord(random, permitted_templates, permitted_constants, permitted_vowels, vocabulary);
            vocabulary[i] = word;
            if (vocabulary[i].isEmpty()) return false;
        }

        System.out.print("Permitted consonants: ");
        for (Consonant phoneme : permitted_constants.values()) System.out.print(phoneme.symbol + ",");
        System.out.println();
        System.out.print("Pemitted vowels: ");
        for (String vowel: permitted_vowels) System.out.print(vowel + ",");
        System.out.println();
        System.out.print("Permitted templates: ");
        for (Sound[] template : permitted_templates) {
            System.out.print("[");
            for (Sound sound : template) {
                if (sound == Sound.C) System.out.print("C");
                else if (sound == Sound.V) System.out.print("V");
            }
            System.out.print("],");
        }

        System.out.println();
        for (int i = 0; i < vocabulary.length; i++) {
            System.out.println(starter_nouns[i] + ": " + vocabulary[i]);
        }
        return true;

    }

    public static void main(String[] args) {
        System.out.println("*** STARTER VOCABULARY ***");
        boolean go;
        do {
            go = generateInitialVocabulary();
        } while (!go);

    }
}
