import _ from 'lodash';
import React from 'react';

import safeEval from 'safe-eval';
import { flatten } from 'flat';

import ReactSelect from 'react-select';

import {
  Card,
  CardBody,
  Col,
  FormGroup,
  FormText,
  Label,
  Row,
} from 'reactstrap';

import { FileDrop } from '../helpers/forms/fields/file-drop';

import {
  KpiDeerView,
  KpiFagiView,
  KpiLimesView,
  KpiTripleGeoView,
} from './execution/viewer';

import {
  message,
} from '../../service';

import { EnumTool, ToolTitles } from '../../model/process-designer';

const selectStyle = {
  control: (base) => {
    return {
      ...base,
      borderRadius: '0px',
      borderColor: '#cccccc',
      ':focus': {
        borderColor: '#8ad4ee',
      },
      boxShadow: 'none',
    };
  },
  container: (base) => ({
    ...base,
    zIndex: '1002',
  }),
  menu: (base) => ({
    ...base,
    borderRadius: '0px',
    boxShadow: 'none',
    marginTop: '-1px',
    border: '1px solid #cccccc',
  }),
};

const tools = [{
  value: EnumTool.TripleGeo,
  label: ToolTitles[EnumTool.TripleGeo],
}, {
  value: EnumTool.LIMES,
  label: ToolTitles[EnumTool.LIMES],
}, {
  value: EnumTool.FAGI,
  label: ToolTitles[EnumTool.FAGI],
}, {
  value: EnumTool.DEER,
  label: ToolTitles[EnumTool.DEER],
}, {
  value: EnumTool.ReverseTripleGeo,
  label: ToolTitles[EnumTool.ReverseTripleGeo],
}];

class KpiViewer extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      data: null,
      file: null,
      original: null,
      tool: EnumTool.TripleGeo,
    };
  }

  resolveKpiComponent(tool) {
    switch (tool) {
      case EnumTool.DEER:
        return KpiDeerView;
      case EnumTool.FAGI:
        return KpiFagiView;
      case EnumTool.LIMES:
        return KpiLimesView;
      case EnumTool.ReverseTripleGeo:
      case EnumTool.TripleGeo:
        return KpiTripleGeoView;
    }

    return null;
  }

  loadFile(file) {
    const { tool } = this.state;

    if (!file) {
      message.error('Select a file');
      return;
    } else if (!file.name.endsWith('.json')) {
      message.error('Select a JSON file');
      return;
    }

    const reader = new FileReader();
    const filePath = file.name;

    const promise = new Promise((resolve, reject) => {
      reader.onload = () => {
        try {
          // For LIMES convert text to simple JavaScript object
          const data = tool === EnumTool.LIMES ? safeEval(reader.result) : JSON.parse(reader.result);
          // Flatten data
          const flattenedData = flatten(data);
          // Remove empty objects
          const result = Object.keys(flattenedData).reduce((current, key) => {
            if (!_.isObject(flattenedData[key])) {
              current.data.push({
                key,
                value: flattenedData[key],
                description: null,
              });
            }
            return current;
          }, { data: [], original: data });
          // Sort values
          result.data.sort((a1, a2) => a1.key > a2.key ? 1 : -1);

          resolve({ ...result, file: { filePath } });
        } catch (err) {
          reject(err.message);
        }
      };
    });
    reader.readAsText(file);

    promise
      .then(({ data, original, file }) => {
        this.setState({
          data,
          file,
          original,
        });
      })
      .catch((reason) => {
        message.error('Failed to read output file. Reason: ' + reason, 'fa-warning');
      });
  }

  render() {
    const { data, file, original, tool } = this.state;

    const ComponentKpi = this.resolveKpiComponent(tool);

    return (
      <div className="animated fadeIn">
        <Card>
          <CardBody className="card-body">
            <Row>
              <Col>
                <FormGroup>
                  <Label for="tool">Select SLIPO Toolkit Component</Label>
                  <ReactSelect
                    name="tool"
                    id="tool"
                    value={tools.find(opt => opt.value === tool)}
                    onChange={(option) => this.setState({ tool: option.value, data: null, original: null, file: null })}
                    options={tools}
                    styles={selectStyle}
                  />
                </FormGroup>
              </Col>
            </Row>
            <Row>
              <Col>
                <FormGroup>
                  <Label for="debug">KPI file</Label>
                  <FileDrop
                    id="debug"
                    value={null}
                    onChange={(data) => {
                      this.loadFile(data);
                    }}
                  />
                  <FormText color="muted">Drop a JSON file to load KPI data</FormText>
                </FormGroup>
              </Col>
            </Row>
          </CardBody>
        </Card>

        {data && ComponentKpi &&
          <Card>
            <CardBody>
              <ComponentKpi
                data={data}
                file={file}
                original={original}
              />
            </CardBody>
          </Card>
        }
      </div>
    );
  }

}

export default KpiViewer;
