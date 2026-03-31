/* eslint-disable react-refresh/only-export-components */
import React, { createContext, useContext } from "react";

type ThemeConfig = {
  token?: {
    colorPrimary?: string;
    colorInfo?: string;
  };
};

type ConfigProviderProps = {
  children: React.ReactNode;
  theme?: ThemeConfig;
  locale?: unknown;
};

const ThemeContext = createContext<ThemeConfig | undefined>(undefined);

export function ConfigProvider({ children, theme, locale }: ConfigProviderProps) {
  void locale;
  const colorPrimary = theme?.token?.colorPrimary;

  return (
    <ThemeContext.Provider value={theme}>
      <div
        style={
          colorPrimary
            ? ({ ["--color-primary" as string]: colorPrimary } as React.CSSProperties)
            : undefined
        }
      >
        {children}
      </div>
    </ThemeContext.Provider>
  );
}

type LayoutComponent = React.FC<React.HTMLAttributes<HTMLDivElement>> & {
  Header: React.FC<React.HTMLAttributes<HTMLElement>>;
  Sider: React.FC<React.HTMLAttributes<HTMLElement>>;
  Content: React.FC<React.HTMLAttributes<HTMLElement>>;
};

const BaseLayout: React.FC<React.HTMLAttributes<HTMLDivElement>> = ({ style, ...props }) => (
  <div {...props} style={{ display: "flex", ...(style ?? {}) }} />
);

const Header: React.FC<React.HTMLAttributes<HTMLElement>> = ({ style, ...props }) => (
  <header {...props} style={{ ...(style ?? {}) }} />
);

const Sider: React.FC<React.HTMLAttributes<HTMLElement>> = ({ style, ...props }) => (
  <aside {...props} style={{ ...(style ?? {}) }} />
);

const Content: React.FC<React.HTMLAttributes<HTMLElement>> = ({ style, ...props }) => (
  <main {...props} style={{ ...(style ?? {}) }} />
);

export const Layout = BaseLayout as LayoutComponent;
Layout.Header = Header;
Layout.Sider = Sider;
Layout.Content = Content;

type MenuItem = { key?: React.Key; label?: React.ReactNode; icon?: React.ReactNode };
export type MenuProps = {
  items?: MenuItem[];
  selectedKeys?: string[];
  mode?: "inline" | "horizontal" | "vertical";
  onClick?: (info: { key: string }) => void;
  style?: React.CSSProperties;
};

export function Menu({ items = [], selectedKeys = [], onClick, style }: MenuProps) {
  const theme = useContext(ThemeContext);
  return (
    <nav style={style}>
      {items.map((item) => {
        const key = item.key?.toString() ?? "";
        const isSelected = selectedKeys.includes(key);
        return (
          <button
            key={key}
            type="button"
            onClick={() => onClick?.({ key })}
            style={{
              display: "flex",
              alignItems: "center",
              gap: 8,
              width: "100%",
              textAlign: "left",
              border: "none",
              background: isSelected ? "#f0fdf4" : "transparent",
              color: isSelected ? (theme?.token?.colorPrimary ?? "#0c9136") : "#111827",
              padding: "10px 16px",
              cursor: "pointer",
            }}
          >
            {item.icon}
            <span>{item.label}</span>
          </button>
        );
      })}
    </nav>
  );
}

export const Typography = {
  Title: ({
    level = 1,
    style,
    children,
  }: {
    level?: 1 | 2 | 3 | 4 | 5;
    style?: React.CSSProperties;
    children: React.ReactNode;
  }) => {
    const Tag = `h${level}` as keyof React.JSX.IntrinsicElements;
    return <Tag style={style}>{children}</Tag>;
  },
  Text: ({
    children,
    style,
  }: {
    children: React.ReactNode;
    type?: "secondary";
    style?: React.CSSProperties;
  }) => <span style={{ color: "#6b7280", ...style }}>{children}</span>,
  Paragraph: ({
    children,
    style,
  }: {
    children: React.ReactNode;
    type?: "secondary";
    style?: React.CSSProperties;
  }) => <p style={{ color: "#6b7280", ...style }}>{children}</p>,
};

type FormProps = {
  children: React.ReactNode;
  layout?: "vertical" | "horizontal";
  onFinish?: () => void;
};

type FormItemProps = {
  children: React.ReactNode;
  label?: React.ReactNode;
  style?: React.CSSProperties;
};

type FormComponent = React.FC<FormProps> & {
  Item: React.FC<FormItemProps>;
};

export const Form = (({ children, onFinish }: FormProps) => {
  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onFinish?.();
  };

  return <form onSubmit={handleSubmit}>{children}</form>;
}) as FormComponent;

Form.Item = ({ children, label, style }: FormItemProps) => {
  return (
    <label style={{ display: "flex", flexDirection: "column", gap: 6, ...(style ?? {}) }}>
      {label}
      {children}
    </label>
  );
};

export function Card({ children }: { children: React.ReactNode }) {
  return (
    <section style={{ border: "1px solid #e5e7eb", borderRadius: 10, padding: 20 }}>
      {children}
    </section>
  );
}

export function Button({
  children,
  onClick,
  type,
  size,
  disabled,
  style,
  htmlType,
  loading,
}: {
  children: React.ReactNode;
  onClick?: () => void;
  type?: "primary" | "default";
  size?: "small" | "middle" | "large";
  disabled?: boolean;
  style?: React.CSSProperties;
  htmlType?: "button" | "submit" | "reset";
  loading?: boolean;
}) {
  const theme = useContext(ThemeContext);
  return (
    <button
      type={htmlType ?? "button"}
      onClick={onClick}
      disabled={disabled || loading}
      style={{
        background:
          type === "primary" || !type ? (theme?.token?.colorPrimary ?? "#0c9136") : "#fff",
        color: type === "primary" || !type ? "#fff" : "#111827",
        border: type === "primary" || !type ? "none" : "1px solid #d1d5db",
        padding: size === "large" ? "10px 16px" : "8px 12px",
        borderRadius: 6,
        opacity: disabled || loading ? 0.6 : 1,
        cursor: disabled || loading ? "not-allowed" : "pointer",
        ...(style ?? {}),
      }}
    >
      {loading ? "Cargando..." : children}
    </button>
  );
}

export function Result({
  title,
  subTitle,
  extra,
}: {
  status?: string;
  title: string;
  subTitle?: string;
  extra?: React.ReactNode;
}) {
  return (
    <section style={{ textAlign: "center", padding: 32 }}>
      <h1>{title}</h1>
      {subTitle && <p>{subTitle}</p>}
      {extra}
    </section>
  );
}

export function Space({
  children,
  direction = "horizontal",
  size = 8,
  style,
}: {
  children: React.ReactNode;
  direction?: "horizontal" | "vertical";
  size?: number;
  style?: React.CSSProperties;
}) {
  return (
    <div
      style={{
        display: "flex",
        flexDirection: direction === "vertical" ? "column" : "row",
        gap: size,
        ...(style ?? {}),
      }}
    >
      {children}
    </div>
  );
}

export function Tag({ children }: { children: React.ReactNode; color?: string }) {
  return (
    <span style={{ background: "#f3f4f6", borderRadius: 999, padding: "2px 8px", fontSize: 12 }}>
      {children}
    </span>
  );
}

export function Skeleton({ paragraph }: { active?: boolean; paragraph?: { rows: number } }) {
  return <div aria-label="skeleton">Cargando... ({paragraph?.rows ?? 3})</div>;
}

export function Empty({ description }: { description?: React.ReactNode }) {
  return <div>{description ?? "Sin datos"}</div>;
}

export function Alert({
  message,
  description,
}: {
  type?: string;
  showIcon?: boolean;
  message: React.ReactNode;
  description?: React.ReactNode;
}) {
  return (
    <div>
      <strong>{message}</strong>
      {description ? <p>{description}</p> : null}
    </div>
  );
}

export type TableProps<T> = {
  rowKey: keyof T | ((row: T) => React.Key);
  columns: Array<{
    title: React.ReactNode;
    dataIndex?: keyof T;
    key: string;
    width?: number;
    ellipsis?: boolean;
    render?: (value: unknown, row: T) => React.ReactNode;
  }>;
  dataSource: T[];
  pagination?: { pageSize?: number; hideOnSinglePage?: boolean } | boolean;
  loading?: boolean;
};

export function Table<T extends Record<string, unknown>>({
  rowKey,
  columns,
  dataSource,
  loading,
}: TableProps<T>) {
  const keyGetter = typeof rowKey === "function" ? rowKey : (row: T) => row[rowKey] as React.Key;

  if (loading) {
    return <div>Cargando...</div>;
  }

  return (
    <table style={{ width: "100%", borderCollapse: "collapse" }}>
      <thead>
        <tr>
          {columns.map((column) => (
            <th
              key={column.key}
              style={{ textAlign: "left", borderBottom: "1px solid #e5e7eb", padding: "8px 4px" }}
            >
              {column.title}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {dataSource.map((row) => (
          <tr key={keyGetter(row)}>
            {columns.map((column) => {
              const value = column.dataIndex ? row[column.dataIndex] : undefined;
              return (
                <td
                  key={column.key}
                  style={{ borderBottom: "1px solid #f3f4f6", padding: "8px 4px" }}
                >
                  {column.render ? column.render(value, row) : (value as React.ReactNode)}
                </td>
              );
            })}
          </tr>
        ))}
      </tbody>
    </table>
  );
}

type InputProps = {
  value?: string;
  onChange?: (event: React.ChangeEvent<HTMLInputElement>) => void;
  placeholder?: string;
  maxLength?: number;
  [key: `aria-${string}`]: string | undefined;
};

type TextAreaProps = {
  value?: string;
  onChange?: (event: React.ChangeEvent<HTMLTextAreaElement>) => void;
  placeholder?: string;
  maxLength?: number;
  rows?: number;
  [key: `aria-${string}`]: string | undefined;
};

type InputComponent = React.FC<InputProps> & {
  TextArea: React.FC<TextAreaProps>;
};

export const Input = (({ value, onChange, placeholder, maxLength, ...ariaProps }: InputProps) => {
  return (
    <input
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      maxLength={maxLength}
      {...ariaProps}
      style={{
        border: "1px solid #d9d9d9",
        borderRadius: 6,
        padding: "6px 10px",
        minHeight: 32,
      }}
    />
  );
}) as InputComponent;

Input.TextArea = ({
  value,
  onChange,
  placeholder,
  maxLength,
  rows = 4,
  ...ariaProps
}: TextAreaProps) => {
  return (
    <textarea
      value={value}
      onChange={onChange}
      placeholder={placeholder}
      maxLength={maxLength}
      rows={rows}
      {...ariaProps}
      style={{
        border: "1px solid #d9d9d9",
        borderRadius: 6,
        padding: "6px 10px",
        width: "100%",
      }}
    />
  );
};

type SelectOption = {
  label: React.ReactNode;
  value: string;
};

export function Select({
  value,
  onChange,
  options,
  disabled,
  placeholder,
  style,
  ...ariaProps
}: {
  value?: string;
  onChange?: (value: string) => void;
  options?: SelectOption[];
  disabled?: boolean;
  placeholder?: string;
  style?: React.CSSProperties;
  [key: `aria-${string}`]: string | undefined;
}) {
  return (
    <select
      value={value ?? ""}
      onChange={(event) => onChange?.(event.target.value)}
      disabled={disabled}
      style={{
        border: "1px solid #d9d9d9",
        borderRadius: 6,
        padding: "6px 10px",
        minHeight: 32,
        ...(style ?? {}),
      }}
      {...ariaProps}
    >
      {placeholder ? <option value="">{placeholder}</option> : null}
      {(options ?? []).map((option) => (
        <option key={option.value} value={option.value}>
          {option.label}
        </option>
      ))}
    </select>
  );
}

export function Switch({
  checked,
  onChange,
}: {
  checked?: boolean;
  onChange?: (checked: boolean) => void;
}) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={Boolean(checked)}
      onClick={() => onChange?.(!checked)}
      style={{
        width: 44,
        height: 24,
        borderRadius: 999,
        border: "1px solid #d9d9d9",
        background: checked ? "#0c9136" : "#d9d9d9",
        cursor: "pointer",
        padding: 2,
        display: "flex",
        alignItems: "center",
        justifyContent: checked ? "flex-end" : "flex-start",
      }}
    >
      <span
        style={{
          width: 18,
          height: 18,
          borderRadius: "50%",
          background: "#fff",
          display: "inline-block",
        }}
      />
    </button>
  );
}

export function Tabs({
  items,
}: {
  items: Array<{ key: string; label: React.ReactNode; children: React.ReactNode }>;
}) {
  const [activeKey, setActiveKey] = React.useState(items[0]?.key);
  const active = items.find((item) => item.key === activeKey) ?? items[0];

  return (
    <div>
      <div
        role="tablist"
        style={{ display: "flex", gap: 6, borderBottom: "1px solid #f0f0f0", marginBottom: 16 }}
      >
        {items.map((item) => (
          <button
            key={item.key}
            role="tab"
            aria-selected={item.key === active.key}
            onClick={() => setActiveKey(item.key)}
            style={{
              border: "none",
              borderBottom: item.key === active.key ? "2px solid #0c9136" : "2px solid transparent",
              background: "transparent",
              color: item.key === active.key ? "#0c9136" : "#595959",
              padding: "8px 4px",
              cursor: "pointer",
              fontWeight: 500,
            }}
          >
            {item.label}
          </button>
        ))}
      </div>
      <div>{active?.children}</div>
    </div>
  );
}

export function Modal({
  open,
  title,
  children,
  onCancel,
  onOk,
  okText,
}: {
  open: boolean;
  title: React.ReactNode;
  children: React.ReactNode;
  onCancel?: () => void;
  onOk?: () => void;
  okText?: string;
}) {
  if (!open) {
    return null;
  }

  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(0, 0, 0, 0.45)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 1000,
      }}
    >
      <div
        style={{ background: "#fff", borderRadius: 8, width: 520, maxWidth: "90vw", padding: 20 }}
      >
        <h2 style={{ marginTop: 0 }}>{title}</h2>
        <div style={{ marginBottom: 16 }}>{children}</div>
        <div style={{ display: "flex", justifyContent: "flex-end", gap: 8 }}>
          <Button type="default" onClick={onCancel}>
            Cancelar
          </Button>
          <Button type="primary" onClick={onOk}>
            {okText ?? "OK"}
          </Button>
        </div>
      </div>
    </div>
  );
}

export const message = {
  success: (_content?: React.ReactNode) => undefined,
  error: (_content?: React.ReactNode) => undefined,
};
