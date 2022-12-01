package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Parser;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;

import java.util.concurrent.ForkJoinPool;


@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;
    private final Parser parser;

    @Override
    public IndexingResponse startIndexing() {
        ForkJoinPool pool = new ForkJoinPool(parser.getParallelism());
        for (Site site: sites.getSites()) {
            SiteParser siteParser = new SiteParser(site, "/", parser);
            pool.execute(siteParser);
        }

        IndexingResponse response = new IndexingResponse();
        response.setResult(true);
        response.setError("");
        return response;
    }

    @Override
    public IndexingResponse stopIndexing() {

        IndexingResponse response = new IndexingResponse();
        response.setResult(true);
        response.setError("");
        return response;
    }
}
