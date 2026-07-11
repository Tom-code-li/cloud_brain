import http from '@ohos.net.http';

function safeResult(response: http.HttpResponse): string {
  if (response.result === null || response.result === undefined) {
    return '';
  }
  if (typeof response.result === 'string') {
    return response.result;
  }
  return JSON.stringify(response.result);
}

export class HttpClient {
  static async get(url: string): Promise<string> {
    const httpRequest = http.createHttp();
    try {
      const response = await httpRequest.request(url, {
        method: http.RequestMethod.GET,
        connectTimeout: 60000,
        readTimeout: 60000
      });
      if (response.responseCode < 200 || response.responseCode >= 300) {
        throw new Error('HTTP ' + response.responseCode.toString() + ': ' + safeResult(response));
      }
      return safeResult(response);
    } finally {
      httpRequest.destroy();
    }
  }

  static async post(url: string, body: string): Promise<string> {
    const httpRequest = http.createHttp();
    try {
      const response = await httpRequest.request(url, {
        method: http.RequestMethod.POST,
        header: { 'Content-Type': 'application/json' },
        extraData: body,
        connectTimeout: 60000,
        readTimeout: 60000
      });
      if (response.responseCode < 200 || response.responseCode >= 300) {
        throw new Error('HTTP ' + response.responseCode.toString() + ': ' + safeResult(response));
      }
      return safeResult(response);
    } finally {
      httpRequest.destroy();
    }
  }

  static async put(url: string, body: string): Promise<string> {
    const httpRequest = http.createHttp();
    try {
      const response = await httpRequest.request(url, {
        method: http.RequestMethod.PUT,
        header: { 'Content-Type': 'application/json' },
        extraData: body,
        connectTimeout: 60000,
        readTimeout: 60000
      });
      if (response.responseCode < 200 || response.responseCode >= 300) {
        throw new Error('HTTP ' + response.responseCode.toString() + ': ' + safeResult(response));
      }
      return safeResult(response);
    } finally {
      httpRequest.destroy();
    }
  }
}
