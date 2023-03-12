package me.onlyfire.translator;

import com.deepl.api.Translator;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TranslatorMain {

    protected static final List<String> LANGS = List.of("BG", "CS", "DA", "DE", "EL", "EN-GB", "EN-US", "ES", "ET", "FI", "FR", "HU", "ID", "IT", "JA", "KO", "LT", "LV", "NB", "NL", "PL", "PT", "PT", "PT", "RO", "RU", "SK", "SL", "SV", "TR", "UK", "ZH");

    public static void main(String[] args) throws IOException {
        String jarName = System.getProperty("java.class.path");
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        Options options = new Options()
                .addOption(Option.builder("a").longOpt("auth-key")
                        .desc("Auth key for DeepL API")
                        .hasArg()
                        .required()
                        .build()
                )
                .addOption(Option.builder("i").longOpt("input")
                        .desc("Input file to be translated")
                        .hasArg()
                        .required()
                        .build())
                .addOption(Option.builder("l").longOpt("lang")
                        .desc("Language to translate to")
                        .hasArg()
                        .required()
                        .build()
                );

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            var input_file = new File(line.getOptionValue("i"));
            if (!input_file.exists()) {
                System.out.printf("File %s does not exist%n", input_file.getAbsolutePath());
                return;
            }

            var lang_to_translate = line.getOptionValue("l").toUpperCase();
            if (!LANGS.contains(lang_to_translate)) {
                System.out.printf("Language %s is not supported%n", lang_to_translate);
                System.out.println("Supported languages: " + LANGS);
                return;
            }

            var authKey = line.getOptionValue("a");
            var writer = new YamlConfigWriter();
            var loadedConfiguration = writer.readAndSaveDefaults(input_file);
            var deeplTranslator = new Translator(authKey);

            if (loadedConfiguration == null) return;

            // translate the configuration
            new YamlTranslator(deeplTranslator).translateMap(loadedConfiguration, lang_to_translate);
            // work has been done, save the file
            writer.writeConfiguration(input_file, loadedConfiguration);
        } catch (ParseException exp) {
            formatter.printHelp(jarName, options);
            System.out.println("Unexpected exception: " + exp.getMessage());
        }

    }
}