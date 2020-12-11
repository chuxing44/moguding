package com.qianmua.job;


import com.qianmua.service.SignService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class MyCronJob extends QuartzJobBean {

    @Autowired
    private SignService signService;

    /**
     * 晚班签到
     */
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        try {
            signService.sign();
        } catch (InterruptedException e) {
            System.err.println("定时任务异常：");
            e.printStackTrace();
        }

    }

}