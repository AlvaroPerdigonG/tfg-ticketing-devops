import { handlers } from "./handlers";

type UnhandledRequestPolicy = "error" | "warn" | "bypass";

export const server = {
  listen: (_options?: { onUnhandledRequest?: UnhandledRequestPolicy }) => {
    void handlers;
  },
  resetHandlers: () => {
    void handlers;
  },
  close: () => {
    void handlers;
  },
};
