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
      <div style={colorPrimary ? ({ ["--color-primary" as string]: colorPrimary } as React.CSSProperties) : undefined}>
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
              color: isSelected ? theme?.token?.colorPrimary ?? "#0c9136" : "#111827",
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
  Title: ({ level = 1, style, children }: { level?: 1 | 2 | 3 | 4 | 5; style?: React.CSSProperties; children: React.ReactNode }) => {
    const Tag = `h${level}` as keyof React.JSX.IntrinsicElements;
    return <Tag style={style}>{children}</Tag>;
  },
  Text: ({ children, style }: { children: React.ReactNode; type?: "secondary"; style?: React.CSSProperties }) => (
    <span style={{ color: "#6b7280", ...style }}>{children}</span>
  ),
  Paragraph: ({ children, style }: { children: React.ReactNode; type?: "secondary"; style?: React.CSSProperties }) => (
    <p style={{ color: "#6b7280", ...style }}>{children}</p>
  ),
};

export function Card({ children }: { children: React.ReactNode }) {
  return <section style={{ border: "1px solid #e5e7eb", borderRadius: 10, padding: 20 }}>{children}</section>;
}

export function Button({ children, onClick, type, size }: { children: React.ReactNode; onClick?: () => void; type?: "primary" | "default"; size?: "small" | "middle" | "large" }) {
  const theme = useContext(ThemeContext);
  return (
    <button
      type="button"
      onClick={onClick}
      style={{ background: type === "primary" || !type ? theme?.token?.colorPrimary ?? "#0c9136" : "#fff", color: type === "primary" || !type ? "#fff" : "#111827", border: type === "primary" || !type ? "none" : "1px solid #d1d5db", padding: size === "large" ? "10px 16px" : "8px 12px", borderRadius: 6 }}
    >
      {children}
    </button>
  );
}

export function Result({ title, subTitle, extra }: { status?: string; title: string; subTitle?: string; extra?: React.ReactNode }) {
  return (
    <section style={{ textAlign: "center", padding: 32 }}>
      <h1>{title}</h1>
      {subTitle && <p>{subTitle}</p>}
      {extra}
    </section>
  );
}


export function Space({ children, direction = "horizontal", size = 8, style }: { children: React.ReactNode; direction?: "horizontal" | "vertical"; size?: number; style?: React.CSSProperties }) {
  return <div style={{ display: "flex", flexDirection: direction === "vertical" ? "column" : "row", gap: size, ...(style ?? {}) }}>{children}</div>;
}

export function Tag({ children }: { children: React.ReactNode; color?: string }) {
  return <span style={{ background: "#f3f4f6", borderRadius: 999, padding: "2px 8px", fontSize: 12 }}>{children}</span>;
}

export function Skeleton({ paragraph }: { active?: boolean; paragraph?: { rows: number } }) {
  return <div aria-label="skeleton">Cargando... ({paragraph?.rows ?? 3})</div>;
}

export function Empty({ description }: { description?: React.ReactNode }) {
  return <div>{description ?? "Sin datos"}</div>;
}

export function Alert({ message, description }: { type?: string; showIcon?: boolean; message: React.ReactNode; description?: React.ReactNode }) {
  return <div><strong>{message}</strong>{description ? <p>{description}</p> : null}</div>;
}

export type TableProps<T> = {
  rowKey: keyof T | ((row: T) => React.Key);
  columns: Array<{
    title: React.ReactNode;
    dataIndex: keyof T;
    key: string;
    width?: number;
    ellipsis?: boolean;
    render?: (value: unknown, row: T) => React.ReactNode;
  }>;
  dataSource: T[];
  pagination?: { pageSize?: number; hideOnSinglePage?: boolean };
};

export function Table<T extends Record<string, unknown>>({ rowKey, columns, dataSource }: TableProps<T>) {
  const keyGetter = typeof rowKey === "function" ? rowKey : (row: T) => row[rowKey] as React.Key;

  return (
    <table style={{ width: "100%", borderCollapse: "collapse" }}>
      <thead>
        <tr>
          {columns.map((column) => (
            <th key={column.key} style={{ textAlign: "left", borderBottom: "1px solid #e5e7eb", padding: "8px 4px" }}>
              {column.title}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {dataSource.map((row) => (
          <tr key={keyGetter(row)}>
            {columns.map((column) => {
              const value = row[column.dataIndex];
              return (
                <td key={column.key} style={{ borderBottom: "1px solid #f3f4f6", padding: "8px 4px" }}>
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
