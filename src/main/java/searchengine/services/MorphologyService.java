package searchengine.services;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MorphologyService {
    Map<String, Integer> collectLemmas(String html);

    Set<String> getLemmaSet(String html);

    String getSnippet(String html, List<String> lemmas, int snippetSize);
}
