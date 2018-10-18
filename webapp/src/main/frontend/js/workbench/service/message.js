import * as React from 'react';

import {
  toast,
} from 'react-toastify';

import {
  EnumErrorLevel,
} from '../model';

import {
  ToastTemplate,
} from '../components/helpers';

const displayText = (text, level = EnumErrorLevel.ERROR, iconClass = null) => {
  toast.dismiss();

  switch (level) {
    case EnumErrorLevel.WARN:
      if (React.isValidElement(text)) {
        toast.warn(text);
      } else {
        toast.warn(
          <ToastTemplate iconClass={iconClass ? iconClass : 'fa-warning'} text={text} />
        );
      }
      break;
    case EnumErrorLevel.INFO:
      if (React.isValidElement(text)) {
        toast.info(text);
      } else {
        toast.info(
          <ToastTemplate iconClass={iconClass ? iconClass : 'fa-info-circle'} text={text} />
        );
      }
      break;
    default:
      if (React.isValidElement(text)) {
        toast.error(text);
      } else {
        toast.error(
          <ToastTemplate iconClass={iconClass ? iconClass : 'fa-exclamation-circle'} text={text} />
        );
      }
      break;
  }
};

const displayHtml = (html, level = EnumErrorLevel.ERROR, iconClass = null) => {
  toast.dismiss();

  switch (level) {
    case EnumErrorLevel.WARN:
      toast.warn(
        <ToastTemplate iconClass={iconClass ? iconClass : 'fa-warning'} html={html} />
      );
      break;
    case EnumErrorLevel.INFO:
      toast.info(
        <ToastTemplate iconClass={iconClass ? iconClass : 'fa-info-circle'} html={html} />
      );
      break;
    default:
      toast.error(
        <ToastTemplate iconClass={iconClass ? iconClass : 'fa-exclamation-circle'} html={html} />
      );
      break;
  }
};

const error = (text, iconClass = null) => {
  displayText(text, EnumErrorLevel.ERROR, iconClass);
};

const errorHtml = (html, iconClass = null) => {
  displayHtml(html, EnumErrorLevel.ERROR, iconClass);
};

const warn = (text, iconClass = null) => {
  displayText(text, EnumErrorLevel.WARN, iconClass);
};

const warnHtml = (html, iconClass = null) => {
  displayHtml(html, EnumErrorLevel.WARN, iconClass);
};

const info = (text, iconClass = null) => {
  displayText(text, EnumErrorLevel.INFO, iconClass);
};

const infoHtml = (html, iconClass = null) => {
  displayHtml(html, EnumErrorLevel.INFO, iconClass);
};

const success = (text, iconClass = null) => {
  if (React.isValidElement(text)) {
    toast.success(text);
  } else {
    toast.success(
      <ToastTemplate iconClass={iconClass ? iconClass : 'fa-exclamation-circle'} text={text} />
    );
  }
};

export default {
  error,
  errorHtml,
  info,
  infoHtml,
  success,
  warn,
  warnHtml,
};
