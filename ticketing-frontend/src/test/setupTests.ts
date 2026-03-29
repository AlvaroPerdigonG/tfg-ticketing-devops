import "@testing-library/jest-dom";
import { cleanup } from "@testing-library/react";
import { afterAll, afterEach, beforeAll } from "vitest";
import { server } from "./msw/server";

beforeAll(() => {
  server.listen({ onUnhandledRequest: "error" });
});

afterEach(() => {
  cleanup();
  server.resetHandlers();
});

afterAll(() => {
  server.close();
});

const nativeGetComputedStyle = window.getComputedStyle.bind(window);

window.getComputedStyle = ((element: Element, pseudoElt?: string) => {
  if (pseudoElt) {
    return nativeGetComputedStyle(element);
  }

  return nativeGetComputedStyle(element);
}) as typeof window.getComputedStyle;

Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => undefined,
    removeListener: () => undefined,
    addEventListener: () => undefined,
    removeEventListener: () => undefined,
    dispatchEvent: () => false,
  }),
});

class ResizeObserverMock implements ResizeObserver {
  observe(): void {}
  unobserve(): void {}
  disconnect(): void {}
}

if (typeof window.ResizeObserver === "undefined") {
  window.ResizeObserver = ResizeObserverMock;
}
