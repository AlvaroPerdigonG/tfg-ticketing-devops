import type { MockedRequest, MockedResponse, RequestHandler } from "./handlers";

function createMethodHandler(method: string) {
  return (
    path: string,
    resolver: (
      request: MockedRequest,
    ) => MockedResponse | undefined | Promise<MockedResponse | undefined>,
  ): RequestHandler => {
    return (request) => {
      if (request.method !== method) return undefined;
      if (request.url.pathname !== path) return undefined;
      return resolver(request);
    };
  };
}

export const http = {
  get: createMethodHandler("GET"),
  post: createMethodHandler("POST"),
  patch: createMethodHandler("PATCH"),
};
