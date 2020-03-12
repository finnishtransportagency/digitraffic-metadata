package fi.livi.digitraffic.tie.service.v1.lotju;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

public abstract class LotjuServiceEndpointMock {

    public static final long RANDOM_PORT = RandomUtils.nextLong(6000,7000);

    private static final Logger log = LoggerFactory.getLogger(LotjuServiceEndpointMock.class);
    private final String metadataServerAddress;
    private final Class<?> metatiedotClass;
    private final QName serviceName;
    private final Jaxb2Marshaller jaxb2Marshaller;
    protected final String resourcePath;

    private boolean stateAfterChange = false;

    protected final ResourceLoader resourceLoader;
    private boolean inited;

    public LotjuServiceEndpointMock(final ResourceLoader resourceLoader, final String metadataServerAddress, final Class<?> metatiedotClass,
                                    final QName serviceName, final Jaxb2Marshaller jaxb2Marshaller, String resourcePath) {
        log.info("RANDOM_PORT={} metadataServerAddress={}", RANDOM_PORT, metadataServerAddress);
        this.resourceLoader = resourceLoader;
        this.metadataServerAddress = metadataServerAddress;
        this.metatiedotClass = metatiedotClass;
        this.serviceName = serviceName;
        this.jaxb2Marshaller = jaxb2Marshaller;
        this.resourcePath = resourcePath;
    }

    /**
     * Initiaize data and call initService-method here.
     * Call this before tests.
     */
    public abstract void initStateAndService();

    /**
     * Must be called before api-operations
     */
    protected void initService() {
        log.info("Init LotjuServiceEndpointMock with address " + metadataServerAddress + " and serviceClass " + metatiedotClass);
        final JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
        svrFactory.setServiceClass(metatiedotClass);
        svrFactory.setAddress(metadataServerAddress);
        svrFactory.setServiceBean(this);
        svrFactory.setServiceName(serviceName);
        svrFactory.create();
        inited = true;
    }

    public boolean isInited() {
        return inited;
    }

    public boolean isStateAfterChange() {
        return stateAfterChange;
    }

    public void setStateAfterChange(final boolean stateChanged) {
        this.stateAfterChange = stateChanged;
    }

    protected <TYPE> TYPE readLotjuSoapResponse(Class<TYPE> returnType) {
        return readLotjuSoapResponse(returnType, null);
    }

    protected <TYPE> TYPE readLotjuSoapResponse(Class<TYPE> returnType, final Long lotjuId) {
        final String filePath = resolveFilePath(returnType.getSimpleName(), lotjuId);
        if (filePath == null) {
            return null;
        }
        return (TYPE)readLotjuMetadataXml(filePath, getObjectFactoryClass());

    }

    protected abstract Class<?> getObjectFactoryClass();

    private String resolveFilePath(final String file, final Long lotjuId) {
        String filePath = getFilePath(file, lotjuId, isStateAfterChange());
        // Check if changed file exists and return initial if not
        if ( isStateAfterChange() && !resourceLoader.getResource("classpath:" + filePath).exists() ) {
            filePath = getFilePath(file, lotjuId, false);
        }
        if ( resourceLoader.getResource("classpath:" + filePath).exists() ) {
            return filePath;
        }
        return null;
    }

    private String getFilePath(final String file, final Long lotjuId, boolean changed) {
        return String.format(resourcePath + "%s%s%s.xml",
            file,
            lotjuId != null ?  lotjuId : "",
            changed ? "Changed" : "");
    }


    /**
     * Read given lotju xml and returns response value
     * @param filePath
     * @param objectFactoryClass
     * @return response value Object returned from JAXBElement<?>.getValue()
     */
    protected Object readLotjuMetadataXml(final String filePath, final Class<?> objectFactoryClass) {
        log.info("Read Lotju SOAP response: {}", filePath);
        try {
            final Resource resource = resourceLoader.getResource("classpath:" + filePath);
            final String content = FileUtils.readFileToString(resource.getFile(), UTF_8);

            final JAXBElement<?> response =
                    (JAXBElement<?>) jaxb2Marshaller.unmarshal(new StringSource(content));
            return response.getValue();
        } catch (final IOException e) {
            throw new LotjuTestException(e);
        } catch (final XmlMappingException e) {
            throw new LotjuTestException(e);
        }
    }

    private class LotjuTestException extends RuntimeException {
        public LotjuTestException(Exception e) {
            super(e);
        }
    }
}
