const COPY = 'copy';

export function copy(elementId, text) {
  const element = document.getElementById(elementId) || null;
  if (!element) {
    return false;
  }

  if (document.queryCommandSupported(COPY)) {
    element.focus();
    element.value = text;
    element.select();
    document.execCommand(COPY);
    return true;
  }

  return false;
}
