package com.qianmua.sign.in;

import com.qianmua.annotation.Log;
import com.qianmua.annotation.MailNotify;
import com.qianmua.constant.AutoManageType;
import com.qianmua.constant.RandomChickenSoup;
import com.qianmua.mail.ExecuteSendMailFunction;
import com.qianmua.mail.MailServer;
import com.qianmua.pojo.User;
import com.qianmua.pojo.vo.AutoWriteDayInfo;
import com.qianmua.pojo.vo.AutoWriteWeekInfo;
import com.qianmua.pojo.vo.LoginVo;
import com.qianmua.pojo.vo.SinginVo;
import com.qianmua.util.CallRequestBack;
import com.qianmua.util.DateFormatUtils;
import com.qianmua.util.JsonUtils;
import com.qianmua.util.NetworkApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

/**
 * @author jinchao.hu
 * @version 1.0
 * @date 2021/1/8  9:30
 * @description : 执行签到
 */
@Component
public class SignInServer {

    @Autowired
    private MailServer mailServer;

    /**
     * BASE API
     */
    private static final String uri = "https://api.moguding.net:9000";

    /**
     * sign with
     */
    @Log(needLog = true)
    @MailNotify
    public synchronized void doSign(LoginVo login, final SinginVo singin) {
        String loginurl = uri + "/session/user/v1/login";
        NetworkApi.request(JsonUtils.serialize(login),
                loginurl,
                "",
                json -> {
                    String token = checkToken(json);
                    checkPlanId(singin );
                    doAutoSign(singin , token );
                    autoWrite(singin, token );
                });
    }

    /**
     * 获取token
     * 可能会抛出用户token 获取异常
     * @param json json 数据
     * @return token
     */
    private String checkToken(String json) {
        User parse = JsonUtils.parse(json, User.class);
        return Optional.ofNullable(parse)
                .map(var1 -> var1.getData().getToken())
                .orElseThrow( () -> new RuntimeException("user token with null."));
    }


    /**
     * 自动日报，周报，月报
     * @param singin 信息实体
     * @param token token
     */
    private void autoWrite(SinginVo singin, String token) {
        String autoWriteUrl = uri + "/practice/paper/v1/save";

        // 日
        if (DateFormatUtils.isDayLast()){
            System.out.println(LocalDateTime.now() + " 日报:");
            doAutoWriteDay(singin, token, autoWriteUrl , AutoManageType.AUTO__WRITE_DAY);
        }else
            System.out.println(LocalDateTime.now() + " 日报条件不足");

        // 周
        if (DateFormatUtils.isThisWeekSaturday()){
            System.out.println(LocalDateTime.now() + " 周报：" );
            doAutoWriteWeek(singin , token , autoWriteUrl);
        }else
            System.out.println(LocalDateTime.now() + " 周报条件不足");

        // 月
        if (DateFormatUtils.isThisMonthLast()){
            System.out.println(LocalDateTime.now().getDayOfMonth() + " 月报: ");
            doAutoWriteDay(singin, token, autoWriteUrl , AutoManageType.AUTO__WRITE_MONTH);
        }else
            System.out.println(LocalDateTime.now() + " 月报条件不足");

    }

    /**
     * 自动日报
     */
    private void doAutoWriteDay(SinginVo singin, String token, String autoWriteUrl ,String type) {
        AutoWriteDayInfo info = new AutoWriteDayInfo();

        info.setAttachmentList(new ArrayList<>())
                .setAttachments("")
                .setContent(getRandomChickenSoup())
                .setPlanId(singin.getPlanId())
                .setReportType(type)
                .setTitle(AutoManageType.AUTO_TITLE);

        NetworkApi.request(
                JsonUtils.serialize(info),
                autoWriteUrl,
                token,
                json1 -> { });

    }

    /**
     * 自动周报
     */
    private void doAutoWriteWeek(SinginVo singinVo , String token , String url){
        AutoWriteWeekInfo weekInfo = new AutoWriteWeekInfo();

        // gen week
        StringBuilder builder = new StringBuilder();
        builder.append("第");
        builder.append(DateFormatUtils.getStartWithEndTime() / 7 );
        builder.append("周");

        weekInfo.setAttachmentList(new ArrayList<>())
                .setAttachments("")
                .setContent(getRandomChickenSoup())
                .setPlanId(singinVo.getPlanId())
                .setReportType(AutoManageType.AUTO__WRITE_WEEK)
                .setTitle(AutoManageType.AUTO_TITLE)
                .setStartTime(DateFormatUtils.getStartDateTime())
                .setEndTime(DateFormatUtils.getEndDateTime())
                .setWeeks(builder.toString());

        NetworkApi.request(JsonUtils.serialize(weekInfo),
                url,
                token,
                json1 -> { });


    }

    /**
     * 自动签到
     * @param singin 信息实体
     * @param token token
     */
    private String doAutoSign(SinginVo singin, String token) {
        String sign = uri + "/attendence/clock/v1/save";

        NetworkApi.request(JsonUtils.serialize(singin), sign, token,
                json1 -> {  });

        return singin.getPlanId();
    }

    /*
    鸡汤 - -！
     */
    private String getRandomChickenSoup(){
        int length = RandomChickenSoup.CHICKEN_SOUP.length;
        int random = new Random().nextInt(length);
        return RandomChickenSoup.CHICKEN_SOUP[random];

    }

    private void checkPlanId(SinginVo singin) {
        Objects.requireNonNull(singin.getPlanId());
    }
}
