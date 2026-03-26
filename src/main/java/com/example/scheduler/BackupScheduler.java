/*
 * BackupScheduler.java
 *
 * Copyright (c) 2025 Nguyen. All rights reserved.
 * This software is the confidential and proprietary information of Nguyen.
 */

package com.example.scheduler;

import com.example.BackupManager;
import com.example.config.BackupConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.File;

/**
 * BackupScheduler.java
 *
 * @author Nguyen
 */
public class BackupScheduler {
    private Scheduler scheduler;

    public BackupScheduler() throws SchedulerException {
        this.scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
    }

    public void scheduleBackup(String cronExpression, String configFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BackupConfig config = mapper.readValue(new File(configFile), BackupConfig.class);

        JobDetail job = JobBuilder.newJob(BackupJob.class)
                .withIdentity("backupJob", "backupGroup")
                .usingJobData("configFile", configFile)
                .build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("backupTrigger", "backupGroup")
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    public static class BackupJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            String configFile = context.getJobDetail().getJobDataMap().getString("configFile");

            try {
                ObjectMapper mapper = new ObjectMapper();
                BackupConfig config = mapper.readValue(new File(configFile), BackupConfig.class);

                BackupManager backupManager = new BackupManager();
                backupManager.performBackup(config);

            } catch (Exception e) {
                throw new JobExecutionException(e);
            }
        }
    }
}
