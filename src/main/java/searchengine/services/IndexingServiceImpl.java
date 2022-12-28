package searchengine.services;

import lombok.Getter;
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
import java.util.concurrent.ForkJoinTask;


@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;
    private final ParserCfg parserCfg;
    private final ServicesFactory servicesFactory;

    private ForkJoinPool forkJoinPool;
    private List<SiteRecursiveReader> siteRecursiveReaders;

    @Override
    public IndexingResponse startIndexing() {
        List<SiteParser> siteParsers = new ArrayList<>();
        forkJoinPool = new ForkJoinPool(parserCfg.getParallelism());
        servicesFactory.getPageService().deleteAll();
        servicesFactory.getSiteService().deleteAll();
        SiteParser.setParserCfg(parserCfg);
        SiteParser.setPageService(servicesFactory.getPageService());

        for (SiteCfg siteCfg : sites.getSites()) {
            Site site = servicesFactory.getSiteService().addBySiteCfg(siteCfg, Status.INDEXING);
            SiteParser siteParser = new SiteParser(site, siteCfg.getUrl() + "/");
            siteParsers.add(siteParser);
            forkJoinPool.execute(siteParser);
        }
        return new IndexingResponse(true, "");
    }


    @Override
    public IndexingResponse stopIndexing() {
        forkJoinPool.shutdownNow();
        return new IndexingResponse(true, "");
    }
}
