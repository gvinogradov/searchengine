package searchengine.services;

import org.jsoup.Connection;
import searchengine.config.ParserCfg;

public interface NetworkService {

    Connection.Response getResponse(String url);
    boolean checkSiteConnection(String url);
    boolean isAvailableContent(Connection.Response response);

}
