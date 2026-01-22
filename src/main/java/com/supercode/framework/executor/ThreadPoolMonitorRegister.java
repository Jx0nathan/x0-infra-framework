package com.supercode.framework.executor;

import com.dianping.cat.status.StatusExtension;
import com.dianping.cat.status.StatusExtensionRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolMonitorRegister {
    public static void registerCatHeartbeat(ThreadPoolExecutor executor, String executorName) {
        StatusExtensionRegister.getInstance().register(new StatusExtension() {
            @Override
            public String getId() {
                return "Custom threadPool ".concat(executorName);
            }

            @Override
            public String getDescription() {
                return "Custom threadPool ".concat(executorName);
            }

            @Override
            public Map<String, String> getProperties() {
                Map<String, String> map = new HashMap<>(5);
                map.put("largest-pool-size", String.valueOf(executor.getLargestPoolSize()));
                map.put("max-pool-size", String.valueOf(executor.getMaximumPoolSize()));
                map.put("core-pool-size", String.valueOf(executor.getCorePoolSize()));
                map.put("current-pool-size", String.valueOf(executor.getPoolSize()));
                map.put("queue-size", String.valueOf(executor.getQueue().size()));
                return map;
            }
        });
    }
}
