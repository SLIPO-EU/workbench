export const downloadLink = (data, fileName) => {
  if ((typeof global !== 'undefined') && (global.hasOwnProperty('URL'))) {
    let a = document.createElement('a');
    let url = window.URL.createObjectURL(data);

    a.href = url;
    a.download = fileName;
    a.click();

    window.URL.revokeObjectURL(url);
  }
};

export default {
  downloadLink,
};
