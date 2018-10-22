package fi.livi.digitraffic.tie.data.model.maintenance.converter;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.GenericConversionService;

public abstract class AutoRegisteredConverter<S, T> implements Converter<S, T> {

    @Autowired
    @Qualifier("mvcConversionService")
    private GenericConversionService conversionService;

    @SuppressWarnings("unused")
    @PostConstruct
    private void register() {
        conversionService.addConverter(this);
    }

    public <TT> TT convert(Object source, Class<TT> targetType) {
        return convert(source, targetType);
    }
}