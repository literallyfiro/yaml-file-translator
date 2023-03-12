package me.onlyfire.translator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.Map;

public class YamlConfigWriter {

    protected static final YAMLMapper MAPPER = new YAMLMapper().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER);

    public void writeConfiguration(File input, Map<String, Object> config) throws IOException {
        MAPPER.writeValue(input, config);
    }

    public Map<String, Object> readAndSaveDefaults(File input) throws IOException {
        try {
            Files.copy(input.toPath(), new File("original_" + input.getName()).toPath());
        } catch (FileAlreadyExistsException e) {
            System.out.printf("File %s already exists. %n", "original_" + input.getName());
            return null;
        }

        return MAPPER.readValue(input, new TypeReference<>() {});
    }
}
