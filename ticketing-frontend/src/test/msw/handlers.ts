export type MockedRequest = {
  url: URL;
  method: string;
  headers: Headers;
  body: unknown;
};

export type MockedResponse = {
  status?: number;
  headers?: HeadersInit;
  body?: BodyInit | null;
};

export type RequestHandler = (
  request: MockedRequest,
) => MockedResponse | undefined | Promise<MockedResponse | undefined>;

export const handlers: RequestHandler[] = [];

export function jsonResponse(
  body: unknown,
  init: Omit<MockedResponse, "body"> = {},
): MockedResponse {
  return {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init.headers ?? {}),
    },
    body: JSON.stringify(body),
  };
}
