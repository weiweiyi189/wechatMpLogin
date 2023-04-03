import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';

/**
 * 在非cors的情况下，阻止浏览器在接收到401时弹窗
 */
export class Prevent401Popup implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let headers = req.headers;
    headers = headers.append('X-Requested-With', 'XMLHttpRequest');
    req = req.clone({headers});
    return next.handle(req);
  }
}
