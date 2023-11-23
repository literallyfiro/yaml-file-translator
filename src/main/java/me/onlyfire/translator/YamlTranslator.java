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

    @SuppressWarnings({"unchecked", "rawtypes"})
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
                } else if (entry.getValue() instanceof List) {
                    var translated = translateList((List) entry.getValue(), langToTranslate);
                    map.put(entry.getKey(), translated);
                }

            } catch (InterruptedException | DeepLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List translateList(List list, String langToTranslate) throws DeepLException, InterruptedException {
        if (list.isEmpty()) return list;

        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            if (item == null) continue;

            if (item instanceof Map) {
                translateMap((Map<String, Object>) list.get(i), langToTranslate);
                continue;
            }

            if (item instanceof String) {
                list.set(i, translateString((String) list.get(i), langToTranslate));
            }
        }

        return list;
    }

    private String translateString(String string, String langToTranslate) throws DeepLException, InterruptedException {
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
