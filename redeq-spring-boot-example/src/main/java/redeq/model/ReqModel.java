package redeq.model;

import lombok.Data;

import java.util.Map;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/5
 */
@Data
public class ReqModel {
    private String topic;
    private String jobId;
    private Map<String, Object> body;
}
