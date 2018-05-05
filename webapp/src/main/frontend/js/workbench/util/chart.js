export function kpiDataToSeriesByPrefix(prefix, data) {
  return data
    .map((d) => {
      return {
        field: d.key,
        count: d.value,
      };
    })
    .filter((d) => {
      return d.field.startsWith(prefix);
    })
    .map((d) => {
      return {
        ...d,
        field: d.field.substring(prefix.length),
      };
    })
    .sort((v1, v2) => {
      if (v1.field > v2.field) {
        return -1;
      }
      if (v1.field < v2.field) {
        return 1;
      }
      return 0;
    });
}
