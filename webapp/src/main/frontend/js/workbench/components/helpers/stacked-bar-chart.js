import * as React from 'react';
import * as PropTypes from 'prop-types';
import { ResponsiveBar } from '@nivo/bar';

/**
 * A wrapper component for {@link ResponsiveBar}.
 *
 * @class StackedBarChart
 * @extends {React.Component}
 */
class StackedBarChart extends React.Component {

  constructor(props) {
    super(props);

  }

  static propTypes = {
    data: PropTypes.array.isRequired,
  }

  render() {
    const height = this.props.data.length * 50;

    return (
      <div
        className="slipo-chart-container"
        style={{ display: "flex", flexWrap: "wrap", height: (height < 500 ? 500 : height) }}
      >
        <ResponsiveBar
          data={this.props.data}
          keys={this.props.keys}
          minValue={0}
          enableLabel={false}
          indexBy={this.props.indexBy}
          margin={{
            top: 50,
            right: 130,
            bottom: 50,
            left: 310,
          }}
          padding={0.3}
          layout="horizontal"
          borderColor="#00ff80"
          axisTop={null}
          axisRight={null}
          axisBottom={{
            tickSize: 5,
            tickPadding: 5,
            tickRotation: 0,
            legendPosition: "middle",
            legendOffset: 32
          }}
          axisLeft={{
            format: (value) => {
              return value.length > 55 ? value.substr(0, 55) + '...' : value;
            },
            tickSize: 5,
            tickPadding: 5,
            tickRotation: 0,
            legendPosition: "middle",
            legendOffset: -40
          }}
          labelSkipWidth={12}
          labelSkipHeight={12}
          labelTextColor={{
            from: "color",
            modifiers: [
              [
                "darker",
                1.6
              ]
            ]
          }}
          animate={true}
          motionStiffness={90}
          motionDamping={15}
          tooltip={this.props.tooltip}
        />
      </div >
    );
  }
}

export default StackedBarChart;
