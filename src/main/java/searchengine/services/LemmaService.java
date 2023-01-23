package searchengine.services;

import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LemmaService {
    void deleteAll();
    Lemma get(int siteId, String lemma);
    List<Lemma> createLemmas(Set<String> lemmaSet, Site site);
    void mergeFrequency(Map<Lemma, Integer> lemmaFrequency);
    void mergeFrequency(List<Lemma> lemmas);
    Integer getLemmasCount(int siteId);
}
