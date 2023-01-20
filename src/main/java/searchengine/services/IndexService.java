package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.repository.IndexRepository;

@Service
public interface IndexService {
    Index save(Index index);
}
