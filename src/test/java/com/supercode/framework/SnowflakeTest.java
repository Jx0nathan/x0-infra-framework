package com.supercode.framework;

import com.supercode.framework.algorithms.id.Snowflake;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SnowflakeTest {

    private static final Set<Long> IDS = Sets.newConcurrentHashSet();


    @Test
    public void test() {
        for (int i = 0; i < 100; i++) {
            if (!IDS.add(Snowflake.generate())) {
                System.out.println("重复了!!!");
            }
        }
    }


    @Test
    public void test2() {
        ExecutorService service = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 10; i++) {
            service.execute(new IdRun());
        }

    }

    class IdRun implements Runnable {
        @Override
        public void run() {
            for (int j = 0; j < 100; j++) {
                if (!IDS.add(Snowflake.generate())) {
                    System.out.println("重复了!!!");
                }
            }
        }
    }

}
