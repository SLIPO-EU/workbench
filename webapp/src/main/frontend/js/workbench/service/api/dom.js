const downloadBlob = (blob, fileName) => {
  if ((typeof global !== 'undefined') && (global.hasOwnProperty('URL'))) {
    const url = global.URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.download = fileName;
    a.href = url;
    a.style = "display: none";

    document.body.appendChild(a);
    a.click();

    // Cleanup
    setTimeout(() => {
      document.body.removeChild(a);
      global.URL.revokeObjectURL(url);
    }, 1000);
  }
};

const downloadUrl = (url, fileName) => {
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};

export default {
  downloadBlob,
  downloadUrl,
};
