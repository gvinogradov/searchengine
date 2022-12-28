package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "parsing-settings")
public class ParserCfg {
    private String userAgent;
    private String referer;
    private int timeout;
    private int treshhold;
    private int parallelism;
    private int threadDelay;
}
