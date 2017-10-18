import * as React from 'react';
import * as PropTypes from 'prop-types';

import _ from 'lodash';

import { VictoryChart, VictoryBar, VictoryAxis, VictoryTheme, VictoryTooltip } from 'victory';

/**
 * A wrapper component for {@link BarChart}.
 *
 * @class BarChart
 * @extends {React.Component}
 */
class BarChart extends React.Component {

  constructor(props) {
    super(props);

  }

  componentDidMount() {

  }

  componentWillUnmount() {

  }

  propsToSeries() {
    let { showLabels } = this.props.options;

    return this.props.series.map((s) => {
      return s.points.map((p) => {
        let data = {
          x: p.x,
          y: p.y,
        };
        if (showLabels) {
          data.label = p.label || '';
        }
        return data;
      });
    });
  }

  render() {
    const series = this.propsToSeries();

    const data = series[0];

    return (
      <div className="slipo-chart-container" style={{ display: "flex", flexWrap: "wrap" }} ref={(el) => { this._container = el; }}>
        <VictoryChart
          width={600}
          theme={VictoryTheme.material}
          domainPadding={20}
        >
          <VictoryAxis
            tickValues={data.map(value => value.x)}
          />
          <VictoryAxis
            dependentAxis
          />
          <VictoryBar
            x="x"
            y="y"
            labelComponent={<VictoryTooltip />}
            data={series[0]}
          />
        </VictoryChart>
      </div >
    );
  }
}

BarChart.propTypes = {
  // Array of data point collections (series)
  series: PropTypes.arrayOf(
    PropTypes.shape({
      // Data point collection unique name
      name: PropTypes.string.isRequired,
      // Legend name
      legend: PropTypes.string,
      // Data points
      points: PropTypes.arrayOf(
        PropTypes.shape({
          x: PropTypes.any.isRequired,
          y: PropTypes.any.isRequired,
          // Optional data point label
          label: PropTypes.string,
        }).isRequired
      ),
    }).isRequired
  ).isRequired,
  // Configuration options
  options: PropTypes.shape({
    // True if labels should rendered
    showLabels: PropTypes.bool
  }),
};

BarChart.defaultProps = {
  options: {
    showLabels: false
  }
};

export default BarChart;
