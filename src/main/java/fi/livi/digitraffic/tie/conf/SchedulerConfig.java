package fi.livi.digitraffic.tie.conf;

import fi.livi.digitraffic.tie.metadata.quartz.*;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Configuration
@ConditionalOnProperty(name = "quartz.enabled")
public class SchedulerConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);

    @Bean
    public JobFactory jobFactory(final ApplicationContext applicationContext)
    {
        final AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(final DataSource dataSource,
                                                     final JobFactory jobFactory,
                                                     Optional<List<Trigger>> triggerBeans) throws IOException {

        final SchedulerFactoryBean factory = new SchedulerFactoryBean();
        // this allows to update triggers in DB when updating settings in config file:
        factory.setOverwriteExistingJobs(true);
        factory.setDataSource(dataSource);
        factory.setJobFactory(jobFactory);

        factory.setQuartzProperties(quartzProperties());

        if (triggerBeans.isPresent()) {
            for (Trigger triggerBean : triggerBeans.get()) {
                log.info("Schedule trigger " + triggerBean.getJobKey());
            }
            factory.setTriggers(triggerBeans.get().toArray(new Trigger[0]));
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
    public JobDetailFactoryBean cameraUpdateJobDetail() {
        return createJobDetail(CameraUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean tmsStationUpdateJobDetail() {
        return createJobDetail(TmsStationUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean weatherStationUpdateJobDetail() {
        return createJobDetail(WeatherStationUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean forecastSectionCoordinatesUpdateJobDetail() {
        return createJobDetail(ForecastSectionCoordinatesUpdateJob.class);
    }

    @Bean
    public JobDetailFactoryBean forecastSectionWeatherUpdateJobDetail() {
        return createJobDetail(ForecastSectionWeatherUpdateJob.class);
    }

    @Bean(name = "cameraUpdateJobTrigger")
    public SimpleTriggerFactoryBean cameraUpdateJobTrigger(@Qualifier("cameraUpdateJobDetail") final JobDetail jobDetail,
                                                           @Value("${cameraStationUpdateJob.frequency}") final long frequency) {
        return createTrigger(jobDetail, frequency);
    }

    @Bean(name = "tmsStationUpdateJobTrigger")
    public SimpleTriggerFactoryBean tmsStationUpdateJobTrigger(@Qualifier("tmsStationUpdateJobDetail") final JobDetail jobDetail,
                                                               @Value("${tmsStationUpdateJob.frequency}") final long frequency) {
        return createTrigger(jobDetail, frequency);
    }

    @Bean(name = "weatherStationUpdateJobTrigger")
    public SimpleTriggerFactoryBean weatherStationUpdateJobTrigger(@Qualifier("weatherStationUpdateJobDetail") final JobDetail jobDetail,
                                                                   @Value("${weatherStationUpdateJob.frequency}") final long frequency) {
        return createTrigger(jobDetail, frequency);
    }

    @Bean(name = "forecastSectionCoordinatesUpdateJobTrigger")
    public SimpleTriggerFactoryBean forecastSectionCoordinatesUpdateJobTrigger(@Qualifier("forecastSectionCoordinatesUpdateJobDetail") final JobDetail jobDetail,
                                                                               @Value("${forecastSectionCoordinatesUpdateJob.frequency}") final long frequency) {
        return createTrigger(jobDetail, frequency);
    }

    @Bean(name = "forecastSectionWeatherUpdateJobTrigger")
    public SimpleTriggerFactoryBean forecastSectionWeatherUpdateJobTrigger(@Qualifier("forecastSectionWeatherUpdateJobDetail") final JobDetail jobDetail,
                                                                           @Value("${forecastSectionWeatherUpdateJob.frequency}") final long frequency) {
        return createTrigger(jobDetail, frequency);
    }


    private static JobDetailFactoryBean createJobDetail(final Class jobClass) {
        final JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(jobClass);
        // job has to be durable to be stored in DB:
        factoryBean.setDurability(true);
        return factoryBean;
    }

    private static SimpleTriggerFactoryBean createTrigger(final JobDetail jobDetail, final long pollFrequencyMs) {
        final SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(jobDetail);
        // Delay first execution 5 seconds
        factoryBean.setStartDelay(5000L);
        factoryBean.setRepeatInterval(pollFrequencyMs);
        factoryBean.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        // In case of misfire: The first misfired execution is run immediately, remaining are discarded.
        // Next execution happens after desired interval. Effectively the first execution time is moved to current time.
        factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT);
        return factoryBean;
    }
}
