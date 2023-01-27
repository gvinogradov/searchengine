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
    Lemma createBlankLemma(String lemma);
    void mergeFrequency(Map<Lemma, Integer> lemmaFrequency);
    void mergeFrequency(List<Lemma> lemmas);
    void decreaseFrequencyByLemmaId(int lemmaId);
    Integer getLemmasCount(int siteId);
    List<Lemma> getSortedFoundList(Set<String> lemmasInQuery, int maxFrequency);
    List<Lemma> getSortedFoundList(Set<String> lemmasInQuery, int maxFrequency, int siteId);
    List<Lemma> filteredLemmasList(Set<String> lemmasInQuery, List<Lemma> foundLemmas, int maxFrequency);
}
