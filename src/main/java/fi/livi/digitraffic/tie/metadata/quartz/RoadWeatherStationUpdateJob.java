package fi.livi.digitraffic.tie.metadata.quartz;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import fi.livi.digitraffic.tie.metadata.service.roadweather.RoadWeatherStationUpdater;

@DisallowConcurrentExecution
public class RoadWeatherStationUpdateJob implements Job {

    private static final Logger log = Logger.getLogger(RoadWeatherStationUpdateJob.class);

    @Autowired
    public RoadWeatherStationUpdater roadWeatherStationUpdater;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        log.info("Quartz RoadWeatherStationUpdateJob start");
        roadWeatherStationUpdater.updateWeatherStations();
        roadWeatherStationUpdater.updateRoadWeatherSensors();
    }
}
