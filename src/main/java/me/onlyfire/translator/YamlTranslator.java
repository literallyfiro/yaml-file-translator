package me.onlyfire.translator;

import com.deepl.api.DeepLException;
import com.deepl.api.TextResult;
import com.deepl.api.Translator;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class YamlTranslator {

    private final Translator translator;

    public YamlTranslator(Translator translator) {
        this.translator = translator;
    }

    public void translateMap(Map<String, Object> map, String langToTranslate) {
        translateMap(map, langToTranslate, new AtomicInteger(countKeys(map)), "");
    }

    @SuppressWarnings("unchecked")
    private void translateMap(Map<String, Object> map, String langToTranslate, AtomicInteger countKeys, String currentKeyPath) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            try {
                String fullKeyPath = currentKeyPath.isEmpty() ? entry.getKey() : currentKeyPath + "." + entry.getKey();
                System.out.println("Translating key: " + fullKeyPath + " (Remaining: " + countKeys.decrementAndGet() + " keys)");

                if (entry.getValue() instanceof String) {
                    var translated = translateString((String) entry.getValue(), langToTranslate);
                    map.put(entry.getKey(), translated);
                } else if (entry.getValue() instanceof Map) {
                    // value is a map, call translateMap recursively
                    Map<String, Object> innerMap = (Map<String, Object>) entry.getValue();
                    translateMap(innerMap, langToTranslate, countKeys, fullKeyPath);
                } else if (entry.getValue() instanceof List<?> && ((List<?>) entry.getValue()).get(0) instanceof String) {
                    var translated = translateList((List<String>) entry.getValue(), langToTranslate);
                    map.put(entry.getKey(), translated);
                }

            } catch (InterruptedException | DeepLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<String> translateList(List<String> list, String langToTranslate)
            throws DeepLException, InterruptedException {
        var translated_list = translator.translateText(list, null, langToTranslate);
        return translated_list.stream().map(TextResult::getText).collect(Collectors.toList());
    }

    private String translateString(String string, String langToTranslate)
            throws DeepLException, InterruptedException {
        if (string.isEmpty()) return string;
        return translator.translateText(string, null, langToTranslate).getText();
    }

    private int countKeys(Map<String, Object> map) {
        int count = map.size();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                count += countKeys((Map<String, Object>) entry.getValue());
            } else if (entry.getValue() instanceof List) {
                for (Object item : (List) entry.getValue()) {
                    if (item instanceof Map) {
                        count += countKeys((Map<String, Object>) item);
                    }
                }
            }
        }
        return count;
    }

}
