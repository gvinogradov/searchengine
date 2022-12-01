package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.config.Parser;
import searchengine.config.Site;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;


public class SiteParser extends RecursiveAction {

    private Site site;
    private String path;
    private List<String> urls;
    private final Parser parser;

    public SiteParser(Site site, String path, Parser parser) {
        this.site = site;
        this.path = path;
        this.parser = parser;
        urls = getPage(site.getUrl() + path);
    }

    public boolean isSubURL(String URL, String subURL) {
        String regex = URL + "[-a-zA-Z0-9()@:%_\\+.~#?&//=]+/";
        return subURL.matches(regex) && subURL.startsWith(URL)
                && !subURL.equals(URL);
    }

    private List<String> getPage(String url) {
        try {
            Document document = Jsoup
                    .connect(url)
                    .userAgent(parser.getUserAgent())
                    .referrer(parser.getReferer())
                    .timeout(parser.getTimeout())
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

    private String getPath(String url) {
        return url.replace(site.getUrl(), "");
    }

    @Override
    protected void compute() {
        for (String url : urls) {
            if (!url.startsWith(site.getUrl())) {
                continue;
            }
            String pathOfURL = getPath(url);
            //           Если в базе нет такого пути
            if (isSubURL(site.getUrl() + path, url) ) {
                System.out.println(url);
                SiteParser newTask = new SiteParser(site, pathOfURL, parser);

                try {
                    Thread.sleep(parser.getThreadDelay());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                newTask.fork();
            }
        }
    }
}