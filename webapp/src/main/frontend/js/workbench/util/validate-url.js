import fetch from 'isomorphic-fetch';

export function validateUrl(url) {
  if (!url || !url.startsWith('http')) {
    return Promise.reject('Enter valid url');
  }
  return fetch(url, {
    method: 'GET',
  })
    .then((res) => {
      if (!res.ok) {
        throw res.statusText;
      }
    })
    .catch((err) => {
      throw typeof err === 'string' ? err : 'Cannot resolve url';
    });
}
