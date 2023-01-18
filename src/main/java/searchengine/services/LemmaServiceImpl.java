package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.repository.LemmaRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LemmaServiceImpl implements LemmaService{
    private final LemmaRepository lemmaRepository;

    @Override
    public void deleteAll() {
        lemmaRepository.deleteAll();
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
    public Integer getLemmasCount(int siteId) {
        Integer count = lemmaRepository.getLemmasCount(siteId);
        return count == null ? 0 : count;
    }
}
