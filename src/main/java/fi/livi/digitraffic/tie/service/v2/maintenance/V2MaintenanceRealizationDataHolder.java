package fi.livi.digitraffic.tie.service.v2.maintenance;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Coordinate;

import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceRealizationData;
import fi.livi.digitraffic.tie.model.v2.maintenance.MaintenanceTask;

/**
 * Holds one realization (points has same tasks) data before saving to db
 */
class V2MaintenanceRealizationDataHolder {

    private List<Coordinate> coordinates = new ArrayList<>();
    private Set<MaintenanceTask> tasks = new HashSet<>();
    private MaintenanceRealizationData realizationData;
    private String sendingSystem;
    private Integer messageId;
    private ZonedDateTime sendingTime;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    private int coordinateIndex = -1;

    public V2MaintenanceRealizationDataHolder(final MaintenanceRealizationData realizationData, final String sendingSystem, final Integer messageId, final ZonedDateTime sendingTime) {
        this.realizationData = realizationData;
        this.sendingSystem = sendingSystem;
        this.messageId = messageId;
        this.sendingTime = sendingTime;
    }

    public void resetCoordinatesAndTasks() {
        coordinates = new ArrayList<>();
        startTime = null;
        endTime = null;
        tasks = new HashSet<>();
    }

    public void addCoordinate(final Coordinate coordinate, final ZonedDateTime time, final List<MaintenanceTask> maintenanceTasks) {
        coordinates.add(coordinate);
        ++coordinateIndex;
        if (startTime == null) {
            startTime = time;
        }
        if (endTime == null || endTime.isBefore(time)) {
            endTime = time;
        }

        if (tasks.isEmpty()) {
            tasks.addAll(maintenanceTasks);
        } else if (!tasks.containsAll(maintenanceTasks) || !maintenanceTasks.containsAll(tasks)) {
            throw new IllegalArgumentException(String.format("Tasks can't change in one realization, new realization should be made. " +
                "MaintenanceRealizationData id: %d and coordinateIndex: %d", realizationData.getId(), coordinateIndex));
        }
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public Set<MaintenanceTask> getTasks() {
        return tasks;
    }

    public Set<Long> getTaskids() {
        return tasks.stream().map(MaintenanceTask::getId).collect(Collectors.toSet());
    }

    public boolean isValidLineString() {
        return coordinates.stream().distinct().count() > 1;
    }

    public MaintenanceRealizationData getRealizationData() {
        return realizationData;
    }

    public String getSendingSystem() {
        return sendingSystem;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public ZonedDateTime getSendingTime() {
        return sendingTime;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public int getCoordinateIndex() {
        return coordinateIndex;
    }

    public boolean containsCoordinateData() {
        return coordinates.size() > 0;
    }
}
