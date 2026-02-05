export interface ApiResponse<T> {
  success: boolean;   // Backend'deki boolean success
  message: string;   // Backend'deki String message
  data: T;           // Backend'deki T data (generic yapı)
}
