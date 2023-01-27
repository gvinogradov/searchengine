package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LemmaServiceImpl implements LemmaService{
    private final LemmaRepository lemmaRepository;

    @Override
    public void deleteAll() {
        lemmaRepository.deleteAll();
    }

    @Override
    public Lemma get(int siteId, String lemma) {
        return lemmaRepository.get(siteId, lemma);
    }

    @Override
    public List<Lemma> createLemmas(Set<String> lemmaSet, Site site) {
        List<Lemma> lemmas = new ArrayList<>();
        for (String lemmaName : lemmaSet) {
            Lemma lemma = new Lemma();
            lemma.setLemma(lemmaName);
            lemma.setSite(site);
            lemmas.add(lemma);
        }
        return lemmas;
    }

    @Override
    public Lemma createBlankLemma(String lemma) {
        Lemma lemmaEntity = new Lemma();
        lemmaEntity.setLemma(lemma);
        return lemmaEntity;
    }

    @Override
    public void mergeFrequency(Map<Lemma, Integer> lemmaFrequency) {
        for(Map.Entry<Lemma, Integer> entry : lemmaFrequency.entrySet()) {
            String lemma = entry.getKey().getLemma();
            int frequency = entry.getValue();
            int siteId = entry.getKey().getSite().getId();
            lemmaRepository.merge(siteId, lemma, frequency);
        }
    }

    @Override
    public void mergeFrequency(List<Lemma> lemmas) {
        for (Lemma lemma: lemmas) {
            lemmaRepository.merge(lemma.getSite().getId(),
                    lemma.getLemma(),
                    1);
        }
    }

    @Override
    public void decreaseFrequencyByLemmaId(int lemmaId) {
        lemmaRepository.decreaseFrequencyByLemmaId(lemmaId);
    }

    @Override
    public Integer getLemmasCount(int siteId) {
        Integer count = lemmaRepository.getLemmasCount(siteId);
        return count == null ? 0 : count;
    }

    @Override
    public List<Lemma> getSortedFoundList(Set<String> lemmasInQuery, int maxFrequency) {
        List<Lemma> foundLemmas = lemmaRepository.getSortedFoundList(lemmasInQuery);
        return filteredLemmasList(lemmasInQuery, foundLemmas, maxFrequency);
    }

    @Override
    public List<Lemma> getSortedFoundList(Set<String> lemmasInQuery, int maxFrequency, int siteId) {
        List<Lemma> foundLemmas = lemmaRepository.getSortedFoundList(lemmasInQuery, siteId);
        return filteredLemmasList(lemmasInQuery, foundLemmas, maxFrequency);
    }

    @Override
    public List<Lemma> filteredLemmasList(Set<String> lemmasInQuery, List<Lemma> foundLemmas, int maxFrequency) {
        for (String lemma: lemmasInQuery) {
            if (foundLemmas.stream()
                    .anyMatch(l -> l.getLemma().equals(lemma))) {
                foundLemmas.add(createBlankLemma(lemma));
            }
        }
        List<Lemma> filteredLemmas = foundLemmas.stream()
                .filter(l -> l.getFrequency() <= maxFrequency)
                .sorted()
                .toList();

        return filteredLemmas;
    }
}
