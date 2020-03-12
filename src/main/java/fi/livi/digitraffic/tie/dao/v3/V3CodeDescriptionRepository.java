package fi.livi.digitraffic.tie.dao.v3;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dao.SqlRepository;
import fi.livi.digitraffic.tie.model.v3.V3CodeDescription;

@Repository
public interface V3CodeDescriptionRepository extends SqlRepository {
    @Query(value =
        "select code, description_fi descriptionFi, description_en descriptionEn " +
        "from code_description where domain = 'VARIABLE_SIGN'",
        nativeQuery = true)
    List<V3CodeDescription> listAllVariableSignTypes();
}
