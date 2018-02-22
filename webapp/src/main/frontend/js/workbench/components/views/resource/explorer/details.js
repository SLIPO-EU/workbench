import * as React from 'react';
import * as PropTypes from 'prop-types';

import {
  Col,
  Row,
} from 'reactstrap';

import {
  FormattedTime,
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
    resource: PropTypes.object.isRequired,
    version: PropTypes.number,
    intl: intlShape.isRequired,
  }

  render() {
    const { resource, version, intl } = this.props;
    const r = (resource.version === version) ? resource : resource.revisions.find((v) => v.version === version);

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
              value={version.toString()}
              readOnly={true}
            />
          </Col>
          <Col>
            <TextField
              id="versionCount"
              label="# of Versions"
              value={(resource.revisions.length || 1).toString()}
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
              value={intl.formatDate(r.createdOn)}
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
              value={intl.formatDate(r.updatedOn)}
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
              value={r.metadata.size.toString()}
              readOnly={true}
            />
          </Col>
        </Row>
      </div>
    );
  }
}

export default injectIntl(ResourceDetails);
