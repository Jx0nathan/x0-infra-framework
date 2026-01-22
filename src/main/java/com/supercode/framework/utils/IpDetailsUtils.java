package com.supercode.framework.utils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.*;
import com.maxmind.geoip2.record.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Callable;

/**
 * @author :jonathan.ji
 * @date : 2023/8/31
 */
@Log4j2
public class IpDetailsUtils {
    private static DatabaseReader cityReader;
    private static DatabaseReader countryReader;
    private static DatabaseReader connectionTypeReader;
    private static DatabaseReader anonymousIPReader;
    private static DatabaseReader domainReader;
    private static DatabaseReader ispReader;
    private static DatabaseReader asnReader;


    /**
     * 获取Ip所在的城市，返回城市全称
     */
    public static String getCity(String ip) {
        if (ip == null || cityReader == null) {
            return StringUtils.EMPTY;
        } else {
            try {
                InetAddress ipAddress = InetAddress.getByName(ip);
                CityResponse response = cityReader.city(ipAddress);
                City city = response.getCity();
                log.info("City: " + city.getName());
                return city.getName();
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                return StringUtils.EMPTY;
            }
        }
    }

    /**
     * 获取Ip所在的国家，返回国家简写，例如：US
     */
    public static String getCountryShort(String ip2) {
        if (ip2 == null || countryReader == null) {
            return StringUtils.EMPTY;
        } else {
            try {
                InetAddress ipAddress = InetAddress.getByName(ip2);
                Country country = countryReader.country(ipAddress).getCountry();
                log.info("Country: " + country.getIsoCode());
                return country.getIsoCode();
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                return StringUtils.EMPTY;
            }
        }
    }

    /**
     * 获取Ip所在详情
     * 1: 国家
     * 2: 城市
     * 3: 住址
     * 4: 邮箱
     * 5: 经纬度
     * </pre>
     */
    public static IpDetailsUtils.Geoip2Detail getDetail(String ip2) {
        if (ip2 == null) {
            return null;
        } else {
            try {
                InetAddress ipAddress = InetAddress.getByName(ip2);
                CityResponse response = getReaderData(() -> cityReader.city(ipAddress));
                Country country = getReaderData(() -> countryReader.country(ipAddress).getCountry());
                Subdivision subdivision = response.getMostSpecificSubdivision();
                City city = response.getCity();
                Postal postal = response.getPostal();
                Location location = response.getLocation();
                ConnectionTypeResponse.ConnectionType connectionType = getReaderData(() ->
                        connectionTypeReader.connectionType(ipAddress).getConnectionType());
                Continent continent = response.getContinent();
                AnonymousIpResponse anonymousIpResponse = getReaderData(() -> anonymousIPReader.anonymousIp(ipAddress));
                String domain = getReaderData(() -> domainReader.domain(ipAddress).getDomain());
                IspResponse isp = getReaderData(() -> ispReader.isp(ipAddress));
                AsnResponse asn = getReaderData(() -> asnReader.asn(ipAddress));
                Geoip2Detail geoip2Detail = new Geoip2Detail(country, subdivision, city, postal, location, continent, connectionType,
                        anonymousIpResponse, isp, domain, asn);
                log.info(ip2 + country.getIsoCode() + city.getName());
                return geoip2Detail;
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
                return null;
            }
        }
    }

    private static <T> T getReaderData(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static void init(FilePath filePath) {
        if (filePath == null) {
            log.error("初始化 Geoip2 失败 配置路径不存在");
        }
        IpDetailsUtils.cityReader = initReader(filePath.getCityPath());
        IpDetailsUtils.countryReader = initReader(filePath.getCountryPath());
        IpDetailsUtils.connectionTypeReader = initReader(filePath.getConnectionTypePath());
        IpDetailsUtils.asnReader = initReader(filePath.getAsnPath());
        IpDetailsUtils.domainReader = initReader(filePath.getDomainPath());
        IpDetailsUtils.ispReader = initReader(filePath.getIspPath());
        IpDetailsUtils.anonymousIPReader = initReader(filePath.getAnonymousIPPath());
    }

    private static DatabaseReader initReader(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            log.error("geoip2路径为空");
            return null;
        }
        File database = new File(filePath);
        if (database.exists()) {
            try {
                return new DatabaseReader.Builder(database).build();
            } catch (IOException e) {
                log.error("初始化 Geoip2 失败", e);
            }
        } else {
            log.error("初始化 Geoip2 失败 配置路径不存在");
        }
        return null;
    }

    @Data
    @AllArgsConstructor
    public static class FilePath {
        private String cityPath;
        private String countryPath;
        private String connectionTypePath;
        private String anonymousIPPath;
        private String domainPath;
        private String ispPath;
        private String asnPath;

    }

    @AllArgsConstructor
    @Data
    public static class Geoip2Detail {
        // 国家
        private final Country country;
        // 住房
        private final Subdivision subdivision;
        // 城市
        private final City city;
        // 邮政信息（邮编）
        private final Postal Postal;
        // 经纬度
        private final Location Location;
        // 大陆非洲，南极洲，亚洲，欧洲，北美洲，大洋洲，南美洲
        private final Continent continent;
        // 连接类型（Corporate是企业类型、Dialup是拨号上网、Cable/DSL是有线宽带、Cellular是移动蜂窝网络）
        private final ConnectionTypeResponse.ConnectionType connectionType;
        // 匿名ip，里面包含字段有是否匿名vpn、是否为公共代理、是否为tor出口节点、是否为托管提供商等
        private final AnonymousIpResponse anonymousIp;
        // 互联网提供商，organization是提供商组织或者公司名称
        private final IspResponse isp;
        // 域名
        private final String domain;
        // 自治系统（Autonomous System）
        private final AsnResponse asn;


    }
}
