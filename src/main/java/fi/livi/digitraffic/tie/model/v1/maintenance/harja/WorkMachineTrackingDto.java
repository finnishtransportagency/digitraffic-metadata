package fi.livi.digitraffic.tie.model.v1.maintenance.harja;

import java.time.ZonedDateTime;

import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.TypeDef;

import fi.livi.digitraffic.tie.conf.postgres.WorkMachineTrackingRecordUserType;
import fi.livi.digitraffic.tie.metadata.geojson.Geometry;

@TypeDef(name = "WorkMachineTrackingRecordUserType", typeClass = WorkMachineTrackingRecordUserType.class)
@Table(name = "WORK_MACHINE_TRACKING")
@Immutable
public interface WorkMachineTrackingDto {

    Long getId();

    WorkMachineTrackingRecord getRecord();

    Geometry.Type getType();

    ZonedDateTime getCreated();

    ZonedDateTime getHandled();
}
