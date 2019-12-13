import * as React from 'react';
import * as PropTypes from 'prop-types';
import { ResponsivePie } from '@nivo/pie';

/**
 * A wrapper component for {@link ResponsivePie }.
 */
class PieChart extends React.Component {

  static propTypes = {
    data: PropTypes.arrayOf(
      PropTypes.shape({
        id: PropTypes.string.isRequired,
        label: PropTypes.string.isRequired,
        value: PropTypes.number.isRequired,
        color: PropTypes.string.isRequired,
      }).isRequired
    ).isRequired,
  }

  render() {
    const { data } = this.props;

    return (
      <div className="slipo-chart-container" style={{ display: "flex", flexWrap: "wrap", height: 350 }} ref={(el) => { this._container = el; }}>
        <ResponsivePie
          data={data}
          margin={{ top: 20, right: 10, bottom: 80, left: 10 }}
          padAngle={0.7}
          cornerRadius={3}
          colors={{ scheme: 'nivo' }}
          borderWidth={1}
          borderColor={{ from: 'color', modifiers: [['darker', 0.2]] }}
          enableRadialLabels={false}
          slicesLabelsSkipAngle={10}
          slicesLabelsTextColor="#333333"
          animate={true}
          motionStiffness={90}
          motionDamping={15}
          legends={[
            {
              anchor: 'bottom',
              direction: 'row',
              translateY: 56,
              itemWidth: 100,
              itemHeight: 18,
              itemTextColor: '#999',
              symbolSize: 18,
              symbolShape: 'circle',
              effects: [
                {
                  on: 'hover',
                  style: {
                    itemTextColor: '#000'
                  }
                }
              ]
            }
          ]}
        />
      </div >
    );
  }
}

export default PieChart;
