import { handlers, type MockedRequest, type RequestHandler } from "./handlers";

type UnhandledRequestPolicy = "error" | "warn" | "bypass";

const runtimeHandlers: RequestHandler[] = [];
let initialHandlers: RequestHandler[] = [...handlers];
let originalFetch: typeof global.fetch | undefined;
let onUnhandledRequest: UnhandledRequestPolicy = "error";

async function toMockedRequest(input: RequestInfo | URL, init?: RequestInit): Promise<MockedRequest> {
  const request = input instanceof Request ? input : new Request(input, init);
  const contentType = request.headers.get("content-type");

  let body: unknown = undefined;
  if (contentType?.includes("application/json")) {
    body = await request.clone().json();
  } else if (request.method !== "GET" && request.method !== "HEAD") {
    body = await request.clone().text();
  }

  return {
    url: new URL(request.url),
    method: request.method,
    headers: request.headers,
    body,
  };
}

export const server = {
  listen: (options?: { onUnhandledRequest?: UnhandledRequestPolicy }) => {
    onUnhandledRequest = options?.onUnhandledRequest ?? "error";
    initialHandlers = [...handlers];
    runtimeHandlers.splice(0, runtimeHandlers.length, ...initialHandlers);

    originalFetch = global.fetch.bind(global);

    global.fetch = async (input: RequestInfo | URL, init?: RequestInit): Promise<Response> => {
      const request = await toMockedRequest(input, init);

      for (const handler of runtimeHandlers) {
        const mockedResponse = await handler(request);
        if (mockedResponse) {
          return new Response(mockedResponse.body, {
            status: mockedResponse.status ?? 200,
            headers: mockedResponse.headers,
          });
        }
      }

      if (onUnhandledRequest === "bypass") {
        return originalFetch!(input, init);
      }

      const message = `Unhandled request: ${request.method} ${request.url.toString()}`;
      if (onUnhandledRequest === "warn") {
        // eslint-disable-next-line no-console
        console.warn(message);
        return originalFetch!(input, init);
      }

      throw new Error(message);
    };
  },

  use: (...nextHandlers: RequestHandler[]) => {
    runtimeHandlers.unshift(...nextHandlers);
  },

  resetHandlers: () => {
    runtimeHandlers.splice(0, runtimeHandlers.length, ...initialHandlers);
  },

  close: () => {
    if (originalFetch) {
      global.fetch = originalFetch;
    }
  },
};
