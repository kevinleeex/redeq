package com.lidengju.redeq.constant;

/**
 * Constants for ReDeQ
 *
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * @date 2021/9/4
 **/
public class RedeqConstants {
    private RedeqConstants() {

    }

    /**
     * the prefix of job pool key.
     */
    public static final String JOB_POOL_KEY_PRE = "REDEQ:JOB_POOL";
    /**
     * the prefix of bucket Queue(zset bucket).
     */
    public static final String ZSET_BUCKET_PRE = "REDEQ:BUCKET";
    /**
     * the prefix of ready queue for consuming.
     */
    public static final String READY_QUEUE_PRE = "REDEQ:READY_QUEUE:";
    /**
     * the key of lock for pulling job to Ready Queue.
     */
    public static final String PULLING_JOB_LOCK = "REDEQ:PULLING_JOB_LOCK";
    /**
     * the key of lock to add a job to Job Pool and Bucket Queue.
     */
    public static final String ADD_JOB_LOCK = "REDEQ:ADD_JOB_LOCK";
    /**
     * the key of lock to remove a job from Job Pool and Bucket Queue.
     */
    public static final String REMOVE_JOB_LOCK = "REDEQ:REMOVE_JOB_LOCK";
    /**
     * the key of lock for consuming a task in Ready Queue.
     */
    public static final String CONSUME_TASK_LOCK = "REDEQ:CONSUME_TASK_LOCK:";
    /**
     * separator (\x1C\x1D) between topic and jobId
     */
    public static final String SEP = String.format("%c%c", (char) 28, (char) 29);
}
