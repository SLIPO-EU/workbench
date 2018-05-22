export const EnumErrorLevel = {
  INFO: 'INFO',
  WARN: 'WARN',
  ERROR: 'ERROR',
};

const ErrorLevelValues = {
  [EnumErrorLevel.INFO]: 0,
  [EnumErrorLevel.WARN]: 1,
  [EnumErrorLevel.ERROR]: 2,
};


export class ServerError extends Error {

  constructor(errors) {
    super(errors[0].description);

    this.name = 'ServerError';
    this.errors = errors;
    this.level = errors.reduce((level, err) => {
      if (Object.keys(ErrorLevelValues).includes(err.level)) {
        return (ErrorLevelValues[level] < ErrorLevelValues[err.level] ? err.level : level);
      }
      return EnumErrorLevel.ERROR;
    }, EnumErrorLevel.INFO);
  }

}
