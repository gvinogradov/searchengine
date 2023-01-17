package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.ParserCfg;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.utils.Parser;
import searchengine.utils.ThreadHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;
    private final ParserCfg parserCfg;
    private final FactoryService factoryService;
    private ThreadPoolExecutor threadPoolExecutor;

    public IndexingServiceImpl(SitesList sites, ParserCfg parserCfg, FactoryService factoryService) {
        this.sites = sites;
        this.parserCfg = parserCfg;
        this.factoryService = factoryService;
        this.factoryService.getSiteService().dropIndexingStatus();
    }

    @Override
    public IndexingResponse startIndexing() {
        if (factoryService.getSiteService().isIndexing())
        {
            return new IndexingResponse(false, "Идет индексация");
        }

        Parser.setIsCanceled(false);

        Thread thread = new Thread(() -> {
            factoryService.getPageService().deleteAll();
            factoryService.getSiteService().deleteAll();

            List<Site> sitesToParsing = factoryService.getSiteService().getSitesToParsing(sites);
            factoryService.getSiteService().saveAll(sitesToParsing);

            for (Site site : sitesToParsing) {
                if (site.getStatus() == Status.INDEXING) {
                    ThreadHandler task = new ThreadHandler(parserCfg, factoryService, site);
                    Thread parseSite = new Thread(task);
                    parseSite.start();
                }
            }
        });
        thread.start();

        return new IndexingResponse(true, "");
    }

    @Override
    public IndexingResponse stopIndexing() {
        Parser.setIsCanceled(true);
        return new IndexingResponse(true, "");
    }

}
