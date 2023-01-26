package searchengine.services;

import java.util.Map;
import java.util.Set;

public interface MorphologyService {
    Map<String, Integer> collectLemmas(String html);
    Set<String> getLemmaSet(String html);
}
