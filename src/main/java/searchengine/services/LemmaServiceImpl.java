package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public Integer getLemmasCount(int siteId) {
        Integer count = lemmaRepository.getLemmasCount(siteId);
        return count == null ? 0 : count;
    }
}
