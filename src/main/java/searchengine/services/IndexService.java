package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;

import java.util.List;
import java.util.Map;

@Service
public interface IndexService {
    Index save(Index index);
    void saveAll(List<Index> indexes);
    void deleteAll();
    List<Index> addIndexes(Map<String, Integer> lemmaMap, Site site, Page page);
    List<Integer> getLemmaIdListByPageId(int pageId);
    void deleteByPageId(int pageId);
}
