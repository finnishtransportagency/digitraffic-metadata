package fi.livi.digitraffic.tie.conf.jms;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import progress.message.jclient.ErrorCodes;


public class JMSErrorResolver {

    private static final Logger log = LoggerFactory.getLogger(JMSErrorResolver.class);

    private JMSErrorResolver() {}

    /**
     * Resolves JMS error code to human readable format
     * IE. code "-5" resolves to "ERR_CONNECTION_DROPPED"
     *
     * @param errCode error code value ie. "-5"
     * @return
     */
    public static String resolveErrorMessageByErrorCode(final String errCode) {
        Optional<Field> errorField = Arrays.stream(ErrorCodes.class.getDeclaredFields())
                .filter(field -> isFieldValueEqualWithErrorCode(field, errCode) )
                .findFirst();
        if (errorField.isPresent()) {
            return ErrorCodes.class.getSimpleName() + "." + errorField.get().getName();
        }
        return null;
    }

    private static boolean isFieldValueEqualWithErrorCode(final Field field, final String errCode) {
        try {
            if (Modifier.isStatic(field.getModifiers())) {
                Object value = FieldUtils.readDeclaredStaticField(ErrorCodes.class, field.getName());
                return value != null && StringUtils.equals(errCode, "" + value);
            }
        } catch (Exception e) {
            log.debug("Error while resolving field value", e);
            return false;
        }
        return false;
    }
}
