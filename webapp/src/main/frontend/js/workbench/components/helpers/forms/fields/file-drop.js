import React from 'react';
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';

import decorateField from './form-field';
import formatFileSize from '../../../../util/file-size';

/**
 * Component for selecting a single file to upload
 *
 * @export
 * @class FileDrop
 * @extends {React.Component}
 */
export class FileDrop extends React.Component {

  constructor() {
    super();
    this.state = {
      file: null,
    };
  }

  static propTypes = {
    id: PropTypes.string.isRequired,
    value: PropTypes.any,
    onChange: PropTypes.func.isRequired,
  }

  static defaultProps = {
    style: {
      textAlign: 'center',
      fontSize: '3em',
      color: '#656565',
      border: '1px dotted #656565'
    },
  }

  componentWillMount() {
    if (this.props.value) {
      this.setState({ file: this.props.value });
    }
  }

  render() {
    const { onChange, style } = this.props;
    return (
      <div>
        <Dropzone
          onDrop={(accepted, rejected) => {
            if (rejected.length) {
              console.error('rejected file:', rejected);
            }
            const file = accepted && accepted.length && accepted[0];
            this.setState({ file });

            if (typeof onChange === 'function') {
              onChange(file);
            }
          }}
          style={style}
          disableClick={false}
          multiple={false}
        >
          <i className="fa fa-cloud-upload fa-4x"></i>
        </Dropzone>
        {this.state.file && this.state.file.name}
        {this.state.file && ` (${formatFileSize(this.state.file.size)})`}
      </div>
    );
  }
}

export default decorateField(FileDrop);
