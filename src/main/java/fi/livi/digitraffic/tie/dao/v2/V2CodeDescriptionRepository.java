package fi.livi.digitraffic.tie.dao.v2;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import fi.livi.digitraffic.tie.dao.SqlRepository;
import fi.livi.digitraffic.tie.dto.v1.CodeDescription;

@Repository
public interface V2CodeDescriptionRepository extends SqlRepository {
    @Query(value =
        "select code, description_fi description " +
        "from code_description where domain = 'VARIABLE_SIGN'",
        nativeQuery = true)
    List<CodeDescription> listAllVariableSignTypes();
}
