package fi.livi.digitraffic.tie.model.v2.maintenance;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import fi.livi.digitraffic.tie.helper.ToStringHelper;

@Entity
@Immutable
@Table(name = "MAINTENANCE_TASK")
public class MaintenanceTask {

    @Id
    @Column
    private Long id;

    @Column
    private String fi;

    @Column
    private String sv;

    @Column
    private String en;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="OPERATION_ID", referencedColumnName = "ID", nullable = false, insertable = false, updatable = false)
    private MaintenanceTaskOperation operation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="CATEGORY_ID", referencedColumnName = "ID", nullable = false, insertable = false, updatable = false)
    private MaintenanceTaskCategory category;

    public MaintenanceTask() {
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

    public MaintenanceTaskOperation getOperation() {
        return operation;
    }

    public MaintenanceTaskCategory getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return ToStringHelper.toStringFull(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final MaintenanceTask that = (MaintenanceTask) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
