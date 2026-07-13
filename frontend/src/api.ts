export interface ApiError {
  code: string;
  message: string;
  details: Record<string, unknown>;
}

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  error: ApiError | null;
  timestamp: string;
}

export async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    headers: { "Content-Type": "application/json", ...(options?.headers ?? {}) },
    ...options
  });
  const payload = (await response.json()) as ApiResponse<T>;
  if (!payload.success) {
    throw new Error(payload.error?.message ?? "请求失败");
  }
  return payload.data;
}