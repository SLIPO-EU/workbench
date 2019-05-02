import * as React from 'react';
import PropTypes from 'prop-types';

import CodeMirror from 'react-codemirror';
import 'codemirror/mode/shell/shell';

import {
  Card,
  CardBody,
  Col,
  Row,
} from 'reactstrap';

class DockerLogViewer extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    data: PropTypes.string,
    file: PropTypes.object.isRequired,
  }

  render() {
    if (!this.props.data) {
      return null;
    }

    return (
      <div>
        <Card>
          <CardBody>
            <Row className="mb-4">
              <Col>
                <i className="fa fa-bug"></i>
                <span>{` Log File : ${this.props.file.filePath.split('/').reverse()[0]}`}</span>
              </Col>
            </Row>
            <Row>
              <Col>
                <CodeMirror
                  style={{ maxHeight: '60vh' }}
                  value={this.props.data}
                  options={{
                    mode: 'shell',
                    lineNumbers: true,
                    lineWrapping: true,
                    readOnly: true,
                  }}
                />
              </Col>
            </Row>
          </CardBody>
        </Card>
      </div>
    );
  }

}

export default DockerLogViewer;
