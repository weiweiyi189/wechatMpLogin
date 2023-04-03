import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {first} from 'rxjs/operators';
import {UserService} from "../../service/user.service";
import {DomSanitizer, SafeUrl} from "@angular/platform-browser";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  loginModel = 'username' as 'username' | 'wechat';

  qrCodeSrc: SafeUrl | undefined;

  /**
   * 登录字段
   */
  formKeys = {
    username: 'username',
    password: 'password',
  };

  /** 登录表单对象 */
  loginForm: FormGroup;

  /** 错误信息 */
  errorInfo: string | undefined;

  /** 提交状态 */
  submitting = false;


  constructor(private builder: FormBuilder,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private userService: UserService,
              private sanitizer: DomSanitizer) {
    /** 创建表单 */
    this.loginForm = this.builder.group({
      username: ['', [Validators.minLength(5),
        Validators.maxLength(11),
        Validators.pattern('\\w+'),
        Validators.required]],
      password: ['', Validators.required],
    });
  }

  ngOnInit(): void {
    this.errorInfo = '';
    this.loginForm.valueChanges
      .subscribe(() => {
        this.errorInfo = '';
      });
  }

  onLogin(): void {
    console.log('执行了login方法');
    const user = {
      username: this.loginForm.get('username')!.value as string,
      password: this.loginForm.get('password')!.value as string,
    };
    this.login(user);
  }

  login(user: {username: string, password: string}) {
    this.userService.login(user)
      .subscribe(() => {
        this.userService.initCurrentLoginUser().subscribe({
          next: () => this.router.navigateByUrl('dashboard').then()
        });
      }, (response) => {
        this.errorInfo = '登录失败，请检查您填写的信息是否正确, 如若检查无误，可能是您的账号被冻结';
      });
  }

  /**
   * 微信扫码登录
   */
  onWeChatLogin() {
    this.userService.getLoginQrCode()
      .subscribe(src => {
        this.qrCodeSrc = this.sanitizer.bypassSecurityTrustUrl(src.replace(/\"/g, ""));
        this.loginModel = 'wechat';
        this.userService.onScanLoginQrCode$.pipe(first()).subscribe(data => {
          // 登录凭证
          const loginUid = data.body;
          this.login({username: loginUid, password: loginUid});
        });
      });
  }
}
