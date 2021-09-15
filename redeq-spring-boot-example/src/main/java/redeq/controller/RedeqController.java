package redeq.controller;

import com.lidengju.redeq.api.RedeqClient;
import com.lidengju.redeq.model.DelayedJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import redeq.model.ReqModel;

/**
 * @author Li, Dengju(hello@lidengju.com)
 * @version 1.0
 * Created on 2021/9/5
 */
@RestController
public class RedeqController {
    @Autowired
    private RedeqClient redeqClient;

    @PostMapping("/job")
    public String addJob(@RequestBody ReqModel reqModel) {
        DelayedJob job = (new DelayedJob.Builder()).withBase(reqModel.getTopic()).withBody(reqModel.getBody()).build();
        redeqClient.add(job);
        return "Added a job";
    }

    @DeleteMapping("/job")
    public String deleteJob(@RequestBody ReqModel reqModel) {
        DelayedJob job = (new DelayedJob.Builder()).withBase(reqModel.getTopic(),reqModel.getJobId()).withBody(reqModel.getBody()).build();
        redeqClient.remove(job);
        return "Deleted a job";
    }

}
