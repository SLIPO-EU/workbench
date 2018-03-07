import * as Types from '../types';

export function groupReducer(state, action) {
  switch (action.type) {
    case Types.REMOVE_STEP:
      return state
        .map((group) => {
          // Remove step from group
          return {
            ...group,
            steps: group.steps.filter((key) => {
              return (key !== action.step.key);
            })
          };
        }).filter((group, index, array) => {
          // Remove empty groups except for the last one
          return !((index > 0) && (group.steps.length === 0) && (group !== array[array.length - 1]));
        }).map((group, index) => {
          // Update indexes to avoid duplicate group keys
          return {
            ...group,
            key: index,
          };
        });

    default:
      return state;
  }
}
