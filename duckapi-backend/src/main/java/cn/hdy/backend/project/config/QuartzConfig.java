package cn.hdy.backend.project.config;

import cn.hdy.backend.project.job.InvokeCountOrderJobDetail;
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
    @Bean(name = "invokeCountOrderJobDetailTask")
    public MethodInvokingJobDetailFactoryBean invokeCountOrderJobDetail(InvokeCountOrderJobDetail invokeCountOrderJobDetail){
        MethodInvokingJobDetailFactoryBean jobDetail = new MethodInvokingJobDetailFactoryBean();
        jobDetail.setConcurrent(true);
        jobDetail.setTargetObject(invokeCountOrderJobDetail);
        jobDetail.setTargetMethod("doJob");
        return jobDetail;
    }

    /**
     * 定时任务触发器
     */
    @Bean(name = "invokeCountOrderJobDetailTrigger")
    public SimpleTriggerFactoryBean invokeCountOrderJobDetailTrigger(JobDetail invokeCountOrderJobDetailTask){
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        triggerFactoryBean.setJobDetail(invokeCountOrderJobDetailTask);
        triggerFactoryBean.setStartDelay(0);
        triggerFactoryBean.setStartTime(new Date());
        //每隔2分钟执行一次
        triggerFactoryBean.setRepeatInterval(2 * 60 * 1000);
        return triggerFactoryBean;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(Trigger invokeCountOrderJobDetailTrigger){
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        // 设置1分钟后执行
        schedulerFactoryBean.setStartupDelay(60);
        schedulerFactoryBean.setTriggers(invokeCountOrderJobDetailTrigger);
        return schedulerFactoryBean;
    }
}
