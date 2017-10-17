export const JobSeries = {
  series: [{
    name: 'Jobs',
    legend: 'Jobs',
    points: [
      { x: 'Completed', y: 15, label: 'Completed' },
      { x: 'Running', y: 1, label: 'Running' },
      { x: 'Failed', y: 3, label: 'Failed' },
    ]
  }],
  options: {
    showLabels: true,
  }
};

export const QuotaSeries = {
  series: [{
    name: 'Quota',
    legend: 'Quota',
    points: [
      { x: '14/09/2017', y: 112 },
      { x: '15/09/2017', y: 83 },
      { x: '16/09/2017', y: 73 },
      { x: '17/09/2017', y: 41 },
    ]
  }]
};
