import {
  initializeGroups,
} from '../state';

export function undoReducer(state, action) {
  if (state.undo.length === 1) {
    return state;
  }
  const undo = [...state.undo];
  const current = undo.splice(undo.length - 1, 1);
  const redo = [current[0], ...state.redo];
  const snapshot = undo[undo.length - 1];

  return {
    ...state,
    groups: snapshot.groups.length === 0 ? initializeGroups() : [...snapshot.groups],
    steps: [...snapshot.steps],
    resources: [...snapshot.resources],
    undo,
    redo,
  };
}

export function redoReducer(state, action) {
  if (state.undo.length === 0) {
    return state;
  }
  const undo = [...state.undo];
  const redo = [...state.redo];

  const current = redo.splice(0, 1);
  undo.push(current[0]);

  const snapshot = undo[undo.length - 1];

  return {
    ...state,
    groups: [...snapshot.groups],
    steps: [...snapshot.steps],
    resources: [...snapshot.resources],
    undo,
    redo,
  };
}
