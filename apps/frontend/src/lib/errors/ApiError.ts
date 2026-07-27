export class ApiError extends Error {
  public readonly status: number;
  public readonly code: string;
  public readonly retryable: boolean;
  public readonly details?: unknown;

  constructor(params: {
    status: number;
    code: string;
    message: string;
    retryable?: boolean;
    details?: unknown;
  }) {
    super(params.message);
    this.name = "ApiError";
    this.status = params.status;
    this.code = params.code;
    this.retryable = params.retryable ?? false;
    this.details = params.details;
    Object.setPrototypeOf(this, ApiError.prototype);
  }
}
