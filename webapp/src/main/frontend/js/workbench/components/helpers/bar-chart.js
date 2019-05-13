import * as React from 'react';
import * as PropTypes from 'prop-types';
import { ResponsiveBar } from '@nivo/bar';

/**
 * A wrapper component for {@link ResponsiveBar}.
 *
 * @class BarChart
 * @extends {React.Component}
 */
class BarChart extends React.Component {

  constructor(props) {
    super(props);

  }

  static propTypes = {
    data: PropTypes.arrayOf(
      PropTypes.shape({
        field: PropTypes.string.isRequired,
        count: PropTypes.number.isRequired,
      }).isRequired
    ).isRequired,
  }

  render() {
    const height = this.props.data.length * 50;
    const maxLen = this.props.data.reduce((result, value) => {
      if (value.field.length * 10 > result) {
        return (value.field.length * 10);
      }
      return result;
    }, 60);

    return (
      <div className="slipo-chart-container" style={{ display: "flex", flexWrap: "wrap", height: (height < 500 ? 500 : height) }} ref={(el) => { this._container = el; }}>
        <ResponsiveBar
          data={this.props.data}
          keys={[
            "count",
          ]}
          minValue={0}
          indexBy="field"
          margin={{
            "top": 50,
            "right": 130,
            "bottom": 50,
            "left": maxLen,
          }}
          isInteractive={false}
          padding={0.3}
          layout="horizontal"
          borderColor="#00ff80"
          colorBy="index"
          axisBottom={{
            "orient": "bottom",
            "tickSize": 5,
            "tickPadding": 5,
            "tickRotation": 0,
            "legendPosition": "middle",
            "legendOffset": 36
          }}
          axisLeft={{
            "orient": "left",
            "tickSize": 5,
            "tickPadding": 5,
            "tickRotation": 0,
            "legendPosition": "middle",
            "legendOffset": -40
          }}
          enableGridX={true}
          enableGridY={false}
          labelSkipWidth={12}
          labelSkipHeight={12}
          labelTextColor="inherit:darker(1.6)"
          animate={true}
          motionStiffness={90}
          motionDamping={15}
        />
      </div >
    );
  }
}

export default BarChart;
