package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.ParserCfg;
import searchengine.config.SiteCfg;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Site;
import searchengine.model.Status;

import java.time.LocalDateTime;
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

    @Override
    public IndexingResponse startIndexing() {
        if (isIndexing) {
            return new IndexingResponse(false, "Идет индексация");
        }
        isIndexing = true;
        Parser.setIsCanceled(false);
        forkJoinPool = new ForkJoinPool(parserCfg.getParallelism());
        pageService.deleteAll();
        siteService.deleteAll();

        for (SiteCfg siteCfg : sites.getSites()) {
            Site site = createSite(siteCfg);
            siteService.save(site);
            Parser parser = new Parser(site, site.getUrl() + "/", pageService, siteService, parserCfg);
            forkJoinPool.execute(parser);
        }
        return new IndexingResponse(true, "");
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (!isIndexing) {
            return new IndexingResponse(false, "Нет работающих процессов индексации");
        }
        isIndexing = false;
        Parser.setIsCanceled(true);
        return new IndexingResponse(true, "");
    }

    @Override
    public Site createSite(SiteCfg siteCfg) {
        Site site = new Site();
        site.setUrl(siteCfg.getUrl());
        site.setName(siteCfg.getName());
        site.setStatus(Status.INDEXING);
        site.setStatusTime(LocalDateTime.now());
        site.setLastError("");
        return site;
    }
}
