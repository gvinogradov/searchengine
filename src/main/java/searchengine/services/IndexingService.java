package searchengine.services;

import searchengine.config.SiteCfg;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Site;

public interface IndexingService {

    IndexingResponse startIndexing();
    IndexingResponse stopIndexing();
    Site createSite(SiteCfg siteCfg);
}
