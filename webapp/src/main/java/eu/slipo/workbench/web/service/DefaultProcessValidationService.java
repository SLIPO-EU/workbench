package eu.slipo.workbench.web.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import eu.slipo.workbench.common.model.Error;
import eu.slipo.workbench.common.model.poi.EnumTool;
import eu.slipo.workbench.common.model.process.InvalidProcessDefinitionException;
import eu.slipo.workbench.common.model.process.ProcessDefinition;
import eu.slipo.workbench.common.model.process.ProcessErrorCode;
import eu.slipo.workbench.common.model.process.Step;
import eu.slipo.workbench.common.model.tool.ReverseTriplegeoConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoConfiguration;
import eu.slipo.workbench.common.model.tool.TriplegeoErrorCode;
import eu.slipo.workbench.common.repository.ProcessRepository;

@Service
public class DefaultProcessValidationService implements IProcessValidationService, InitializingBean {

    private List<String> validEpsgCodes = new ArrayList<String>();

    @Autowired
    private IAuthenticationFacade authenticationFacade;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ProcessRepository processRepository;

    private JdbcTemplate jdbcTemplate;

    private int currentUserId() {
        return authenticationFacade.getCurrentUserId();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void validate(Long id, ProcessDefinition definition, boolean isTemplate) throws InvalidProcessDefinitionException {

        List<Error> errors = new ArrayList<Error>();

        // Process name must be unique
        if ((id == null) && (processRepository.findOne(definition.name(), currentUserId()) != null)) {
            errors.add(new Error(ProcessErrorCode.NAME_DUPLICATE, "Workflow name already exists."));
        }

        // TripleGeo
        definition.steps().stream()
            .filter(s -> s.tool() == EnumTool.TRIPLEGEO)
            .forEach(s -> this.ValidateTripleGeo(s, errors));

        definition.steps().stream()
            .filter(s -> s.tool() == EnumTool.REVERSE_TRIPLEGEO)
            .forEach(s -> this.ValidateReverseTripleGeo(s, errors));

        if (!errors.isEmpty()) {
            throw new InvalidProcessDefinitionException(errors);
        }
    }

    private void ValidateTripleGeo(Step step, List<Error> errors) {
        TriplegeoConfiguration config = (TriplegeoConfiguration) step.configuration();

        // Validate CRS values
        if (!this.ValidateEpsgCode(config.getSourceCRS())) {
            errors.add(new Error(TriplegeoErrorCode.INVALID_CRS, String.format("CRS code %s is not valid", config.getSourceCRS())));
        }
        if (!this.ValidateEpsgCode(config.getTargetCRS())) {
            errors.add(new Error(TriplegeoErrorCode.INVALID_CRS, String.format("CRS code %s is not valid", config.getTargetCRS())));
        }
    }

    private void ValidateReverseTripleGeo(Step step, List<Error> errors) {
        ReverseTriplegeoConfiguration config = (ReverseTriplegeoConfiguration) step.configuration();

        // Validate CRS values
        if (!this.ValidateEpsgCode(config.getSourceCRS())) {
            errors.add(new Error(TriplegeoErrorCode.INVALID_CRS, String.format("CRS code %s is not valid", config.getSourceCRS())));
        }
        if (!this.ValidateEpsgCode(config.getTargetCRS())) {
            errors.add(new Error(TriplegeoErrorCode.INVALID_CRS, String.format("CRS code %s is not valid", config.getTargetCRS())));
        }
    }

    private boolean ValidateEpsgCode(String code) {
        // Allow null as the default value
        if (code == null) {
            return true;
        }
        // Check if the code has already been validated
        if (validEpsgCodes.contains(code)) {
            return true;
        }
        // Check code format
        String[] tokens = code.split(":");
        if (tokens.length != 2) {
            return false;
        }
        if (!tokens[0].equals("EPSG")) {
            return false;
        }
        try {
            Integer.parseInt(tokens[1]);
        } catch (Exception e) {
            return false;
        }
        // Fetch SRID value from PostGIS
        String query = "select srid from spatial_ref_sys where srid = ? limit 1";

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(query, new Object[] { Integer.parseInt(tokens[1]) });
        Integer epseCode = rows.stream().map(r -> (Integer) r.get("srid")).findFirst().orElse(null);
        if (epseCode == null) {
            return false;
        }

        validEpsgCodes.add(code);
        return true;
    }

    @Override
    public void validateProcess(ProcessDefinition definition) throws InvalidProcessDefinitionException {
        validate(null, definition, false);
    }

    @Override
    public void validateProcess(long id, ProcessDefinition definition) throws InvalidProcessDefinitionException {
        validate(id, definition, false);
    }

    @Override
    public void validateTemplate(ProcessDefinition definition) throws InvalidProcessDefinitionException {
        validate(null, definition, true);
    }

    @Override
    public void validateTemplate(long id, ProcessDefinition definition) throws InvalidProcessDefinitionException {
        validate(id, definition, true);
    }

}
