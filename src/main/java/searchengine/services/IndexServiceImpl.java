package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.repository.IndexRepository;

@Service
@RequiredArgsConstructor
public class IndexServiceImpl implements IndexService {
    private final IndexRepository indexRepository;

    @Override
    public Index save(Index index) {
        return indexRepository.saveAndFlush(index);
    }
}
