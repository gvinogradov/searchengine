package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {

    @Query(value = "SELECT * from pages where site_id = :siteId AND path LIKE :path LIMIT 1", nativeQuery = true)
    Page getPagesByPath(int siteId, String path);

    @Query(value = "SELECT COUNT(*) from pages where site_id = :siteId", nativeQuery = true)
    Integer getPagesCount(int siteId);

}
