package searchengine.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.ParserCfg;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.services.FactoryService;
import searchengine.services.IndexingService;
import searchengine.services.NetworkService;
import searchengine.services.SiteService;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@Slf4j
public class ThreadHandler implements Runnable {

    private static ForkJoinPool forkJoinPool;
    private ParserCfg parserCfg;
    private NetworkService networkService;
    private SiteService siteService;
    private IndexingService indexingService;
    private Site site;
    private String startUrl;

    public ThreadHandler(ParserCfg parserCfg, NetworkService networkService,
                         SiteService siteService, IndexingService indexingService,
                         Site site, String startUrl) {
        this.parserCfg = parserCfg;
        this.indexingService = indexingService;
        this.networkService = networkService;
        this.siteService = siteService;
        this.site = site;
        this.startUrl = startUrl;
        if (forkJoinPool == null) {
            forkJoinPool = new ForkJoinPool(parserCfg.getParallelism());
        }
    }

    @Override
    public void run() {
        try {
            Parser parser = new Parser(site, startUrl, networkService, indexingService, parserCfg);
            Long start = System.currentTimeMillis();
            if (forkJoinPool.invoke(parser)) {
                siteService.updateSiteStatus(site, Status.INDEXED, "");
                System.out.println(site.getUrl() + " - " + (System.currentTimeMillis() - start));
            } else {
                siteService.updateSiteStatus(site, Status.FAILED, "Индексация остановлена пользователем");
            }
        } catch (Exception e) {
            log.info("Старт индексации ошибка " + e.getMessage());
        }
    }
}
