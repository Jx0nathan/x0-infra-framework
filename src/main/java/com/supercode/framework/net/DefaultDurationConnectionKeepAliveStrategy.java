package com.supercode.framework.net;

import org.apache.http.HttpResponse;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * @Author: jonathan.ji
 * @Date: 2023/9/11 14:54
 * 防止有的服务端在关闭连接之前不会发送FIN报文，这种情况可能比较少见，为了防止这种情况，我们对所有请求都设置一个timeout时间，设置的策略为：
 * 1、如果response中有timeout，则设置response秒
 * 2、如果response中没有timeout，httpclient提供的默认的是返回-1，我们设置为60-10秒
 */
@Contract(threading = ThreadingBehavior.IMMUTABLE)
public class DefaultDurationConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {
    private final long defaultTimeout = 50 * 1000;

    @Override
    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        long duration = DefaultConnectionKeepAliveStrategy.INSTANCE
                .getKeepAliveDuration(response, context);
        return duration > 0 ? duration : defaultTimeout;
    }
}
