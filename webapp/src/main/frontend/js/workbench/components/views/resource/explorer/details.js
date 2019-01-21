import * as React from 'react';
import * as PropTypes from 'prop-types';

import {
  Button,
  Col,
  Row,
} from 'reactstrap';

import {
  injectIntl,
  intlShape,
} from 'react-intl';

import {
  formatFileSize
} from '../../../../util';

import {
  TextAreaField,
  TextField,
} from '../../../helpers/forms/fields';

class ResourceDetails extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    intl: intlShape.isRequired,
    resource: PropTypes.object.isRequired,
    viewMap: PropTypes.func.isRequired,
  }

  render() {
    const { resource: r, intl } = this.props;

    return (
      <div>
        <Row>
          <Col>
            <TextField
              id="name"
              label="Name"
              value={r.metadata.name}
              readOnly={true}
            />
          </Col>
          <Col>
            <TextField
              id="version"
              label="Version"
              value={r.version.toString()}
              readOnly={true}
            />
          </Col>
          <Col>
            <TextField
              id="versionCount"
              label="# of Versions"
              value={(r.revisions.length || 1).toString()}
              readOnly={true}
            />
          </Col>
        </Row>
        <Row>
          <Col>
            <TextAreaField
              id="description"
              label="Description"
              value={r.metadata.description}
              rows={5}
              readOnly={true}
            />
          </Col>
        </Row>
        <Row>
          <Col>
            <TextField
              id="createdOn"
              label="Created On"
              value={intl.formatTime(r.createdOn, { day: 'numeric', month: 'numeric', year: 'numeric' })}
              readOnly={true}
            />
          </Col>
          <Col>
            <TextField
              id="createdBy"
              label="Created By"
              value={r.createdBy.name}
              readOnly={true}
            />
          </Col>
        </Row>
        <Row>
          <Col>
            <TextField
              id="updatedOn"
              label="Updated On"
              value={intl.formatTime(r.updatedOn, { day: 'numeric', month: 'numeric', year: 'numeric' })}
              readOnly={true}
            />
          </Col>
          <Col>
            <TextField
              id="updatedBy"
              label="Updated By"
              value={r.updatedBy.name}
              readOnly={true}
            />
          </Col>
        </Row>
        <Row>
          <Col>
            <TextField
              id="filePath"
              label="Filename"
              value={r.filePath}
              readOnly={true}
            />
          </Col>
        </Row>
        <Row>
          <Col>
            <TextField
              id="initialFormat"
              label="Initial Format"
              value={r.inputFormat || '-'}
              readOnly={true}
            />
          </Col>
          <Col>
            <TextField
              id="format"
              label="Format"
              value={r.format}
              readOnly={true}
            />
          </Col>
        </Row>
        <Row>
          <Col>
            <TextField
              id="fileSize"
              label="File Size"
              value={formatFileSize(r.fileSize)}
              readOnly={true}
            />
          </Col>
          <Col>
            <TextField
              id="entities"
              label="# of Elements"
              value={r.numberOfEntities ? r.numberOfEntities.toString() : '0'}
              readOnly={true}
            />
          </Col>
        </Row>
        {r.exported &&
          <Row>
            <Col>
              <div className="float-right">
                <Button color="primary" onClick={() => this.props.viewMap(r.id, r.version)} className="float-left"><i className="fa fa-map-o mr-1"></i> View Map</Button>
              </div>
            </Col>
          </Row>
        }
      </div>
    );
  }
}

export default injectIntl(ResourceDetails);
