package com.supercode.framework.config;

import com.supercode.framework.utils.IpDetailsUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;


/**
 * @author Jonathan
 * @date : 2023/9/1
 */
@Configuration
@Log4j2
public class IpDetailConfiguration {
    private static final String BASEPATH = "/opt/app/maxmind/";
    private static final String cityPath = BASEPATH + "GeoIP2-City.mmdb";
    private static final String countryPath = BASEPATH + "GeoIP2-Country.mmdb";
    private static final String connectionTypePath = BASEPATH + "GeoIP2-Connection-Type.mmdb";
    private static final String anonymousIPPath = BASEPATH + "GeoIP2-Anonymous-IP.mmdb";
    private static final String domainPath = BASEPATH + "GeoIP2-Domain.mmdb";
    private static final String ispPath = BASEPATH + "GeoIP2-ISP.mmdb";
    private static final String asnPath = BASEPATH + "GeoLite2-ASN.mmdb";

    @PostConstruct
    private void init() {
        IpDetailsUtils.FilePath filePath = new IpDetailsUtils.FilePath(cityPath,
                countryPath, connectionTypePath, anonymousIPPath, domainPath, ispPath, asnPath);
        IpDetailsUtils.init(filePath);
        log.info("初始化 Geoip2结束");
    }
}