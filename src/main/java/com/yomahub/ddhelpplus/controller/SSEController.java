package com.yomahub.ddhelpplus.controller;

import com.yomahub.ddhelpplus.sse.SSEManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@Controller
public class SSEController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Map<String, SseEmitter> sseMap = new HashMap<>();

    @RequestMapping(value = "/sse/connect/{clientId}", method = RequestMethod.GET)
    @ResponseBody
    public SseEmitter connect(@PathVariable("clientId")String clientId){
        // 设置超时时间，0表示不过期。默认30秒，超过时间未完成会抛出异常：AsyncRequestTimeoutException
        SseEmitter sseEmitter = new SseEmitter(0L);
        SSEManager.addSse(clientId, sseEmitter);
        return sseEmitter;
    }

    @RequestMapping(value = "/sse/close/{clientId}", method = RequestMethod.GET)
    @ResponseBody
    public String close(@PathVariable("clientId")String clientId){
        SSEManager.close(clientId);
        return "success";
    }

}
