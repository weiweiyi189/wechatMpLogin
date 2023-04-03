import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {mergeMap} from 'rxjs/operators';

/**
 * 请求前缀\只获取最后一次结果
 * 1. 处理异常
 * 2. 添加loading
 * 3. 合并接收数据
 */
@Injectable()
export class ApiPrefixAndMergeMapInterceptor implements HttpInterceptor {
  static forRoot(data: {
    api: string
  }): typeof ApiPrefixAndMergeMapInterceptor {
    this.api = data.api;
    return ApiPrefixAndMergeMapInterceptor;
  }

  static set api(api: string) {
    this._api = api;
  }

  static _api = undefined as unknown as string;
  public static DONT_INTERCEPT_HEADER_KEY = 'do_not_intercept';

  /**
   * 获取带有API前缀的URL.
   */
  public static getApiUrl(url: string): string {
    // 如果url不以http打头，则添加api前缀
    if (url.startsWith('/')) {
      return this._api + url;
    } else if (url.startsWith('http')) {
      return url;
    } else {
      // 如果不以/打头，比如clazz，则接拼的时候加入/
      return this._api + '/' + url;
    }
  }

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler,
  ): Observable<HttpEvent<any>> {
    if (typeof ApiPrefixAndMergeMapInterceptor._api === 'undefined') {
      console.warn('ApiPrefixAndMergeMapInterceptor: 未设置API，不拦截请求');
      return next.handle(request);
    }
    // 当带有不拦截的前缀时，不进行拦截
    let url = request.url;
    if ('true' !== request.headers.get(ApiPrefixAndMergeMapInterceptor.DONT_INTERCEPT_HEADER_KEY)) {
      url = ApiPrefixAndMergeMapInterceptor.getApiUrl(request.url);
    }

    // 由于request是个不可变对象，所以需要克隆一个req出来
    const req = request.clone({url});
    // 转发到下一个
    return next.handle(req).pipe(
      // return next.handle(request).pipe(
      // mergeMap = 如果在短期内发生相同的提交，则只返回最后一个
      mergeMap((event: any) => {
        return of(event);
      })
    );
  }
}
