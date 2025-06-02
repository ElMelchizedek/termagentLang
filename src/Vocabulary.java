import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.HashMap;
import java.util.stream.Collectors;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

class SeedVocabulary implements InterfaceLanguage {
    enum Manner {Nasal, Plosive, Affricate, Fricative, Liquid, Glide};
    enum VowelType {Short, Long, Diphthong};
    record Vowel(String symbol, VowelType type) {}

    static String[] consonant_symbols;
    static Map <String, Consonant> possible_consonants;
    static Vowel[] possible_vowels;
    static Sound[][] templates;
    static String[] starter_nouns;

    public static boolean isValidWord(ArrayList<Object> word) {
        for (int i = 0; i < (word.size() - 1); i++) {
            if ( (word.get(i) instanceof Consonant phonemeA) && (word.get(i + 1) instanceof Consonant phonemeB) ) {

                // + Universal Bans
                // No same voice and manner.
                if (phonemeA.manner() == phonemeB.manner()) return false;
                // No nasal and opposing voice.
                if ( (phonemeA.manner() == Manner.Nasal) && (phonemeA.voiced() != phonemeB.voiced())) return false;
                // No glottal and consonant.
                if (phonemeA.symbol().equals("'")) return false;

                // + Voicing-based restrictions.
                if ( (!phonemeA.voiced()) && (phonemeB.voiced()) ) {
                    if ( (phonemeA.manner() == Manner.Plosive) && (phonemeB.manner() == Manner.Nasal) ) return false;
                    if ( (phonemeA.manner() == Manner.Affricate) && (phonemeB.manner() == Manner.Liquid)) return false;
                }
            } else if ( (word.get(i) instanceof String phonemeA) && (word.get(i + 1) instanceof String phonemeB)) {
                ArrayList<String> long_vowels = (ArrayList<String>) Arrays.stream(possible_vowels)
                        .filter(vowel -> vowel.type == VowelType.Long)
                        .map(Vowel::symbol)
                        .collect(Collectors.toCollection(ArrayList::new));

                // No chain of diphthongs.
                if ( (phonemeA.length() > 1) || (phonemeB.length() > 1) ) return false;
                // No chain of long vowels.
                if ( (Arrays.asList(long_vowels).contains(phonemeA)) && (Arrays.asList(long_vowels).contains(phonemeB))) return false;
                // No chain of short and long vowel.
                if ( ( (Arrays.asList(long_vowels).contains(phonemeA)) && !(Arrays.asList(long_vowels).contains(phonemeB)) ) ||
                        ( !(Arrays.asList(long_vowels).contains(phonemeA)) && (Arrays.asList(long_vowels).contains(phonemeB)))) return false;
            } else if ( (word.get(i) instanceof String phonemeA) && (word.get(i+1) instanceof Consonant phonemeB)) {

                // No diphthong at start of word.
                if (phonemeA.length() > 1) return false;
            }
        }

        return true;
    }

    public static String generateWord(  Random random,
                                        ArrayList<Sound[]> permitted_templates,
                                        Map<String, Consonant> permitted_consonants,
                                        ArrayList<String> permitted_vowels) {
        String realWord = "";
        ArrayList<Object> word;

        while (true) {
            word = new ArrayList<>();
            int target_length = 4;

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
        }

        for (Object phoneme : word) {
            if (phoneme instanceof Consonant) {
                realWord = realWord.concat(((Consonant) phoneme).symbol());
            } else if (phoneme instanceof String) {
                realWord = realWord.concat((String) phoneme);
            }
        }

        return realWord;
    }

    public static void generatePermittances(Random random, Map<String, Consonant> permitted_consonants,
                                              ArrayList<Vowel> permitted_vowels, ArrayList<Sound[]> permitted_templates) {

        for (String symbol : consonant_symbols) {
            if (random.nextBoolean()) permitted_consonants.put(symbol, possible_consonants.get(symbol));
        }
        for (Vowel vowel : possible_vowels) {
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

    // I fucking hate this shit.
    public void generateSpecifications(String file) {
        Gson gson = new Gson();
        JsonObject root = null;
        try {
            String json_content = new String(Files.readAllBytes(Paths.get(file)));
            JsonReader reader = new JsonReader(new StringReader(json_content));
            reader.setStrictness(Strictness.LENIENT);
            root = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        JsonArray symbols_array = root.getAsJsonArray("symbols");
        consonant_symbols = gson.fromJson(symbols_array, String[].class);

        JsonObject consonants_object = root.getAsJsonObject("consonants");
        Type consonants_map_type = new TypeToken<Map<String, Consonant>>(){}.getType();
        possible_consonants = gson.fromJson(consonants_object, consonants_map_type);

        JsonArray vowels_array = root.getAsJsonArray("vowels");
        possible_vowels = gson.fromJson(vowels_array, Vowel[].class);

        JsonArray templates_array = root.getAsJsonArray("templates");
        templates = gson.fromJson(templates_array, Sound[][].class);

        JsonArray nouns_array = root.getAsJsonArray("nouns");
        starter_nouns = gson.fromJson(nouns_array, String[].class);
    }

}

public class Vocabulary extends SeedVocabulary {
    private final Map<String, Consonant> consonants = new HashMap<>();
    private final ArrayList<Vowel> vowels = new ArrayList<>();
    private final ArrayList<Sound[]> templates = new ArrayList<>();
    private final ArrayList<String> inventory = new ArrayList<>();

    public Vocabulary(boolean debug, Random random, String path) {
        generateSpecifications(path);
        generatePermittances(random, consonants, vowels, templates);

        if (debug) {
            System.out.print("Permitted consonants: ");
            for (Consonant phoneme : consonants.values()) System.out.print(phoneme.symbol() + ",");
            System.out.println();
            System.out.print("Pemitted vowels: ");
            for (Vowel vowel: vowels) System.out.print(vowel.symbol() + ",");
            System.out.println();
            System.out.print("Permitted templates: ");
            for (Sound[] template : templates) {
                System.out.print("[");
                for (Sound sound : template) {
                    if (sound == Sound.C) System.out.print("C");
                    else if (sound == Sound.V) System.out.print("V");
                }
                System.out.print("],");
            }
        }

        for (int i = 0; i < starter_nouns.length; i++ ) {
            // Get vowel symbols.
            ArrayList<String> vowels = (ArrayList<String>) Arrays.stream(possible_vowels)
                            .map(Vowel::symbol)
                            .collect(Collectors.toCollection(ArrayList::new));

            inventory.add(generateWord(random, templates, consonants, vowels));
        }

        if (debug) {
            System.out.println();
            for (int i = 0; i < inventory.size(); i++) {
                System.out.println(starter_nouns[i] + ": " + inventory.get(i));
            }
        }

        System.out.println("\n\n");
    }


    public Map<String, Consonant> getConsonants() {
        return this.consonants;
    }
    public List<Consonant> getConsonantsValues() {
        return new ArrayList<>(consonants.values());
    }
    public ArrayList<Vowel> getVowels() {
        return this.vowels;
    }
    public ArrayList<Sound[]> getTemplates() {
        return this.templates;
    }
    public ArrayList<String> getInventory() {
        return this.inventory;
    }

    public String generateWord(Random random) {
        // Get vowel symbols.
        ArrayList<String> vowels = (ArrayList<String>) Arrays.stream(possible_vowels)
                .map(Vowel::symbol)
                .collect(Collectors.toCollection(ArrayList::new));

        return (generateWord(random, templates, consonants, vowels));
    }

    public void soundChange() {}
}