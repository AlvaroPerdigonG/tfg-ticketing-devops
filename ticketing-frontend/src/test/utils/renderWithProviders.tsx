/* eslint-disable react-refresh/only-export-components */
import type { PropsWithChildren, ReactElement } from "react";
import { render, type RenderOptions } from "@testing-library/react";
import { ConfigProvider } from "antd";
import { MemoryRouter, type MemoryRouterProps } from "react-router-dom";

type RenderWithProvidersOptions = Omit<RenderOptions, "wrapper"> & {
  router?: MemoryRouterProps;
};

function Providers({ children, router }: PropsWithChildren<{ router?: MemoryRouterProps }>) {
  if (router) {
    return (
      <ConfigProvider>
        <MemoryRouter {...router}>{children}</MemoryRouter>
      </ConfigProvider>
    );
  }

  return <ConfigProvider>{children}</ConfigProvider>;
}

export function renderWithProviders(ui: ReactElement, options: RenderWithProvidersOptions = {}) {
  const { router, ...renderOptions } = options;

  return render(ui, {
    wrapper: ({ children }) => <Providers router={router}>{children}</Providers>,
    ...renderOptions,
  });
}
