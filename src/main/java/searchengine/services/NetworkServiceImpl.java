package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.config.ParserCfg;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NetworkServiceImpl implements NetworkService {

    private final ParserCfg parserCfg;

    @Override
    public Connection.Response getResponse(String url) {
        Connection.Response response = null;
        try {
            response = Jsoup.connect(url)
                    .userAgent(parserCfg.getUserAgent())
                    .ignoreContentType(true)
                    .referrer(parserCfg.getReferer())
                    .timeout(parserCfg.getTimeout())
                    .execute();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return response;
    }

    @Override
    public boolean checkSiteConnection(String url) {
        Connection.Response response = getResponse(url);
        return response == null ?
                false : response.statusCode() == HttpStatus.OK.value();
    }

    @Override
    public boolean isAvailableContent(Connection.Response response) {
        return ((response != null)
                && (response.statusCode() == HttpStatus.OK.value())
                && (response.contentType().equalsIgnoreCase(parserCfg.getContentType())));
    }
}
