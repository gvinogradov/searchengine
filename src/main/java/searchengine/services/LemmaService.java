package searchengine.services;

import searchengine.model.Lemma;

import java.util.Map;

public interface LemmaService {
    void deleteAll();
    Lemma get(int siteId, String lemma);
    void mergeFrequency(Map<Lemma, Integer> lemmaFrequency);
    Integer getLemmasCount(int siteId);
}
