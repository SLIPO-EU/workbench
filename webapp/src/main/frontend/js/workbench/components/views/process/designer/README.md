# Workflow Designer objects

This document describes the structure of the simple JavaScript objects stored
in the application state and exchanged in action creators. 

All enumeration values are declared in file [constants.js](../../../../model/process-designer/constants.js)

## Step

A `Step` represents a single step in a workflow. Steps are stored in property `steps` 
of the state.

| Property            | Type      | Description |
| ------------------- | --------- | ----------- |
| key                 | Number    | Step unique key |
| group               | Number    | Step group key |
| type                | String    | A value of type `EnumToolboxItem`. It is always set to `EnumToolboxItem.Operation` |
| tool                | String    | A value of type `EnumTool` |
| operation           | String    | A value of type `EnumOperation` |
| order               | Number    | The order of the step in the designer |
| name                | String    | Step name|
| iconClass           | String    | The icon class for the specific `tool` |
| resources           | Number[]  | Array of step input resource keys. Resources reference objects in state `resources` array |
| dataSources         | Object[]  | Array of data source objects. Current implementation supports only a single data source. The array may contain zero or one elements  |
| configuration       | Object    | Configuration object that is resolved based on the `tool` value |
| errors              | Object    | Configuration errors |
| outputKey           | Number    | Key of the output resource if the step returns a result or `undefined` |
| outputFormat        | String    | A value of type `EnumFormat` |


## Data Sources

Data sources are assigned to TripleGeo steps. The schema of a data source object
depends on the `source` property value.

| Property            | Type      | Description |
| ------------------- | --------- | ----------- |
| key                 | Number    | Data source unique key for the specific step. Current implemenation supports only a single data source. The key value is always `0` |
| type                | String    | A value of type `EnumToolboxItem`. It is always set to `EnumToolboxItem.DataSource` |
| source              | String    | A value of type `EnumDataSource` |
| iconClass           | String    | The icon class for the specific `source` |
| name                | String    | Data source name|
| configuration       | Object    | Configuration object that is resolved based on the `source` value |
| errors              | Object    | Configuration errors |

## EnumDataSource.HARVESTER

Additional properties for harvester data sources

| Property            | Type      | Description |
| ------------------- | --------- | ----------- |
| harvester           | String    | A value of type `EnumHarvester` when `source` is equal to `EnumDataSource.HARVESTER` or `undefined` |

## Resources

A resource represents any dataset that can be used as an input to a step. Currently 
two resource types are supported, namely, `CATALOG` and `OUTPUT`. The former refers
to a resource registered in the catalog while the latter represents the output of 
a process step execution. Resources are stored in property `resources` of the state.
Steps reference resources with their unique keys.

| Property            | Type      | Description |
| ------------------- | --------- | ----------- |
| key                 | Number    | Resource unique key |
| inputType           | String    | A value of type `EnumInputType` |
| resourceType        | String    | A value of type `EnumResourceType` |
| name                | String    | The resource name |
| iconClass           | String    | The icon class for the specific resource `resourceType` |

Depending on the value of the `inputType` property, a resource may have one or
more additional properties

# EnumInputType.CATALOG

| Property            | Type      | Description |
| ------------------- | --------- | ----------- |
| id                  | Number    | Catalog resource unique id |
| version             | Number    | Resource version number |
| description         | String    | The resource description |

# EnumInputType.OUTPUT

| Property            | Type      | Description |
| ------------------- | --------- | ----------- |
| tool                | String    | A value of type `EnumTool` |
| stepKey             | Number    | Parent step unique key|

# Drag Sources

Items that can be dragged are represented as drag sources. A drag source
provides a set of properties to a drop target to handle the drop operation.

## Step Drag source


| Property            | Type      | Description |
| ------------------- | --------- | ----------- |
| key                 | Number    | Step unique key |
| order               | Number    | Step order |
| step                | Object    | `Step` being dragged |
