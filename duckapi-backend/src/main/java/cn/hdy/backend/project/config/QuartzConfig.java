package cn.hdy.backend.project.config;

import cn.hdy.backend.project.job.GoldCoinGoodsOrderJobDetail;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import java.util.Date;

/**
 * @author 滴滴鸭
 *
 * 定时任务配置
 */
@Configuration
public class QuartzConfig {
    /**
     * 定时任务
     */
    @Bean(name = "goldCoinGoodsOrderJobDetailTask")
    public MethodInvokingJobDetailFactoryBean goldCoinGoodsOrderJobDetail(GoldCoinGoodsOrderJobDetail goldCoinGoodsOrderJobDetail){
        MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
        jobDetail.setConcurrent(true);
        jobDetail.setTargetObject(goldCoinGoodsOrderJobDetail);
        jobDetail.setTargetMethod("doJob");
        return jobDetail;
    }

    /**
     * 定时任务触发器
     */
    @Bean(name = "goldCoinGoodsOrderJobDetailTrigger")
    public SimpleTriggerFactoryBean goldCoinGoodsOrderJobDetailTrigger(JobDetail goldCoinGoodsOrderJobDetailTask){
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        triggerFactoryBean.setJobDetail(goldCoinGoodsOrderJobDetailTask);
        triggerFactoryBean.setStartDelay(0);
        triggerFactoryBean.setStartTime(new Date());
        //每隔30s执行一次
        triggerFactoryBean.setRepeatInterval(30 * 1000);
        return triggerFactoryBean;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(Trigger goldCoinGoodsOrderJobDetailTrigger){
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        // 设置1分钟后执行
        schedulerFactoryBean.setStartupDelay(60);
        schedulerFactoryBean.setTriggers(goldCoinGoodsOrderJobDetailTrigger);
        return schedulerFactoryBean;
    }
}
