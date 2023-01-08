package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.ParserCfg;
import searchengine.config.SiteCfg;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Site;
import searchengine.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;
    private final ParserCfg parserCfg;
    private final PageService pageService;
    private final SiteService siteService;

    private boolean isIndexing;
    private ForkJoinPool forkJoinPool;
    private List<SiteParser> siteParsers;

    @Override
    public IndexingResponse startIndexing() {
        if (isIndexing) {
            return new IndexingResponse(false, "Идет индексация");
        }
        isIndexing = true;
        SiteParser.setIsCanceled(false);
        siteParsers = new ArrayList<>();
        forkJoinPool = new ForkJoinPool(parserCfg.getParallelism());
        pageService.deleteAll();
        siteService.deleteAll();

        for (SiteCfg siteCfg : sites.getSites()) {
            Site site = siteService.addBySiteCfg(siteCfg, Status.INDEXING);
            SiteParser siteParser = new SiteParser(site, siteCfg.getUrl() + "/", pageService, parserCfg);
//            siteParsers.add(siteParser);
            forkJoinPool.execute(siteParser);
        }
        return new IndexingResponse(true, "");
    }


    @Override
    public IndexingResponse stopIndexing() {
        if (!isIndexing) {
            return new IndexingResponse(false, "Нет работающих процессов индексации");
        }
        isIndexing = false;
        SiteParser.setIsCanceled(true);
        return new IndexingResponse(true, "");
    }
}
