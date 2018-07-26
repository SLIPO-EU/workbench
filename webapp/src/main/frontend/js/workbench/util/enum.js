function isDefined(type, value) {
  return (!!Object.keys(type).find((key) => type[key] === value));
}

export {
  isDefined,
};
