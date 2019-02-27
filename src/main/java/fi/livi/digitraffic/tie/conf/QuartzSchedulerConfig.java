package fi.livi.digitraffic.tie.conf;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import fi.livi.digitraffic.tie.metadata.quartz.AutowiringSpringBeanJobFactory;
import fi.livi.digitraffic.tie.metadata.quartz.CameraMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.CameraStationsStatusMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.Datex2RoadworksMessageUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.Datex2TrafficAlertMessageUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.Datex2WeightRestrictionsMessageUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.ForecastSectionCoordinatesMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.ForecastSectionV2DataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.ForecastSectionV2MetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.ForecastSectionWeatherUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.LocationMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.TmsStationMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.TmsStationSensorConstantsMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.TmsStationsStatusMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.WeatherStationMetadataUpdateJob;
import fi.livi.digitraffic.tie.metadata.quartz.WeatherStationsStatusMetadataUpdateJob;

@Configuration
@ConditionalOnProperty(name = "dt.job.scheduler.enabled")
@ConditionalOnNotWebApplication
public class QuartzSchedulerConfig {
    private static final Logger log = LoggerFactory.getLogger(QuartzSchedulerConfig.class);
    private final Environment environment;

    private final static String JOB_SCHEDLULE_STRING_FORMAT = "dt.job.%s.schedule";

    public QuartzSchedulerConfig(final Environment environment) {
        this.environment = environment;
    }

    @Bean
    public JobFactory jobFactory(final ApplicationContext applicationContext) {
        final AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    @QuartzDataSource
    public DataSource quartzDataSource(final @Value("${road.datasource.url}") String url,
        final @Value("${road.datasource.username}") String username,
        final @Value("${road.datasource.password}") String password) {

        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(12);

        config.setMaxLifetime(570000);
        config.setIdleTimeout(500000);
        config.setConnectionTimeout(60000);
        config.setPoolName("quartz_pool");

        // register mbeans for debug
        config.setRegisterMbeans(true);

        // Auto commit must be true for Quartz
        config.setAutoCommit(true);

        return new HikariDataSource(config);
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(final DataSource quartzDataSource,
                                                     final JobFactory jobFactory,
                                                     final Optional<List<Trigger>> triggerBeans) throws IOException {
        final SchedulerFactoryBean factory = new SchedulerFactoryBean() {
            @Override
            protected Scheduler createScheduler(SchedulerFactory schedulerFactory, String schedulerName) throws SchedulerException {
                Scheduler scheduler = super.createScheduler(schedulerFactory, schedulerName);

                final List<Trigger> triggers = triggerBeans.isPresent() ? triggerBeans.get() : Collections.emptyList();
                final Set<JobKey> jobKeys = triggers.stream().map(f -> f.getJobKey()).collect(Collectors.toSet());

                // Remove jobs from the db that are not in current apps job list
                for (String groupName : scheduler.getJobGroupNames()) {
                    for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                        if (!jobKeys.contains(jobKey)) {
                            try {
                                log.error("Deleting job={}", jobKey);
                                scheduler.deleteJob(jobKey);
                            } catch (SchedulerException e) {
                                log.error("Deleting job=" + jobKey + " failed", e);
                            }
                        }
                    }
                }
                return scheduler;
            }
        };
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(quartzDataSource);
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties());

        if (triggerBeans.isPresent()) {
            final List<Trigger> triggers = triggerBeans.get();
            triggers.forEach(triggerBean -> log.info("Schedule trigger={}", triggerBean.getJobKey()));
            factory.setTriggers(triggers.toArray(new Trigger[triggers.size()]));
        }
        return factory;
    }

    @Bean
    public Properties quartzProperties() throws IOException {
        final PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource("/quartz.properties"));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    @Bean
    public JobDetailFactoryBean cameraMetadataUpdateJobDetail() {
        return createJobDetail(CameraMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean tmsStationMetadataUpdateJobDetail() {
        return createJobDetail(TmsStationMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean weatherStationMetadataUpdateJobDetail() {
        return createJobDetail(WeatherStationMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean cameraStationsStatusMetadataUpdateJobDetail() {
        return createJobDetail(CameraStationsStatusMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean tmsStationsStatusMetadataUpdateJobDetail() {
        return createJobDetail(TmsStationsStatusMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean tmsStationSensorConstantsMetadataUpdateJobDetail() {
        return createJobDetail(TmsStationSensorConstantsMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean weatherStationsStatusMetadataUpdateJobDetail() {
        return createJobDetail(WeatherStationsStatusMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean locationMetadataUpdateJobDetail() { return createJobDetail(LocationMetadataUpdateJob.class); }

    @Bean
    public JobDetailFactoryBean forecastSectionCoordinatesMetadataUpdateJobDetail() {
        return createJobDetail(ForecastSectionCoordinatesMetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean forecastSectionWeatherUpdateJobDetail() {
        return createJobDetail(ForecastSectionWeatherUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean datex2TrafficAlertMessageUpdateJobDetail() {
        return createJobDetail(Datex2TrafficAlertMessageUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean datex2RoadworksMessageUpdateJobDetail() {
        return createJobDetail(Datex2RoadworksMessageUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean datex2WeightRestrictionsMessageUpdateJobDetail() {
        return createJobDetail(Datex2WeightRestrictionsMessageUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean forecastSectionV2MetadataUpdateJobDetail() {
        return createJobDetail(ForecastSectionV2MetadataUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean forecastSectionV2DataUpdateJobDetail() {
        return createJobDetail(ForecastSectionV2DataUpdateJob.class);
    }

    @Bean
    public FactoryBean<? extends Trigger> cameraMetadataUpdateJobTrigger(final JobDetail cameraMetadataUpdateJobDetail) {
        return createTrigger(cameraMetadataUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> tmsStationMetadataUpdateJobTrigger(final JobDetail tmsStationMetadataUpdateJobDetail) {
        return createTrigger(tmsStationMetadataUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> tmsStationSensorConstantsUpdateJobTrigger(final JobDetail tmsStationSensorConstantsMetadataUpdateJobDetail) {
        return createTrigger(tmsStationSensorConstantsMetadataUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> weatherStationMetadataUpdateJobTrigger(final JobDetail weatherStationMetadataUpdateJobDetail) {
        return createTrigger(weatherStationMetadataUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> cameraStationsStatusMetadataUpdateJobTrigger(final JobDetail cameraStationsStatusMetadataUpdateJobDetail) {
        return createTrigger(cameraStationsStatusMetadataUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> tmsStationsStatusMetadataUpdateJobTrigger(final JobDetail tmsStationsStatusMetadataUpdateJobDetail) {
        return createTrigger(tmsStationsStatusMetadataUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> weatherStationsStatusMetadataUpdateJobTrigger(final JobDetail weatherStationsStatusMetadataUpdateJobDetail) {
        return createTrigger(weatherStationsStatusMetadataUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> locationsMetadataUpdateJobTrigger(final JobDetail locationMetadataUpdateJobDetail) {
        return createTrigger(locationMetadataUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> forecastSectionCoordinatesMetadataUpdateJobTrigger(final JobDetail forecastSectionCoordinatesMetadataUpdateJobDetail) {
        return createTrigger(forecastSectionCoordinatesMetadataUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> forecastSectionWeatherUpdateJobTrigger(final JobDetail forecastSectionWeatherUpdateJobDetail) {
        return createTrigger(forecastSectionWeatherUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> datex2TrafficAlertMessageUpdateJobTrigger(final JobDetail datex2TrafficAlertMessageUpdateJobDetail) {
        return createTrigger(datex2TrafficAlertMessageUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> datex2RoadworksMessageUpdateJobTrigger(final JobDetail datex2RoadworksMessageUpdateJobDetail) {
        return createTrigger(datex2RoadworksMessageUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> datex2WeightRestrictionsMessageUpdateJobTrigger(final JobDetail datex2WeightRestrictionsMessageUpdateJobDetail) {
        return createTrigger(datex2WeightRestrictionsMessageUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> forecastSectionV2MetadataUpdateJobTrigger(final JobDetail forecastSectionV2MetadataUpdateJobDetail) {
        return createTrigger(forecastSectionV2MetadataUpdateJobDetail);
    }

    @Bean
    public FactoryBean<? extends Trigger> forecastSectionV2DataUpdateJobTrigger(final JobDetail forecastSectionV2DataUpdateJobDetail) {
        return createTrigger(forecastSectionV2DataUpdateJobDetail);
    }

    private static JobDetailFactoryBean createJobDetail(final Class jobClass) {
        final JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        // job has to be durable to be stored in DB:
        factoryBean.setDurability(true);
        // In case of job executing during the time of a hard shutdown, it will be re-executed when the scheduler is started again
        factoryBean.setRequestsRecovery(true);
        return factoryBean;
    }

    private FactoryBean<? extends Trigger> createTrigger(final JobDetail jobDetail) {

        final String jobScheduleProperty = String.format(JOB_SCHEDLULE_STRING_FORMAT, jobDetail.getJobClass().getSimpleName());
        log.info("jobScheduleProperty={}", jobScheduleProperty);
        final String jobScheduleExpression = environment.getProperty(jobScheduleProperty);
        if (jobScheduleExpression == null) {
            log.warn("Not creating trigger for job {} as jobScheduleProperty={} is missing", jobDetail.getJobClass().getSimpleName(), jobScheduleProperty);
            return null;
        }
        try {
            // Try first to create interval trigger and fallback to cron
            long intervalMs = Long.parseLong(jobScheduleExpression);
            return  createRepeatingTrigger(jobDetail, intervalMs);
        } catch (NumberFormatException nfe) { // cron expression
            return createCronTrigger(jobDetail, jobScheduleExpression);
        }
    }

    /**
     * @param jobDetail
     * @param repeatIntervalMs how often is job repeated in ms. If time <= 0 it's triggered only once.
     * @return
     */
    private static SimpleTriggerFactoryBean createRepeatingTrigger(final JobDetail jobDetail, final long repeatIntervalMs) {

        final String jobName = getJobName(jobDetail);

        final SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setRepeatInterval(repeatIntervalMs);
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        // In case of misfire: The first misfired execution is run immediately, remaining are discarded.
        // Next execution happens after desired interval. Effectively the first execution time is moved to current time.
        factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT);

        log.info("Created SimpleTrigger for jobName={} with repeatIntervalMs={}", jobName, repeatIntervalMs);
        return factoryBean;
    }

    /**
     * @param jobDetail
     * @param cronExpression Cron expression for trigger schedule.
     * @return
     */
    private static CronTriggerFactoryBean createCronTrigger(final JobDetail jobDetail, final String cronExpression) {

        final String jobName = getJobName(jobDetail);

        final CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        factoryBean.setCronExpression(cronExpression);
        factoryBean.setTimeZone(TimeZone.getTimeZone("UTC"));
        factoryBean.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW);
        log.info("Created CronTrigger for jobName={} with cron expression={}", jobName, cronExpression);
        return factoryBean;
    }

    private static String getJobName(JobDetail jobDetail) {
        return jobDetail.getJobClass().getSimpleName();
    }

}
