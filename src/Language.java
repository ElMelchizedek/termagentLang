import java.util.Random;
import java.util.ArrayList;

public class Language {

    public enum Sound { V, C }

    static public String[] possible_consonants = {
            "p", "b", "t", "d", "k", "g", "'", "f", "v", "s", "z", "sh", "zh", "h", "th",
            "dh", "ch", "j", "ts", "dz", "m", "n", "ng", "l", "r", "w", "y", "ny", "hr"
    };
    static public String[] possible_vowels = {
            "a", "e", "i", "o", "u", "aa", "ee", "ii", "oo", "uu", "ai", "ae", "au", "ao", "ea", "ei", "eo", "eu",
            "ia", "ie", "io", "iu", "ua", "ue", "ui", "uo"
    };
    static public Sound[][] templates = {
            {Sound.C, Sound.V}, {Sound.C, Sound.V, Sound.C}, {Sound.V, Sound.C}, {Sound.C, Sound.V, Sound.V},
            {Sound.C, Sound.V, Sound.V, Sound.C}
    };

    static private final String[] starter_nouns = {
            "water", "fire", "sun", "earth", "man", "woman", "food", "drink", "hand", "self"
    };

    public static boolean isValidSyllable(String syllable) {
        String[] forbiddenOnsets = {"bv", "ngt", "fp", "dzg", "shn"};
        String[] forbiddenCodas = {"mth", "hng", "rng"};
        for (String bad : forbiddenOnsets) {
            if (syllable.startsWith(bad)) return false;
        }
        for (String bad : forbiddenCodas) {
            if (syllable.endsWith(bad)) return false;
        }
        return true;
    }

    public static int getExponentialLength(Random random, int max_length) {
        double[] weights = new double[max_length];
        double totalWeight = 0.0;

        for (int i = 0; i < weights.length; i++) {
            weights[i] = 1.0 / Math.pow(2.0, i);
            totalWeight += weights[i];
        }

        double randomValue = random.nextDouble() * totalWeight;
        double cumulativeWeight = 0.0;

        for (int i = 0; i < weights.length; i++) {
            cumulativeWeight += weights[i];
            if (randomValue <= cumulativeWeight) {
                return i + 1;
            }
        }

        return 1;
    }

    public static void main(String[] args) {
        Random random = new Random();

        ArrayList<Sound[]> permitted_templates = new ArrayList<>();
        for (int i = 0; i < templates.length; i++) {
            boolean allow = random.nextBoolean();
            if (allow) permitted_templates.add(templates[i]);
        }
        int max_word_length = random.nextInt(15);

        String[] vocabulary = new String[starter_nouns.length];
        for (int i = 0; i < starter_nouns.length; i++) {
            String word = "";
            int length = getExponentialLength(random, max_word_length);
            Sound[] template = permitted_templates.get(random.nextInt(permitted_templates.size()));

            int k = 0;
            for (int j = 0; j < length; j++) {
                if (k == template.length) {
                    template = permitted_templates.get(random.nextInt(permitted_templates.size()));
                    k = 0;
                    continue;
                }
                if (template[j] == Sound.C) {
                    word = word.concat(possible_consonants[random.nextInt(possible_consonants.length)]);
                } else if (template[j] == Sound.V) {
                    word = word.concat(possible_vowels[random.nextInt(possible_vowels.length)]);
                }
                k++;
            }
            vocabulary[i] = word;
        }

        System.out.println("*** STARTER VOCABULARY ***");
        for (int i = 0; i < vocabulary.length; i++) {
            System.out.println(starter_nouns[i] + ": " + vocabulary[i]);
        }

    }
}
