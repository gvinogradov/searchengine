package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.springframework.stereotype.Service;
import searchengine.config.ParserCfg;
import searchengine.config.SiteCfg;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.*;
import searchengine.utils.Parser;
import searchengine.utils.ThreadHandler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Service
public class IndexingServiceImpl implements IndexingService {

    private final SitesList sites;
    private final ParserCfg parserCfg;
    private final NetworkService networkService;
    private final SiteService siteService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final MorphologyService morphologyService;

    public IndexingServiceImpl(SitesList sites, ParserCfg parserCfg,
                               NetworkService networkService, SiteService siteService,
                               PageService pageService, LemmaService lemmaService,
                               IndexService indexService, MorphologyService morphologyService) {
        this.sites = sites;
        this.parserCfg = parserCfg;
        this.networkService = networkService;
        this.siteService = siteService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.morphologyService = morphologyService;
        siteService.dropIndexingStatus();
    }

    @Override
    public IndexingResponse startIndexing() {
        if (siteService.isIndexing()) {
            return new IndexingResponse(false, "Идет индексация");
        }

        Parser.setIsCanceled(false);

        Thread thread = new Thread(() -> {
            indexService.deleteAll();
            lemmaService.deleteAll();
            pageService.deleteAll();
            siteService.deleteAll();

            List<Site> sitesToParsing = siteService.getSitesToParsing(sites);
            siteService.saveAll(sitesToParsing);

            for (Site site : sitesToParsing) {
                if (site.getStatus() == Status.INDEXING) {
                    ThreadHandler task = new ThreadHandler(new ForkJoinPool(parserCfg.getParallelism()),
                            parserCfg, networkService, siteService,
                            this, site, site.getUrl() + "/");
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

    @Override
    public IndexingResponse indexPage(String url) {
        String finalUrl = url;
        Optional findUrl = sites.getSites().stream()
                .filter(s -> finalUrl.startsWith(s.getUrl()))
                .findFirst();
        if (findUrl.isEmpty()) {
            return new IndexingResponse(false, "Данная страница находится за пределами сайтов, " +
                    "указанных в конфигурационном файле");
        }
        SiteCfg siteCfg = (SiteCfg) findUrl.get();
        url = url.equals(siteCfg.getUrl()) ? url + "/" : url;

        Connection.Response response = networkService.getResponse(url);
        if (!networkService.isAvailableContent(response)) {
            return new IndexingResponse(false, "Ошибка обработки страницы " + url);
        }

        Thread thread = new Thread(() -> {
            Site site = siteService.getByUrl(siteCfg.getUrl());
            if (site == null) {
                site = siteService.createSite(siteCfg, Status.INDEXED, "");
                site = siteService.save(site);
            }

            try {
                parsePage(site, response);
            } catch (Exception e) {
                site.setLastError(e.getMessage());
                site.setStatus(Status.FAILED);
                siteService.save(site);
                log.error(e.getMessage());
            }
        });
        thread.start();
        return new IndexingResponse(true, "");
    }

    @Override
    public void parsePage(Site site, Connection.Response response) throws Exception {
        Page page = pageService.addPage(site, response);

        Map<String, Integer> lemmaMap = morphologyService.collectLemmas(page.getContent());
        List<Integer> lemmaIdList = indexService.getLemmaIdListByPageId(page.getId());
        if (lemmaIdList.size() > 0) {
            indexService.deleteByPageId(page.getId());
            for (Integer lemmaId : lemmaIdList) {
                lemmaService.decreaseFrequencyByLemmaId(lemmaId);
            }
        }

        List<Lemma> lemmas = lemmaService.createLemmas(lemmaMap.keySet(), site);
        lemmaService.mergeFrequency(lemmas);

        List<Index> indexes = indexService.addIndexes(lemmaMap, site, page);
        indexService.saveAll(indexes);
    }

}
