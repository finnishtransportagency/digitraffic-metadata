package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@Immutable
@Table(name = "MAINTENANCE_TASK_OPERATION")
public class MaintenanceTaskOperation {

    @Id
    @Column()
    private Long id;

    @Column
    private String fi;

    @Column
    private String sv;

    @Column
    private String en;

    public MaintenanceTaskOperation() {
        // For Hibernate
    }

    public Long getId() {
        return id;
    }

    public String getFi() {
        return fi;
    }

    public String getSv() {
        return sv;
    }

    public String getEn() {
        return en;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }
}
