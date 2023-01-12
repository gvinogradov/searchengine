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

    @Override
    public Site save(Site site) {
        return siteDAO.saveAndFlush(site);
    }

    @Override
    public Site getByUrl(String url) {
        return siteDAO.getByUrl(url);
    }

    @Override
    public List<Site> getAll() {
        return siteDAO.findAll();
    }

    @Override
    public void deleteAll() {
        siteDAO.deleteAll();
    }

}
