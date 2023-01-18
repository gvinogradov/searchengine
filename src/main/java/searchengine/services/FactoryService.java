package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Getter
public class FactoryService {
    private final NetworkService networkService;
    private final PageService pageService;
    private final SiteService siteService;
    private final LemmaService lemmaService;
}
