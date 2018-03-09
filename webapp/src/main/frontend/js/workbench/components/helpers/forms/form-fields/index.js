import * as Fields from '../fields/';
import decorateFormField from './decorate-form-field';

export const TextField = decorateFormField(Fields.TextField);
export const TextAreaField = decorateFormField(Fields.TextAreaField);
export const PasswordField = decorateFormField(Fields.PasswordField);
export const SelectField = decorateFormField(Fields.SelectField);
export const MultiSelectField = decorateFormField(Fields.MultiSelectField);
export const FileDropField = decorateFormField(Fields.FileDropField);
export const FileSelectField = decorateFormField(Fields.FileSelectField);
