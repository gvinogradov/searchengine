package searchengine.services;

import searchengine.config.SearchCfg;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LemmaService {
    void deleteAll();
    Lemma get(int siteId, String lemma);
    List<Lemma> getSortedLemmas(SearchCfg searchCfg);
    Map<String, Integer> collectLemmaFrequency(SearchCfg searchCfg);
    List<Lemma> createLemmas(Set<String> lemmaSet, Site site);
    void mergeFrequency(List<Lemma> lemmas);
    void decreaseFrequencyByLemmaId(int lemmaId);
    Integer getLemmasCount(int siteId);
    List<Lemma> getFoundLemmas(Set<String> lemmasInQuery, int siteId);
}
