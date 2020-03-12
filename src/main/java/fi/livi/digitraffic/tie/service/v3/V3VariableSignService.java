package fi.livi.digitraffic.tie.service.v3;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.dao.v3.V3CodeDescriptionRepository;
import fi.livi.digitraffic.tie.model.v3.V3CodeDescription;

@Service
public class V3VariableSignService {
    private final V3CodeDescriptionRepository v3CodeDescriptionRepository;

    public V3VariableSignService(final V3CodeDescriptionRepository v3CodeDescriptionRepository) {
        this.v3CodeDescriptionRepository = v3CodeDescriptionRepository;
    }

    @Transactional(readOnly = true)
    public List<V3CodeDescription> listVariableSignTypes() {
        return v3CodeDescriptionRepository.listAllVariableSignTypes();
    }
}
