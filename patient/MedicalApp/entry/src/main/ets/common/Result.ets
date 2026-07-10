export class Result<T> {
  success: boolean = false;
  message: string = '';
  data: T | null = null;
}
