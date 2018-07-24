export function formatFileSize(size) {
  if (size >= 1048576) {
    return (size / 1048576).toFixed(2) + ' MB';
  } else if (size >= 1024) {
    return (size / 1024).toFixed(2) + ' kB';
  }
  return size + ' bytes';
}
