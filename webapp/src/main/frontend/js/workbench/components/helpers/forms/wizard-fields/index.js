import * as Fields from '../fields/';
import decorateWizardField from './decorate-wizard-field';

export const TextField = decorateWizardField(Fields.TextField);
export const PasswordField = decorateWizardField(Fields.PasswordField);
export const SelectField = decorateWizardField(Fields.SelectField);
export const MultiSelectField = decorateWizardField(Fields.MultiSelectField);
export const FileDropField = decorateWizardField(Fields.FileDropField);
export const FileSelectField = decorateWizardField(Fields.FileSelectField);
