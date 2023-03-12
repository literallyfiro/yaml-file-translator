package me.onlyfire.translator;

import com.deepl.api.DeepLException;
import com.deepl.api.TextResult;
import com.deepl.api.Translator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class YamlTranslator {

    private final Translator translator;

    public YamlTranslator(Translator translator) {
        this.translator = translator;
    }

    @SuppressWarnings("unchecked")
    public void translateMap(Map<String, Object> map, String lang_to_translate) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            try {
                if (entry.getValue() instanceof String) {
                    var translated = translateString((String) entry.getValue(), lang_to_translate);
                    map.put(entry.getKey(), translated);
                } else if (entry.getValue() instanceof Map) {
                    // value is a map, call translateMap recursively
                    Map<String, Object> innerMap = (Map<String, Object>) entry.getValue();
                    translateMap(innerMap, lang_to_translate);
                } else if (entry.getValue() instanceof List<?> && ((List<?>) entry.getValue()).get(0) instanceof String) {
                    var translated = translateList((List<String>) entry.getValue(), lang_to_translate);
                    map.put(entry.getKey(), translated);
                }
            } catch (InterruptedException | DeepLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<String> translateList(List<String> list, String lang_to_translate)
            throws DeepLException, InterruptedException {
        var translated_list = translator.translateText(list, null, lang_to_translate);
        return translated_list.stream().map(TextResult::getText).collect(Collectors.toList());
    }

    public String translateString(String string, String lang_to_translate)
            throws DeepLException, InterruptedException {
        if (string.isEmpty()) return string;
        return translator.translateText(string, null, lang_to_translate).getText();
    }

}
