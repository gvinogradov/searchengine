package searchengine.dto.search;

import lombok.Data;

@Data
public class PageRankImpl implements IPageRank {

    private Integer pageId;
    private Integer lemmaRank;

    public PageRankImpl(Integer pageId, Integer lemmaRank) {
        this.pageId = pageId;
        this.lemmaRank = lemmaRank;
    }

    @Override
    public Integer getPageId() {
        return this.pageId;
    }

    @Override
    public Integer getLemmaRank() {
        return this.lemmaRank;
    }
}
