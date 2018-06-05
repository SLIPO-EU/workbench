const downloadBlob = (data, fileName) => {
  if ((typeof global !== 'undefined') && (global.hasOwnProperty('URL'))) {
    let a = document.createElement('a');
    let url = window.URL.createObjectURL(data);

    a.href = url;
    a.download = fileName;
    a.click();

    window.URL.revokeObjectURL(url);
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
