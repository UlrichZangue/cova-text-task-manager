import type { InputHTMLAttributes, ReactNode, TextareaHTMLAttributes } from "react";

interface FieldShellProps {
  label: string;
  htmlFor: string;
  error?: string;
  hint?: string;
  children: ReactNode;
}

function FieldShell({ label, htmlFor, error, hint, children }: FieldShellProps) {
  return (
    <div className="field">
      <label className="field__label" htmlFor={htmlFor}>
        {label}
      </label>
      {children}
      {error ? (
        <p className="field__error">{error}</p>
      ) : hint ? (
        <p className="field__hint">{hint}</p>
      ) : null}
    </div>
  );
}

interface InputFieldProps extends InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
  hint?: string;
}

export function InputField({ label, error, hint, id, ...props }: InputFieldProps) {
  const fieldId = id ?? props.name;
  return (
    <FieldShell label={label} htmlFor={fieldId ?? "field"} error={error} hint={hint}>
      <input id={fieldId} className="field__control" aria-invalid={Boolean(error)} {...props} />
    </FieldShell>
  );
}

interface TextareaFieldProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label: string;
  error?: string;
}

export function TextareaField({ label, error, id, ...props }: TextareaFieldProps) {
  const fieldId = id ?? props.name;
  return (
    <FieldShell label={label} htmlFor={fieldId ?? "field"} error={error}>
      <textarea
        id={fieldId}
        className="field__control field__textarea"
        aria-invalid={Boolean(error)}
        {...props}
      />
    </FieldShell>
  );
}
