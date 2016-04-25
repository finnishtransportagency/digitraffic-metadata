package fi.livi.digitraffic.tie.metadata.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fi.livi.digitraffic.tie.metadata.dao.StaticDataStatusDAO;

@Service
public class StaticDataStatusServiceImpl implements StaticDataStatusService {

    private final StaticDataStatusDAO staticDataStatusDAO;



    @Autowired
    public StaticDataStatusServiceImpl(final StaticDataStatusDAO staticDataStatusDAO) {
        this.staticDataStatusDAO = staticDataStatusDAO;
    }

    @Transactional
    @Override
    public void updateStaticDataStatus(final StaticStatusType type, final boolean updateStaticDataStatus) {
        staticDataStatusDAO.updateStaticDataStatus(type, updateStaticDataStatus);
    }
}
