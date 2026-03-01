import type { ReactNode } from "react";

type IconProps = {
  children?: ReactNode;
};

function DotIcon({ children }: IconProps) {
  return <span aria-hidden="true">{children ?? "•"}</span>;
}

export function DashboardOutlined() {
  return <DotIcon>◧</DotIcon>;
}

export function TagsOutlined() {
  return <DotIcon>🏷</DotIcon>;
}

export function FileAddOutlined() {
  return <DotIcon>＋</DotIcon>;
}

export function ProfileOutlined() {
  return <DotIcon>☰</DotIcon>;
}

export function TeamOutlined() {
  return <DotIcon>👥</DotIcon>;
}
