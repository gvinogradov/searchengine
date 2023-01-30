package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.SearchCfg;
import searchengine.model.Lemma;
import searchengine.model.Site;
import searchengine.repository.LemmaRepository;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LemmaServiceImpl implements LemmaService{
    private final LemmaRepository lemmaRepository;
    private final SiteService siteService;
    private final MorphologyService morphologyService;

    @Override
    public void deleteAll() {
        lemmaRepository.deleteAll();
    }

    @Override
    public Lemma get(int siteId, String lemma) {
        return lemmaRepository.get(siteId, lemma);
    }

    @Override
    public List<Lemma> getSortedLemmas(SearchCfg searchCfg) {
        List<Lemma> lemmas = new ArrayList<>();
        try {
            Set<String> queryLemmas = morphologyService.getLemmaSet(searchCfg.getQuery());
            List<Site> sites = siteService.getSites(searchCfg);
            for (Site site: sites) {
                lemmas.addAll(getFoundLemmas(queryLemmas, site.getId()));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
        return lemmas.stream()
                .filter(l -> l.getFrequency() <= searchCfg.getTreshhold())
                .sorted()
                .toList();
    }

    @Override
    public Map<String, Integer> collectLemmaFrequency(SearchCfg searchCfg) {
        Map<String, Integer> lemmasFrequency = new HashMap<>();
        try {
            Set<String> queryLemmas = morphologyService.getLemmaSet(searchCfg.getQuery());
            for (String lemma: queryLemmas) {
                Integer frequency = lemmaRepository.getLemmaFrequency(lemma);
                if (frequency != null) {
                    lemmasFrequency.put(lemma, frequency);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.emptyMap();
        }

        lemmasFrequency.values().removeIf(v -> v > searchCfg.getTreshhold());
        return lemmasFrequency;
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
    public List<Lemma> getFoundLemmas(Set<String> lemmasInQuery, int siteId) {
        List<Lemma> foundLemmas = lemmaRepository.getLemmasFoundList(lemmasInQuery, siteId);
        for (String lemma: lemmasInQuery) {
            if (!foundLemmas.stream()
                    .anyMatch(l -> l.getLemma().equals(lemma))) {
                return Collections.emptyList();
            }
        }
        return foundLemmas;
    }
}
