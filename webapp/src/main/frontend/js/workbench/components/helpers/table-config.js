import * as React from 'react';
import { Link } from 'react-router-dom';
import { FormattedTime } from 'react-intl';

import moment from 'moment';

import { DynamicRoutes, buildPath } from '../../model/routes';

import JobStatus from './job-status';

/**
 * Job grid sample data
 */
export const JobGridData = [{
  process: {
    id: 1,
    version: 2,
  },
  executionId: 1,
  name: 'Import POI from OSM',
  startedOn: moment().add(-2, 'days').toDate(),
  completedOn: moment().add(-1, 'hours').toDate(),
  status: 'Completed',
}, {
  process: {
    id: 2,
    version: 3,
  },
  executionId: 1,
  name: 'Register resource \'Restaurants\'',
  startedOn: moment().add(-21, 'days').add(-13, 'hours').toDate(),
  completedOn: moment().add(-21, 'days').add(1, 'hours').toDate(),
  status: 'Failed',
}];

/**
 * Job grid sample column configuration
 */
export const JobGridColumns = [{
  Header: 'Workflow Id',
  accessor: 'processId',
  show: false
}, {
  Header: 'Execution Id',
  accessor: 'executionId',
  show: false
}, {
  Header: 'Name',
  accessor: 'name',
  minWidth: 250,
  Cell: props => {
    return (
      <Link to={buildPath(DynamicRoutes.ProcessExecutionViewer, [props.original.process.id, props.original.process.version, props.original.executionId])}>{props.value}</Link>
    );
  }
}, {
  Header: 'Started On',
  accessor: 'startedOn',
  Cell: props => {
    return (
      <FormattedTime value={props.value} day='numeric' month='numeric' year='numeric' />
    );
  }
}, {
  id: 'completedOn',
  Header: 'Completed On',
  accessor: d => d.completedOn,
  Cell: props => {
    return (
      <FormattedTime value={props.value} day='numeric' month='numeric' year='numeric' />
    );
  }
}, {
  Header: props => <span>Status Code</span>,
  accessor: 'status',
  Cell: props => {
    return (
      <JobStatus status={props.value} />
    );
  }
}];

/**
 * Event grid data maper
 */
export const EventGridData = (events) => events.map(event => ({
  level: event.level,
  category: event.category,
  code: event.code,
  createdOn: moment(event.createdOn).toDate(),
  message: event.message,
  source: event.clientAddress,
  account: event.userName,
}));

/**
 * Event grid sample column configuration
 */
export const EventGridColumns = [{
  Header: 'Level',
  accessor: 'level',
  style: { 'textAlign': 'center' }
}, {
  Header: 'Category',
  accessor: 'category',
  style: { 'textAlign': 'center' }
}, {
  Header: 'Code',
  accessor: 'code',
  style: { 'textAlign': 'center' }
}, {
  Header: 'Created On',
  accessor: 'createdOn',
  Cell: props => {
    return (
      <FormattedTime value={props.value} day='numeric' month='numeric' year='numeric' />
    );
  },
  minWidth: 132
}, {
  id: 'message',
  Header: 'Message',
  accessor: d => d.message,
  minWidth: 400,
}, {
  Header: props => <span>Source</span>,
  accessor: 'source',
  style: { 'textAlign': 'center' }
}, {
  Header: props => <span>Account</span>,
  accessor: 'account',
  style: { 'textAlign': 'center' }
}];

/**
 * Resource grid data
 */
export const ResourceGridData = (resources) => resources.map(resource => ({
  id: resource.id,
  name: resource.metadata.name,
  version: resource.version,
  createdOn: moment(resource.createdOn).toDate(),
  count: resource.metadata.size,
  process: resource.jobExecutionId,
}));

/**
 * Resource grid sample column configuration
 */
export const ResourceGridColumns = [{
  Header: 'Id',
  accessor: 'id',
  show: false
}, {
  Header: 'Actions',
  accessor: 'process',
  Cell: props => {
    return (
      props.value ?
        <Link style={{ color: '#00bcf2' }} to={buildPath(DynamicRoutes.ProcessExecutionMapViewer, { id: props.value })}>
          <i className='fa fa-map'></i>
        </Link>
        :
        <span className='fa fa-chain-broken'> </span>
    );
  },
  style: { 'textAlign': 'center' },
}, {
  Header: 'Name',
  accessor: 'name',
  minWidth: 100,
  Cell: props => {
    return (
      <Link to={buildPath(DynamicRoutes.ResourceViewer, [props.row.id, props.row.version])}>{props.value}</Link>
    );
  },
}, {
  Header: 'Version',
  accessor: 'version',
  style: { 'textAlign': 'center' }
}, {
  Header: 'Created On',
  accessor: 'createdOn',
  Cell: props => {
    return (
      <FormattedTime value={props.value} day='numeric' month='numeric' year='numeric' />
    );
  },
  minWidth: 150,
  style: { 'textAlign': 'center' },
}, {
  Header: '# of POIs',
  accessor: 'count',
  style: { 'textAlign': 'center' }
}];
