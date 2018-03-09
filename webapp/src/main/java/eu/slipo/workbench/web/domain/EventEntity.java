package eu.slipo.workbench.web.domain;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import eu.slipo.workbench.web.model.EnumEventLevel;
import eu.slipo.workbench.web.model.EventRecord;

@Entity(name = "Event")
@Table(schema = "public", name = "log4j_message")
public class EventEntity {

    @Id
    @Column(name = "id", updatable = false)
    @SequenceGenerator(sequenceName = "log4j_message_id_seq", name = "log4j_message_id_seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(generator = "log4j_message_id_seq", strategy = GenerationType.SEQUENCE)
    long id = -1L;

    @NotNull
    @Column(name = "`application`", nullable = false)
    String application;

    @Column(name = "generated")
    ZonedDateTime generated;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "`level`")
    EnumEventLevel level;

    @Column(name = "`message`")
    String message;

    @Column(name = "`throwable`")
    String throwable;

    @Column(name = "`logger`")
    String logger;

    @Column(name = "`client_address`")
    String clientAddress;

    @Column(name = "`username`")
    String userName;

    public EventEntity() {
    }

    public long getId() {
        return id;
    }

    public String getApplication() {
        return application;
    }

    public ZonedDateTime getGenerated() {
        return generated;
    }

    public EnumEventLevel getLevel() {
        return level;
    }

    public String getMessage() {
        return message;
    }

    public String getThrowable() {
        return throwable;
    }

    public String getLogger() {
        return logger;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public String getUserName() {
        return userName;
    }

    public EventRecord toEventRecord() {
        EventRecord record = new EventRecord();

        record.setClientAddress(this.clientAddress);
        record.setCreatedOn(generated);
        record.setLevel(level);
        record.setMessage(message);
        record.setModule(application);
        record.setUserName(userName);

        return record;
    }

}
