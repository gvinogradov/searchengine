package searchengine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import searchengine.model.Page;

import java.util.List;

public interface PageDAO extends JpaRepository<Page, Integer> {

    @Query(value = "SELECT * from pages where site_id = :siteId AND path LIKE %:path%", nativeQuery = true)
    List<Page> getPagesByPath(int siteId, String path);

}
