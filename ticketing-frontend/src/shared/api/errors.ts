export class ApiError extends Error {
  readonly status: number;
  readonly code?: string;
  readonly details?: unknown;

  constructor(message: string, status: number, code?: string, details?: unknown) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
    this.details = details;
  }
}

export async function toApiError(response: Response): Promise<ApiError> {
  const status = response.status;

  // Intentamos leer cuerpo JSON tipo { message, code, ... }
  let body: any = null;
  try {
    body = await response.clone().json();
  } catch {
    // ignore
  }

  const message =
    body?.message ||
    body?.error ||
    response.statusText ||
    `Request failed with status ${status}`;

  const code = body?.code;
  return new ApiError(message, status, code, body);
}