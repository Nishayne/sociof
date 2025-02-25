package com.hashedin.huSpark.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler for running jobs
 */
@Component
public class JobScheduler {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job birthdayPostJob;

    /**
     * Run the birthday post job daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleBirthdayPostJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(birthdayPostJob, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
