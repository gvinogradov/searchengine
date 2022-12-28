package searchengine.services;

import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.config.ParserCfg;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

public class SiteRecursiveReader extends RecursiveAction {

    private volatile Site site;
    private List<String> urls;
    private static PageService pageService;
    private static ParserCfg parserCfg;


    public SiteRecursiveReader(Site site, List<String> urls) {
        this.site = site;
        this.urls = urls;
    }

    public SiteRecursiveReader(Site site, List<String> urls, PageService pageService, ParserCfg parserCfg) {
        this(site, urls);
        SiteRecursiveReader.pageService = pageService;
        SiteRecursiveReader.parserCfg = parserCfg;
    }


    public boolean isSubURL(String URL, String subURL) {
        String regex = URL + "[-a-zA-Z0-9()@:%_\\+.~#?&//=]*/";
        return subURL.matches(regex) &&
                subURL.startsWith(URL)
                && !subURL.equals(URL);
    }

    private List<String> getURLs(String url) {
        try {
            Document document = Jsoup
                    .connect(url)
                    .userAgent(SiteRecursiveReader.parserCfg.getUserAgent())
                    .referrer(SiteRecursiveReader.parserCfg.getReferer())
                    .timeout(SiteRecursiveReader.parserCfg.getTimeout())
                    .get();

            Elements elements = document.select("a");
            return elements.stream()
                    .map(e -> e.absUrl("href"))
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }



    private void computeDirectly(List<String> urls) {
        List<String> subURLs = new ArrayList<>();

        for (String url : urls) {
            if (!isSubURL(site.getUrl(), url)) {
                continue;
            }
            String path = url.substring(site.getUrl().length());
            //todo: Написать проверку записи в БД

            if (!SiteRecursiveReader.pageService.existPagePath(site.getId(), path)) {
                //todo: добавить запись в БД и выполнить рекурсивный обход
                subURLs.addAll(getURLs(url));
                Page page = new Page();
                page.setSite(site);
                page.setCode(200);
                page.setContent("123");
                page.setPath(path);
                SiteRecursiveReader.pageService.add(page);
                System.out.println(url + " - add to DB");
            }
        }

        try {
            Thread.sleep(SiteRecursiveReader.parserCfg.getThreadDelay());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }

        SiteRecursiveReader newTask = new SiteRecursiveReader(site, subURLs);
        newTask.fork();
        newTask.join();
    }

    private List<SiteRecursiveReader> createSubtasks(List<String> urls) {
        int len = urls.size();
        List<SiteRecursiveReader> subTasks = new ArrayList<>();
        List<String> firstHalfUrls = new ArrayList<>(urls.subList(0, len / 2));
        List<String> secondHalfUrls = new ArrayList<>(urls.subList(len / 2, len));
        subTasks.add(new SiteRecursiveReader(site, firstHalfUrls));
        subTasks.add(new SiteRecursiveReader(site, secondHalfUrls));
        return subTasks;
    }

    @Override
    protected void compute() {
        if (urls.size() > SiteRecursiveReader.parserCfg.getTreshhold()) {
            ForkJoinTask.invokeAll(createSubtasks(urls));
        } else {
            computeDirectly(urls);
        }
    }
}