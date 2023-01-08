package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.SiteCfg;
import searchengine.exception.ResourceNotFoundException;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.dao.PageDAO;
import searchengine.dao.SiteDAO;
import searchengine.model.Status;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService{

    private final SiteDAO siteDAO;

    private final PageDAO pageDAO;

    private final PageServiceImpl pageService;

    @Override
    public Site add(Site site) {
        return siteDAO.saveAndFlush(site);
    }

    @Override
    public Site addBySiteCfg(SiteCfg siteCfg, Status status) {
        Site site = new Site();
        site.setName(siteCfg.getName());
        site.setUrl(siteCfg.getUrl());
        site.setStatus(status);
        site.setStatusTime(LocalDateTime.now());
        return add(site);
    }

    @Override
    public Site get(int siteId) {
        return siteDAO.findById(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("siteId " + siteId + " not found"));
    }

    public Site get(String url) {
        return siteDAO.getByUrl(url);
    }

    @Override
    public void update(int siteId, Site site) {
        siteDAO.findById(siteId).map(s -> {
            s.setName(site.getName());
            s.setStatus(site.getStatus());
            s.setStatusTime(site.getStatusTime());
            s.setUrl(site.getUrl());
            return siteDAO.save(s);
        }).orElseThrow(() -> new ResourceNotFoundException("siteId " + siteId + " not found"));
    }

    @Override
    public void delete(int id) {
        siteDAO.deleteById(id);
    }

    @Override
    public void deleteAll() {
        siteDAO.deleteAll();
    }
}
