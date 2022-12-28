package searchengine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;

@Repository
public interface SiteDAO extends JpaRepository<Site, Integer> {

    @Query(value = "SELECT * from sites where url LIKE %:url%", nativeQuery = true)
    Site getByUrl(String url);

    @Query(value = "SELECT id from sites where url LIKE %:url% LIMIT 1", nativeQuery = true)
    Integer getSiteIdByUrl(String url);

}
