package fi.livi.digitraffic.tie.conf.postgres;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SerializationException;
import org.hibernate.usertype.UserType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

import fi.livi.digitraffic.tie.conf.SpringContext;
import fi.livi.digitraffic.tie.model.v1.maintenance.harja.WorkMachineTrackingRecord;

public class WorkMachineTrackingRecordUserType implements UserType {

    private static final Logger log = LoggerFactory.getLogger(WorkMachineTrackingRecordUserType.class);
    private ObjectWriter writer;
    private ObjectReader reader;

    @Autowired
    private ObjectMapper objectMapper;

    public WorkMachineTrackingRecordUserType() {
    }

    private void initObjectReaderAndWriter() {
        if (objectMapper == null) {
            final AutowireCapableBeanFactory beanFactory =
                SpringContext.getAppContext().getAutowireCapableBeanFactory();
            beanFactory.autowireBean(this);
            writer = objectMapper.writerFor(WorkMachineTrackingRecord.class);
            reader = objectMapper.readerFor(WorkMachineTrackingRecord.class);
        }
    }

    private ObjectReader getReader() {
        if (reader == null) {
            initObjectReaderAndWriter();
        }
        return reader;
    }

    private ObjectWriter getWriter() {
        if (writer == null) {
            initObjectReaderAndWriter();
        }
        return writer;
    }

    @Override
    public int[] sqlTypes() {
        return new int[]{ Types.JAVA_OBJECT};
    }

    @Override
    public Class<WorkMachineTrackingRecord> returnedClass() {
        return WorkMachineTrackingRecord.class;
    }

    @Override
    public boolean equals(Object x, Object y) throws HibernateException {
        return ObjectUtils.nullSafeEquals(x, y);
    }

    @Override
    public int hashCode(Object o) throws HibernateException {
        if (o == null) {
            return 0;
        }
        return o.hashCode();
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names,
                              final SharedSessionContractImplementor session, final Object owner)
        throws HibernateException, SQLException {

        final String cellContent = rs.getString(names[0]);
        if (cellContent == null) {
            return null;
        }
        try {
            return getReader().readValue(cellContent.getBytes("UTF-8"));
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to convert String to WorkMachineTrackingRecord: " + ex.getMessage(), ex);
        }
    }



    @Override
    public void nullSafeSet(final PreparedStatement ps, final Object value, final int idx,
                            final SharedSessionContractImplementor session)
        throws HibernateException, SQLException {

        if (value == null) {
            ps.setNull(idx, Types.OTHER);
            return;
        }
        try {
            final StringWriter w = new StringWriter();
            getWriter().writeValue(w, value);
            w.flush();
            ps.setObject(idx, w.toString(), Types.OTHER);
        } catch (final Exception ex) {
            throw new RuntimeException("Failed to convert machine tracking to String: " + ex.getMessage(), ex);
        }
    }

    /**
     * Return a deep copy of the persistent state, stopping at entities and at
     * collections. It is not necessary to copy immutable objects, or null
     * values, in which case it is safe to simply return the argument.
     *
     * @param value the object to be cloned, which may be null
     * @return Object a copy
     */
    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (!isMutable()) {
            return value;
        }

        try {
            // use serialization to create a deep copy
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
            oos.flush();
            oos.close();
            bos.close();

            ByteArrayInputStream bais = new ByteArrayInputStream(bos.toByteArray());
            return new ObjectInputStream(bais).readObject();
        } catch (ClassNotFoundException | IOException ex) {
            throw new HibernateException(ex);
        }
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        Object copy = deepCopy(value);

        if (copy instanceof Serializable) {
            return (Serializable) copy;
        }

        throw new SerializationException(String.format("Cannot serialize '%s', %s is not Serializable.", value, value.getClass()), null);
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return deepCopy(cached);
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return deepCopy(original);
    }
}
