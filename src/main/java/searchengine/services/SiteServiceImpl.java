package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.SiteCfg;
import searchengine.config.SitesList;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;
    private final NetworkService networkService;

    @Override
    public Site save(Site site) {
        return siteRepository.saveAndFlush(site);
    }

    @Override
    public void saveAll(List<Site> sites) {
        siteRepository.saveAllAndFlush(sites);
    }

    @Override
    public Site getByUrl(String url) {
        return siteRepository.getByUrl(url);
    }

//    todo: убрать проверку и передавать статус
    @Override
    public Site createSite(SiteCfg siteCfg, Status status, String lastError) {
//        Site site = null;
//        boolean isAvailable = networkService.checkSiteConnection(siteCfg.getUrl());
//        String lastError = isAvailable ? "" : "Сайт не доступен";
//        Status status = isAvailable ? Status.INDEXING : Status.FAILED;
        Site site = new Site();
        site.setUrl(siteCfg.getUrl());
        site.setName(siteCfg.getName());
        site.setStatus(status);
        site.setStatusTime(LocalDateTime.now());
        site.setLastError(lastError);
        return site;
    }

//    todo: перенести статус
    @Override
    public List<Site> getSitesToParsing(SitesList sites) {
        List<Site> sitesToParsing = new ArrayList<>();
        for (SiteCfg siteCfg : sites.getSites()) {
            boolean isAvailable = networkService.checkSiteConnection(siteCfg.getUrl());
            String lastError = isAvailable ? "" : "Сайт не доступен";
            Status status = isAvailable ? Status.INDEXING : Status.FAILED;
            Site site = createSite(siteCfg, status, lastError);
            sitesToParsing.add(site);
        }
        return sitesToParsing;
    }

    @Override
    public Site addSiteToParsing(Site site) {
        return null;
    }

    @Override
    public List<Site> getAll() {
        return siteRepository.findAll();
    }

    @Override
    public void deleteAll() {
        siteRepository.deleteAll();
    }

    @Override
    public boolean isIndexing() {
        return siteRepository.findAnyStatus(Status.INDEXING) != null;
    }

    @Override
    public void dropIndexingStatus() {
        siteRepository.updateStatus(Status.INDEXING, Status.FAILED);
    }

    @Override
    public void updateSiteStatus(Site site, Status status, String lastError) {
        site.setStatus(status);
        site.setLastError(lastError);
        site.setStatusTime(LocalDateTime.now());
        save(site);
    }

}
