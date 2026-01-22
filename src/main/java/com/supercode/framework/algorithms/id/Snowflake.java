package com.supercode.framework.algorithms.id;

import com.google.common.base.Preconditions;
import com.supercode.master.utils.net.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.Calendar;

/**
 * 增加work-id位数后的snowflake算法
 *
 * @author jonathan
 */
public class Snowflake {
    private static final long EPOCH;
    private static final long SEQUENCE_BITS = 12L;
    private static final long WORKER_ID_BITS = 10L;
    private static final long SEQUENCE_MASK = (1 << SEQUENCE_BITS) - 1;
    private static final long WORKER_ID_LEFT_SHIFT_BITS = SEQUENCE_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT_BITS = WORKER_ID_LEFT_SHIFT_BITS + WORKER_ID_BITS;
    private static final long WORKER_ID_MAX_VALUE = 1L << WORKER_ID_BITS;
    private static final long NEW_SEQUENCE_BITS = 8L;
    private static final long NEW_WORKER_ID_BITS = 16L;
    private static final long NEW_SEQUENCE_MASK = (1 << NEW_SEQUENCE_BITS) - 1;
    private static final long NEW_WORKER_ID_LEFT_SHIFT_BITS = NEW_SEQUENCE_BITS;
    private static final long NEW_TIMESTAMP_LEFT_SHIFT_BITS = NEW_WORKER_ID_LEFT_SHIFT_BITS + NEW_WORKER_ID_BITS;
    private static final TimeService timeService = new TimeService();
    private static final int maxTolerateTimeDifferenceMilliseconds = 10;
    private static final Logger log = LoggerFactory.getLogger(Snowflake.class);
    private static volatile long workerId;
    private static byte sequenceOffset;
    private static long sequence;
    private static long lastMilliseconds;

    static {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2016, Calendar.NOVEMBER, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        EPOCH = calendar.getTimeInMillis();
        workerId = genWorkerIdFromIp();
    }

    private Snowflake() {
    }

    public static synchronized Long generate() {
        long currentMilliseconds = timeService.getCurrentMillis();
        if (waitTolerateTimeDifferenceIfNeed(currentMilliseconds)) {
            currentMilliseconds = timeService.getCurrentMillis();
        }
        if (lastMilliseconds == currentMilliseconds) {
            if (0L == (sequence = (sequence + 1) & getSequenceMask())) {
                currentMilliseconds = waitUntilNextTime(currentMilliseconds);
            }
        } else {
            vibrateSequenceOffset();
            sequence = sequenceOffset;
        }
        lastMilliseconds = currentMilliseconds;
        if (workerId < WORKER_ID_MAX_VALUE) {
            // algorithm: timestamp(41bit)|workerid(10bit)|sequence(12bit)
            return ((currentMilliseconds - EPOCH) << TIMESTAMP_LEFT_SHIFT_BITS) | (workerId << WORKER_ID_LEFT_SHIFT_BITS) | sequence;
        } else {// workerId < NEW_WORKER_ID_MAX_VALUE(65536)
            // use new algorithm: timestamp(39bit)|workerid(16bit)|sequence(8bit)
            return ((currentMilliseconds - EPOCH) << NEW_TIMESTAMP_LEFT_SHIFT_BITS) | (workerId << NEW_WORKER_ID_LEFT_SHIFT_BITS) | sequence;
        }
    }

    private static long genWorkerIdFromIp() {
        InetAddress address = NetUtil.getLocalAddress();
        byte[] ipAddressByteArray = address.getAddress();
        int ipAddressLength = ipAddressByteArray.length;
        if (ipAddressLength != 4 && ipAddressLength != 16) {
            throw new IllegalStateException("Bad LocalHost InetAddress, please check your network!");
        } else {
            long workerId = ((ipAddressByteArray[ipAddressLength - 2] & 255) << 8) + (ipAddressByteArray[ipAddressLength - 1] & 255);
            workerId &= 65535L;
            log.info("Platform snowflake workId: {}, localIp:{}", workerId, NetUtil.getLocalHost());
            return workerId;
        }
    }

    private static long getSequenceMask() {
        if (workerId < WORKER_ID_MAX_VALUE) {
            // algorithm: timestamp(41bit)|workerid(10bit)|sequence(12bit)
            return SEQUENCE_MASK;
        } else {// workerId < NEW_WORKER_ID_MAX_VALUE(65536)
            // use new algorithm: timestamp(39bit)|workerid(16bit)|sequence(8bit)
            return NEW_SEQUENCE_MASK;
        }
    }

    private static boolean waitTolerateTimeDifferenceIfNeed(final long currentMilliseconds) {
        if (lastMilliseconds <= currentMilliseconds) {
            return false;
        }
        long timeDifferenceMilliseconds = lastMilliseconds - currentMilliseconds;
        Preconditions.checkState(timeDifferenceMilliseconds < maxTolerateTimeDifferenceMilliseconds,
                "Clock is moving backwards, last time is %d milliseconds, current time is %d milliseconds", lastMilliseconds, currentMilliseconds);
        try {
            Thread.sleep(timeDifferenceMilliseconds);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private static long waitUntilNextTime(final long lastTime) {
        long result = timeService.getCurrentMillis();
        while (result <= lastTime) {
            result = timeService.getCurrentMillis();
        }
        return result;
    }

    private static void vibrateSequenceOffset() {
        sequenceOffset = (byte) (~sequenceOffset & 1);
    }

    static class TimeService {
        long getCurrentMillis() {
            return System.currentTimeMillis();
        }
    }

}
