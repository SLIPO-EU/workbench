package eu.slipo.workbench.rpc.domain;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.batch.core.JobParameter.ParameterType;

/**
 * Describe a job parameter and provide defaults.
 */
@Entity(name = "JobParameter")
@Table(name = "job_parameter", schema = "rpc")
public class JobParameterEntity
{
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE, generator = "job_parameter_id_seq")
    @SequenceGenerator(
        sequenceName = "rpc.job_parameter_id_seq", name = "job_parameter_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Integer id;
    
    @Column(name = "job_name", nullable = false)
    @NotEmpty
    @NaturalId
    private String jobName;
    
    @Column(name = "`name`", nullable = false)
    @NotEmpty
    @NaturalId
    private String name;
    
    @Column(name = "required", nullable = false)
    private boolean required = false;
    
    @Column(name = "identifying", nullable = false)
    private boolean identifying = true;

    @Column(name = "`type`")
    @Enumerated(EnumType.STRING)
    private ParameterType type;
    
    @Column(name = "default_value")
    private String defaultValue;
    
    @Column(name = "default_expression")
    private String defaultExpression;

    private static final ZoneId DEFAULT_ZONE = ZoneId.of("Z");
    
    public JobParameterEntity() {}
    
    public JobParameterEntity(String jobName, String name)
    {
        this.jobName = jobName;
        this.name = name;
    }
    
    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getJobName()
    {
        return jobName;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ParameterType getType()
    {
        return type == null? ParameterType.STRING : type;
    }

    public void setType(ParameterType type)
    {
        this.type = type;
    }

    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public boolean isIdentifying()
    {
        return identifying;
    }

    public void setIdentifying(boolean identifying)
    {
        this.identifying = identifying;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String value)
    {
        this.defaultValue = value;
        this.type = ParameterType.STRING;
    }
    
    public void setDefaultValue(Long value)
    {
        this.defaultValue = value.toString();
        this.type = ParameterType.LONG;
    }
    
    public void setDefaultValue(Double value)
    {
        this.defaultValue = value.toString();
        this.type = ParameterType.DOUBLE;
    }
    
    public void setDefaultValue(Date value)
    {
        setDefaultValue(ZonedDateTime.ofInstant(value.toInstant(), DEFAULT_ZONE));
    }
    
    public void setDefaultValue(ZonedDateTime value)
    {
        this.defaultValue = DateTimeFormatter.ISO_DATE_TIME.format(value);
        this.type = ParameterType.DATE;
    }

    public String getDefaultExpression()
    {
        return defaultExpression;
    }

    public void setDefaultExpression(String defaultExpression)
    {
        this.defaultExpression = defaultExpression;
    }
    
    @AssertTrue(message = "Cannot specify both defaultValue and defaultExpression")
    public boolean checkDefaultExpression()
    {
        return defaultExpression == null || defaultValue == null;
    }
}