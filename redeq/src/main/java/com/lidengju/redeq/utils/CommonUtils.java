package com.lidengju.redeq.utils;

import com.lidengju.redeq.constant.ErrorCodeEnum;
import com.lidengju.redeq.exception.RedeqException;
import org.redisson.api.BatchResult;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/5
 */
public class CommonUtils {

    private static final Clock clock = Clock.systemDefaultZone();

    private CommonUtils() {
    }

    /**
     * @return now in millisecond
     */
    public static long nowInMillis() {
        return Instant.now(clock).toEpochMilli();
    }

    /**
     * check a string if empty
     *
     * @param obj - String
     * @return boolean
     */
    public static boolean isEmpty(String obj) {
        if (obj == null) {
            return true;
        }
        return obj.isEmpty();
    }

    /**
     * check a List if empty
     *
     * @param obj - List of Object
     * @return boolean
     */
    public static boolean isEmpty(List<Object> obj) {
        if (obj == null) {
            return true;
        }
        return obj.isEmpty();
    }

    /**
     * check if redis executed success
     *
     * @param result - Batch execution result
     * @throws RedeqException throw when redis execution return false
     */
    public static void checkSuccess(BatchResult<?> result) throws RedeqException {
        for (Object flag : result.getResponses()) {
            if (flag instanceof Boolean && Boolean.FALSE.equals(flag)) {
                throw new RedeqException(ErrorCodeEnum.REDIS_ERROR);
            }
        }
    }
}
