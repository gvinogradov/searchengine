package searchengine.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.ParserCfg;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.services.FactoryService;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@Slf4j
public class ThreadHandler implements Runnable {

    private static ParserCfg parserCfg;
    private static FactoryService factoryService;
    private static ForkJoinPool forkJoinPool;
    private Site site;

    public ThreadHandler(ParserCfg parserCfg, FactoryService factoryService, Site site) {
        ThreadHandler.parserCfg = parserCfg;
        ThreadHandler.factoryService = factoryService;
        this.site = site;
        if (forkJoinPool == null) {
            forkJoinPool = new ForkJoinPool(parserCfg.getParallelism());
        }
    }

    @Override
    public void run() {
        try {
            Long start = System.currentTimeMillis();
            Parser parser = new Parser(site, site.getUrl() + "/", factoryService, parserCfg);
            if (forkJoinPool.invoke(parser)) {
                factoryService.getSiteService().updateSiteStatus(site, Status.INDEXED, "");
                log.info(site.getUrl() + " - " + String.valueOf(System.currentTimeMillis() - start));
            } else {
                factoryService.getSiteService().updateSiteStatus(site, Status.FAILED, "Индексация остановлена пользователем");
            }
        } catch (Exception e) {
            log.info("Старт индексации ошибка " + e.getMessage());
        }
    }
}
