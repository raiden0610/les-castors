package fr.castor.ws.quartz;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import fr.castor.ws.enums.PropertiesFileWS;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.castor.ws.quartz.jobs.AnnonceGarbageCollector;

@Startup
@Singleton
public class SchedulerBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerBean.class);

    private Scheduler scheduler;

    @Inject
    private JobFactory cdiJobFactory;

    @PostConstruct
    public void scheduleJobs() {

        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.setJobFactory(cdiJobFactory);

            JobKey annonceGarbageCollectorJobKey = JobKey.jobKey("annonceGarbageCollectorJob",
                    "annonceGarbageCollectorJob-Group");
            JobDetail annonceGarbageCollectorJob = JobBuilder.newJob(AnnonceGarbageCollector.class)
                    .withIdentity(annonceGarbageCollectorJobKey).build();

            Properties jobsProperties = PropertiesFileWS.JOBS.getProperties();

            TriggerKey annonceGarbageCollectorJobTriggerKey = TriggerKey.triggerKey(
                    "annonceGarbageCollectorJobTriggerKey", "annonceGarbageCollectorJob-Group");
            Trigger annonceGarbageCollectorJobTrigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity(annonceGarbageCollectorJobTriggerKey)
                    .startNow()
                    .withSchedule(
                            CronScheduleBuilder.cronSchedule(jobsProperties
                                    .getProperty("annonceGarbageCollectorJobTrigger.cron"))).build();

            scheduler.start(); // start before scheduling jobs
            scheduler.scheduleJob(annonceGarbageCollectorJob, annonceGarbageCollectorJobTrigger);

            printJobsAndTriggers(scheduler);

        } catch (SchedulerException e) {
            LOGGER.error("Error while creating scheduler", e);
        }
    }

    private void printJobsAndTriggers(Scheduler scheduler) throws SchedulerException {
        LOGGER.info("Quartz Scheduler: {}", scheduler.getSchedulerName());
        for (String group : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey> groupEquals(group))) {
                LOGGER.info("Found job identified by {}", jobKey);
            }
        }
        for (String group : scheduler.getTriggerGroupNames()) {
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.<TriggerKey> groupEquals(group))) {
                LOGGER.info("Found trigger identified by {}", triggerKey);
            }
        }
    }

    @PreDestroy
    public void stopJobs() {
        if (scheduler != null) {
            try {
                scheduler.shutdown(false);
            } catch (SchedulerException e) {
                LOGGER.error("Error while closing scheduler", e);
            }
        }
    }

}
