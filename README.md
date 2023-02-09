博客
https://segmentfault.com/a/1190000043395324

# 前台
进入到web文件夹
npm install 安装依赖
ng s启动项目

# 后台
进入api目录
后台mvn install 安装依赖
进入application.yml文件，配置好数据库和公众号信息

# nginx
将本目录下的nginx.conf加入到nginx配置中
启动nginx服务

# 登陆
默认账号密码: admin/admin

当前逻辑是：用户需要用账号密码进入系统，绑定微信后，才能进行微信登录。

所以请先进入系统，绑定微信账号。
