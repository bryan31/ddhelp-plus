package com.yomahub.ddhelpplus.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.yomahub.ddhelpplus.core.Api;
import com.yomahub.ddhelpplus.core.UserConfig;
import com.yomahub.ddhelpplus.sse.SSEManager;
import com.yomahub.ddhelpplus.vo.CheckVO;
import com.yomahub.ddhelpplus.vo.DDPlusProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Controller
public class DDPlusController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final String PROP_PATH = "prop.json";

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(ModelMap modelMap){
        String path = FileUtil.getAbsolutePath(PROP_PATH);
        if(FileUtil.exist(path)){
            String json = FileUtil.readUtf8String(path);
            DDPlusProp prop = JSON.parseObject(json, DDPlusProp.class);
            modelMap.put("prop", prop);
        }
        return "index";
    }

    @RequestMapping(value = "/check", method = RequestMethod.POST)
    @ResponseBody
    public String check(@RequestBody DDPlusProp prop){
        Api api = new Api(new UserConfig(prop));
        CheckVO checkVO = api.checkUserConfig();

        if(ObjectUtil.isNotNull(checkVO)){
            prop.setStationId(checkVO.getStationId());
            prop.setAddressId(checkVO.getAddressId());
        }

        String path = FileUtil.getAbsolutePath(PROP_PATH);
        FileUtil.writeUtf8String(JSON.toJSONString(prop),new File(path));
        return JSON.toJSONString(prop);
    }

    @RequestMapping(value = "/runTest", method = RequestMethod.POST)
    @ResponseBody
    public String runTest(@RequestBody DDPlusProp prop){
        Api api = new Api(new UserConfig(prop));
        runTest(api);
        String path = FileUtil.getAbsolutePath(PROP_PATH);
        FileUtil.writeUtf8String(JSON.toJSONString(prop),new File(path));
        return JSON.toJSONString(prop);
    }

    @RequestMapping(value = "/run", method = RequestMethod.POST)
    @ResponseBody
    public String run(@RequestBody DDPlusProp prop){
        Api api = new Api(new UserConfig(prop));
        run(api,1);
        String path = FileUtil.getAbsolutePath(PROP_PATH);
        FileUtil.writeUtf8String(JSON.toJSONString(prop),new File(path));
        return JSON.toJSONString(prop);
    }

    @RequestMapping(value = "/run6", method = RequestMethod.POST)
    @ResponseBody
    public String run6(@RequestBody DDPlusProp prop){
        Api api = new Api(new UserConfig(prop));
        run(api,2);
        String path = FileUtil.getAbsolutePath(PROP_PATH);
        FileUtil.writeUtf8String(JSON.toJSONString(prop),new File(path));
        return JSON.toJSONString(prop);
    }

    @RequestMapping(value = "/run8", method = RequestMethod.POST)
    @ResponseBody
    public String run8(@RequestBody DDPlusProp prop){
        Api api = new Api(new UserConfig(prop));
        run(api,3);
        String path = FileUtil.getAbsolutePath(PROP_PATH);
        FileUtil.writeUtf8String(JSON.toJSONString(prop),new File(path));
        return JSON.toJSONString(prop);
    }

    //并发执行策略
    //mode设置1 人工模式 运行程序则开始抢
    //mode设置2 时间触发 运行程序后等待早上5点59分30秒开始
    //mode设置3 时间触发 运行程序后等待早上8点29分30秒开始
    private void run(Api api, int mode){
        //此为高峰期策略 通过同时获取或更新 购物车、配送、订单确认信息再进行高并发提交订单
        //一定要注意 并发量过高会导致被风控 请合理设置线程数、等待时间和执行时间 不要长时间的执行此程序（我配置的线程数和间隔 2分钟以内）
        //如果想等过高峰期后进行简陋 长时间执行 则将线程数改为1  间隔时间改为10秒以上 并发越小越像真人 不会被风控  要更真一点就用随机数（自己处理）



        //最小订单成交金额 举例如果设置成50 那么订单要超过50才会下单
        double minOrderPrice = 0;

        //基础信息执行线程数
        int baseTheadSize = 2;

        //提交订单执行线程数
        int submitOrderTheadSize = 4;

        //取随机数
        //请求间隔时间最小值
        int sleepMillisMin = 300;
        //请求间隔时间最大值
        int sleepMillisMax = 500;


        //5点59分30秒时间触发
        while (mode == 2 && !timeTrigger(5, 59, 30)) {
        }

        //8点29分30秒时间触发
        while (mode == 3 && !timeTrigger(8, 29, 30)) {
        }


        //保护线程 2分钟未下单自动终止 避免对叮咚服务器造成压力 也避免封号  如果想长时间执行 请使用Sentinel哨兵模式
        new Thread(() -> {
            for (int i = 0; i < 120 && !api.context.containsKey("end"); i++) {
                sleep(1000);
            }
            if (!api.context.containsKey("end")) {
                api.context.put("end", new HashMap<>());
                sleep(3000);
                print(false,"未成功下单，执行2分钟自动停止");
            }
        }).start();

        for (int i = 0; i < baseTheadSize; i++) {
            new Thread(() -> {
                while (!api.context.containsKey("end")) {
                    api.allCheck();
                    //此接口作为补充使用 并不是一定需要 所以执行间隔拉大一点
                    sleep(RandomUtil.randomInt(3000, 5000));
                }
            }).start();
        }

        for (int i = 0; i < baseTheadSize; i++) {
            new Thread(() -> {
                while (!api.context.containsKey("end")) {
                    Map<String, Object> cartMap = api.getCart(mode == 2 || mode == 3);
                    if (cartMap != null) {
                        if (Double.parseDouble(cartMap.get("total_money").toString()) < minOrderPrice) {
                            print(false,"订单金额：" + cartMap.get("total_money").toString() + " 不满足最小金额设置：" + minOrderPrice + " 继续重试");
                        } else {
                            api.context.put("cartMap", cartMap);
                        }
                    }
                    sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                }
            }).start();
        }
        for (int i = 0; i < baseTheadSize; i++) {
            new Thread(() -> {
                while (!api.context.containsKey("end")) {
                    sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                    if (api.context.get("cartMap") == null) {
                        continue;
                    }
                    Map<String, Object> multiReserveTimeMap = api.getMultiReserveTime(api.userConfig.addressId, api.context.get("cartMap"));
                    if (multiReserveTimeMap != null) {
                        api.context.put("multiReserveTimeMap", multiReserveTimeMap);
                    }
                }
            }).start();
        }
        for (int i = 0; i < baseTheadSize; i++) {
            new Thread(() -> {
                while (!api.context.containsKey("end")) {
                    sleep(RandomUtil.randomInt(sleepMillisMin, sleepMillisMax));
                    if (api.context.get("cartMap") == null || api.context.get("multiReserveTimeMap") == null) {
                        continue;
                    }
                    Map<String, Object> checkOrderMap = api.getCheckOrder(api.userConfig.addressId, api.context.get("cartMap"), api.context.get("multiReserveTimeMap"));
                    if (checkOrderMap != null) {
                        api.context.put("checkOrderMap", checkOrderMap);
                    }
                }
            }).start();
        }
        for (int i = 0; i < submitOrderTheadSize; i++) {
            new Thread(() -> {
                while (!api.context.containsKey("end")) {
                    if (api.context.get("cartMap") == null || api.context.get("multiReserveTimeMap") == null || api.context.get("checkOrderMap") == null) {
                        continue;
                    }
                    if (api.addNewOrder(api.userConfig.addressId, api.context.get("cartMap"), api.context.get("multiReserveTimeMap"), api.context.get("checkOrderMap"))) {
                        print(true,"铃声持续1分钟，终止程序即可，如果还需要下单再继续运行程序");
                        api.play();
                    }
                }
            }).start();
        }
    }

    private void runTest(Api api){
        if (api.userConfig.addressId.length() == 0) {
            print(false,"请先执行UserConfig获取配送地址id");
            return;
        }

        // 此为单次执行模式  用于在非高峰期测试下单  也必须满足3个前提条件  1.有收货地址  2.购物车有商品 3.能选择配送信息
        api.allCheck();
        Map<String, Object> cartMap = api.getCart(false);
        if (cartMap == null) {
            return;
        }
        Map<String, Object> multiReserveTimeMap = api.getMultiReserveTime(api.userConfig.addressId, cartMap);
        if (multiReserveTimeMap == null) {
            return;
        }
        Map<String, Object> checkOrderMap = api.getCheckOrder(api.userConfig.addressId, cartMap, multiReserveTimeMap);
        if (checkOrderMap == null) {
            return;
        }
        api.addNewOrder(api.userConfig.addressId, cartMap, multiReserveTimeMap, checkOrderMap);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

    private boolean timeTrigger(int hour, int minute, int second) {
        sleep(1000);
        int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int currentSecond = Calendar.getInstance().get(Calendar.SECOND);
        print(true,"时间触发 当前时间 " + currentHour + ":" + currentMinute + ":" + currentSecond + " 目标时间 " + hour + ":" + minute + ":" + second);
        return currentHour == hour && currentMinute == minute && currentSecond >= second;
    }

    private void print(boolean normal, String message) {
        if (normal) {
            System.out.println(message);
            SSEManager.send(message);
        } else {
            System.err.println(message);
            SSEManager.send(StrUtil.format("<font color='red'>{}</font>",message));
        }
    }
}
