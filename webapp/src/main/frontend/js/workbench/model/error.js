export const EnumErrorLevel = {
  TRACE: 'TRACE',
  DEBUG: 'DEBUG',
  INFO: 'INFO',
  WARN: 'WARN',
  ERROR: 'ERROR',
};

const ErrorLevelValues = {
  [EnumErrorLevel.TRACE]: 0,
  [EnumErrorLevel.DEBUG]: 1,
  [EnumErrorLevel.INFO]: 2,
  [EnumErrorLevel.WARN]: 3,
  [EnumErrorLevel.ERROR]: 4,
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
