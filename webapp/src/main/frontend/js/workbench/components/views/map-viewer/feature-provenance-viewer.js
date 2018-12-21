import * as React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

import {
  Card,
  CardBody,
  CardHeader,
} from 'reactstrap';

const urlProperties = [{
  id: 'homepage',
  type: 'link',
}, {
  id: 'image',
  type: 'image',
}];

class FeatureTimeLineViewer extends React.Component {

  constructor(props) {
    super(props);
  }

  static propTypes = {
    provenance: PropTypes.object,
  }

  handleRowAction(rowInfo, e, handleOriginal) {
    switch (e.target.getAttribute('data-action')) {
      default:
        if (handleOriginal) {
          handleOriginal();
        }
        break;
    }
  }

  isUrl(value) {
    const urlRegEx = /^(http:\/\/www\.|https:\/\/www\.|http:\/\/|https:\/\/)?[a-z0-9]+([-.]{1}[a-z0-9]+)*\.[a-z]{2,5}(:[0-9]{1,5})?(\/.*)?$/gi;

    return urlRegEx.test(value);
  }

  renderCell(cell) {
    const urlProperty = urlProperties.find(p => p.id === cell.property);
    const isUrl = urlProperty && this.isUrl(cell.value);

    if (isUrl) {
      switch (urlProperty.type) {
        case 'link':
          return (
            <a href={cell.value} target="_blank">{cell.value}</a>
          );
        case 'image':
          return (
            <img className="slipo-tl-image" src={cell.value} />
          );
      }
    }

    return (
      <span>{cell.value}</span>
    );
  }

  render() {
    const { provenance } = this.props;

    if (!provenance) {
      return null;
    }

    return (
      <Card>
        <CardHeader className="handle">
          <i className="fa fa-map-marker"></i>
          <span>{`${provenance.stepName} - ${provenance.featureId}`}</span>
        </CardHeader>
        <CardBody>
          <div style={{ overflow: 'auto', height: 480 }}>
            <table className="slipo-tl-table">
              <tbody>
                {/* Steps */}
                <tr key="pivot-row">
                  {
                    provenance.stepRow.map((cell, index) => {
                      return (
                        <td
                          key={`step-${index}`}
                          rowSpan={cell.rowSpan || 1}
                          colSpan={cell.colSpan || 1}
                          className={classnames({
                            'slipo-tl-step': true,
                            'slipo-tl-cell': index !== 0,
                            'slipo-tl-cell-odd': index !== 0 && index % 2 === 1,
                            'slipo-tl-cell-even': index !== 0 && index % 2 === 0,
                            'slipo-tl-selected': cell.selected === true,
                          })}
                        >
                          <i className={`${cell.iconClass || ''} pr-2`} />{cell.value}
                        </td>
                      );
                    })
                  }
                </tr>
                {/* Inputs */}
                <tr key="input-row">
                  {
                    provenance.inputRow.map((cell, index) => {
                      return (
                        <td
                          key={`input-${index}`}
                          rowSpan={cell.rowSpan || 1}
                          colSpan={cell.colSpan || 1}
                          className={classnames({
                            'slipo-tl-cell': true,
                            'slipo-tl-cell-odd': cell.step % 2 === 1,
                            'slipo-tl-cell-even': cell.step % 2 === 0,
                            'slipo-tl-selected': cell.selected === true,
                          })}
                        >
                          {cell.value}
                        </td>
                      );
                    })
                  }
                </tr>
                {/* Values */}
                {
                  provenance.dataRows.map((row, rowIndex) => {
                    return (
                      <tr key={`row-${rowIndex}`}>
                        {
                          row.map((cell, cellIndex) => {
                            return (
                              <td
                                key={`cell-${rowIndex}-${cellIndex}`}
                                rowSpan={cell.rowSpan || 1}
                                colSpan={cell.colSpan || 1}
                                className={classnames({
                                  'slipo-tl-cell': true,
                                  'slipo-tl-cell-odd': cellIndex !== 0 && cell.step % 2 === 1,
                                  'slipo-tl-cell-even': cellIndex !== 0 && cell.step % 2 === 0,
                                  'slipo-tl-selected': cell.selected || cell.modified === true,
                                })}
                              >
                                {this.renderCell(cell)}
                              </td>
                            );
                          })
                        }
                      </tr>
                    );
                  })
                }
              </tbody>
            </table>
          </div>
        </CardBody>
      </Card>
    );
  }

}

export default FeatureTimeLineViewer;
