package searchengine.services;

import com.mysql.cj.log.Log;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.ParserCfg;
import searchengine.config.SiteCfg;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.utils.Parser;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;
    private final ParserCfg parserCfg;
    private final FactoryService factoryService;

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
        factoryService.getPageService().deleteAll();
        factoryService.getSiteService().deleteAll();

        for (SiteCfg siteCfg : sites.getSites()) {
            boolean isAvailable = factoryService.getNetworkService().checkSiteConnection(siteCfg.getUrl());
            if (isAvailable == false) {
                log.error("сайт " + siteCfg.getUrl() + " не доступен");
                continue;
            }

            Site site = factoryService.getSiteService().createSite(siteCfg);
            factoryService.getSiteService().save(site);
            Parser parser = new Parser(site, site.getUrl() + "/", factoryService, parserCfg);
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

}
