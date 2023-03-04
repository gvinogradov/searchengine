package searchengine.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "pages",
        uniqueConstraints=@UniqueConstraint(columnNames={"site_id", "path"}))
public class Page {

    public Page() {
    }

    public Page(Site site, String path, int code, String content) {
        this.site = site;
        this.path = path;
        this.code = code;
        this.content = content;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

//    @Column(columnDefinition = "TEXT NOT NULL, UNIQUE KEY uk_site_path(site_id,path(500))")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String path;

    @Column(nullable = false)
    private Integer code;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

}
