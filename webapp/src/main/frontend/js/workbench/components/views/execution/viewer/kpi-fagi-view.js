import * as React from 'react';
import PropTypes from 'prop-types';

import {
  Col,
  Card,
  CardBody,
  Row,
} from 'reactstrap';

import {
  KpiSharedView,
} from '.';

class KpiFagiView extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    data: PropTypes.arrayOf(PropTypes.shape({
      key: PropTypes.string.isRequired,
      value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
      description: PropTypes.string,
    })),
    file: PropTypes.object.isRequired,
  }

  render() {
    if (!this.props.data) {
      return null;
    }

    return (
      <div>
        <Row>
          <Col>
            <Card>
              <CardBody>
                <KpiSharedView
                  data={this.props.data}
                  file={this.props.file}
                />
              </CardBody>
            </Card>
          </Col>
        </Row>
      </div>
    );
  }
}

export default KpiFagiView;
