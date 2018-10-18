package com.jaspercloud.tcc.client.test;

import com.jaspercloud.tcc.core.exception.TccException;
import org.apache.commons.lang3.RandomUtils;

public class RandomTccException extends TccException {

    public RandomTccException() {
    }

    public RandomTccException(String message) {
        super(message);
    }

    public RandomTccException(String message, Throwable cause) {
        super(message, cause);
    }

    public RandomTccException(Throwable cause) {
        super(cause);
    }

    public static void randomException(int percent) {
        int result = 100 - percent;
        int rand = RandomUtils.nextInt(0, 100);
        if (rand >= result) {
            throw new RandomTccException();
        }
    }
}
