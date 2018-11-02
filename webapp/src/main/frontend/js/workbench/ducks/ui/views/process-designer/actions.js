/*
 * Action types
 */
import * as Types from './types';

/*
 * Action creators
 */

export const reset = function () {
  return {
    type: Types.RESET,
  };
};

export const addStep = function (group, step, appConfiguration) {
  return {
    type: Types.ADD_STEP,
    group,
    step,
    appConfiguration,
  };
};

export const cloneStep = function (step) {
  return {
    type: Types.CLONE_STEP,
    step,
  };
};

export const moveStep = function (dragOrder, hoverOrder) {
  return {
    type: Types.MOVE_STEP,
    dragOrder,
    hoverOrder,
  };
};

export const moveStepInput = function (step, dragOrder, hoverOrder) {
  return {
    type: Types.MOVE_STEP_INPUT,
    step,
    dragOrder,
    hoverOrder,
  };
};

export const removeStep = function (step) {
  return {
    type: Types.REMOVE_STEP,
    step,
  };
};

export const resetActive = function () {
  return {
    type: Types.RESET_SELECTION,
  };
};

export const setActiveStep = function (step) {
  return {
    type: Types.SELECT_STEP,
    step,
  };
};

export const setStepProperty = function (key, property, value) {
  return {
    type: Types.SET_STEP_PROPERTY,
    key,
    property,
    value,
  };
};

export const configureStepBegin = function (step, configuration) {
  return {
    type: Types.CONFIGURE_STEP_BEGIN,
    step,
    configuration,
  };
};

export const configureStepValidate = function (step, errors) {
  return {
    type: Types.CONFIGURE_STEP_VALIDATE,
    step,
    errors,
  };
};

export const configureStepUpdate = function (step, configuration) {
  return {
    type: Types.CONFIGURE_STEP_UPDATE,
    step,
    configuration,
  };
};

export const configureStepEnd = function (step, configuration, errors) {
  return {
    type: Types.CONFIGURE_STEP_END,
    step,
    configuration,
    errors,
  };
};

export const addStepInput = function (step, resource, partKey) {
  return {
    type: Types.ADD_STEP_INPUT,
    step,
    resource,
    partKey,
  };
};

export const removeStepInput = function (step, resource) {
  return {
    type: Types.REMOVE_STEP_INPUT,
    step,
    resource,
  };
};

export const setActiveStepInput = function (step, resource) {
  return {
    type: Types.SELECT_STEP_INPUT,
    step,
    resource,
  };
};

export const addStepDataSource = function (step, dataSource) {
  return {
    type: Types.ADD_STEP_DATA_SOURCE,
    step,
    dataSource,
  };
};

export const removeStepDataSource = function (step, dataSource) {
  return {
    type: Types.REMOVE_STEP_DATA_SOURCE,
    step,
    dataSource,
  };
};

export const setActiveStepDataSource = function (step, dataSource) {
  return {
    type: Types.SELECT_STEP_DATA_SOURCE,
    step,
    dataSource,
  };
};

export const configureStepDataSourceBegin = function (step, dataSource, configuration) {
  return {
    type: Types.CONFIGURE_DATA_SOURCE_BEGIN,
    step,
    dataSource,
    configuration,
  };
};

export const configureStepDataSourceValidate = function (step, dataSource, errors) {
  return {
    type: Types.CONFIGURE_STEP_VALIDATE,
    step,
    dataSource,
    errors,
  };
};

export const configureStepDataSourceUpdate = function (step, dataSource, configuration) {
  return {
    type: Types.CONFIGURE_STEP_UPDATE,
    step,
    dataSource,
    configuration,
  };
};

export const configureStepDataSourceEnd = function (step, dataSource, configuration, errors) {
  return {
    type: Types.CONFIGURE_DATA_SOURCE_END,
    step,
    dataSource,
    configuration,
    errors,
  };
};

export const setConfiguration = function (step, configuration, errors) {
  return {
    type: Types.SET_STEP_CONFIGURATION,
    step,
    configuration,
    errors,
  };
};

export const addResourceToBag = function (resource) {
  return {
    type: Types.ADD_RESOURCE_TO_BAG,
    resource,
  };
};

export const removeResourceFromBag = function (resource) {
  return {
    type: Types.REMOVE_RESOURCE_FROM_BAG,
    resource,
  };
};

export const filterResource = function (filter) {
  return {
    type: Types.SET_RESOURCE_FILTER,
    filter,
  };
};

export const setActiveResource = function (resource) {
  return {
    type: Types.SELECT_RESOURCE,
    resource,
  };
};

export const setActiveProcess = function (process) {
  return {
    type: Types.SELECT_PROCESS,
    process,
  };
};

export const processValidate = function (errors) {
  return {
    type: Types.PROCESS_VALIDATE,
    errors,
  };
};

export const processUpdate = function (properties) {
  return {
    type: Types.PROCESS_UPDATE,
    properties,
  };
};

export const undo = function () {
  return {
    type: Types.UNDO,
  };
};

export const redo = function () {
  return {
    type: Types.REDO,
  };
};

export const showStepExecutionDetails = function () {
  return {
    type: Types.SHOW_STEP_EXECUTION,
  };
};

export const hideStepExecutionDetails = function () {
  return {
    type: Types.HIDE_STEP_EXECUTION,
  };
};

export const selectFile = (id) => ({
  type: Types.SET_SELECTED_FILE,
  id,
});

export const resetSelectedFile = () => ({
  type: Types.RESET_SELECTED_FILE,
});

export const resetSelectedKpi = () => ({
  type: Types.RESET_SELECTED_KPI,
});

export const selectOutputPart = (step, resource, partKey) => ({
  type: Types.SET_STEP_INPUT_OUTPUT_PART,
  step,
  resource,
  partKey,
});
