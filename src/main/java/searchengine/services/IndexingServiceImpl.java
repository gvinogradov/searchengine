package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.ParserCfg;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Site;
import searchengine.utils.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Service
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;
    private final ParserCfg parserCfg;
    private final FactoryService factoryService;
    private final ForkJoinPool forkJoinPool;


    public IndexingServiceImpl(SitesList sites, ParserCfg parserCfg, FactoryService factoryService) {
        this.sites = sites;
        this.parserCfg = parserCfg;
        this.factoryService = factoryService;
        this.forkJoinPool = new ForkJoinPool(parserCfg.getParallelism());
        this.factoryService.getSiteService().dropIndexingStatus();
    }



    @Override
    public IndexingResponse startIndexing() {
        if (factoryService.getSiteService().isIndexing()) {
            return new IndexingResponse(false, "Идет индексация");
        }
        Parser.setIsCanceled(false);

        factoryService.getPageService().deleteAll();
//        factoryService.getSiteService().deleteAll();

        List<Site> sitesToParsing = factoryService.getSiteService().getSitesToParsing(sites);
        if (sitesToParsing.size() == 0) {
            return new IndexingResponse(false, "Нет доступных сайтов для индексации");
        }

        List<Parser> parserList = new ArrayList<>();
        for (Site site: sitesToParsing) {
            parserList.add(new Parser(site, site.getUrl() + "/", factoryService, parserCfg));
        }

        parserList.forEach(p -> forkJoinPool.execute(p));
        return new IndexingResponse(true, "");
    }

    @Override
    public IndexingResponse stopIndexing() {
        Parser.setIsCanceled(true);
        return new IndexingResponse(true, "");
    }

}
