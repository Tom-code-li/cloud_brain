import { router } from '@kit.ArkUI';
import { PatientService } from '../services/PatientService';
import { Result } from '../common/Result';
import { SysUser } from '../models/SysUser';

export class AppState {
  static userId: number = 0;
  static username: string = '';
  static patientId: number = 0;
  static patientName: string = '';
}

@Entry
@Component
struct LoginPage {
  @State username: string = 'patient_guo';
  @State password: string = '{noop}Patient@123';
  @State errorMsg: string = '';
  @State isLoading: boolean = false;

  build() {
    Column() {
      Column() {
        Text('医疗助手')
          .fontSize(28)
          .fontColor('#007AFF')
          .fontWeight(FontWeight.Bold)
          .margin({ top: 80, bottom: 40 })
      }
      .width('100%')
      .alignItems(HorizontalAlign.Center)

      Column() {
        TextInput({ placeholder: '请输入用户名', text: this.username })
          .width('85%')
          .height(48)
          .fontSize(16)
          .backgroundColor('#F5F5F5')
          .borderRadius(8)
          .padding({ left: 16, right: 16 })
          .margin({ bottom: 16 })
          .onChange((value: string) => {
            this.username = value;
          })

        TextInput({ placeholder: '请输入密码', text: this.password })
          .width('85%')
          .height(48)
          .fontSize(16)
          .backgroundColor('#F5F5F5')
          .borderRadius(8)
          .padding({ left: 16, right: 16 })
          .type(InputType.Password)
          .margin({ bottom: 24 })
          .onChange((value: string) => {
            this.password = value;
          })

        if (this.errorMsg !== '') {
          Text(this.errorMsg)
            .fontSize(14)
            .fontColor('#FF3B30')
            .margin({ bottom: 16 })
            .width('85%')
        }

        Button(this.isLoading ? '登录中...' : '登录')
          .width('85%')
          .height(48)
          .fontSize(18)
          .fontColor('#FFFFFF')
          .backgroundColor(this.isLoading ? '#80BFFF' : '#007AFF')
          .borderRadius(8)
          .enabled(!this.isLoading)
          .onClick(() => {
            this.handleLogin();
          })

        Row() {
          Text('还没有账号？')
            .fontSize(14)
            .fontColor('#999999')

          Text('注册')
            .fontSize(14)
            .fontColor('#007AFF')
            .margin({ left: 4 })
            .onClick(() => {
              router.pushUrl({ url: 'pages/RegisterPage' });
            })
        }
        .margin({ top: 24 })
      }
      .width('100%')
      .alignItems(HorizontalAlign.Center)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }

  handleLogin(): void {
    if (this.username.trim() === '') {
      this.errorMsg = '请输入用户名';
      return;
    }
    if (this.password.trim() === '') {
      this.errorMsg = '请输入密码';
      return;
    }

    this.errorMsg = '';
    this.isLoading = true;

    PatientService.login(this.username, this.password)
      .then((result: Result<SysUser>) => {
        if (result.success && result.data !== null) {
          this.finishLogin(result.data);
        } else if (result.message !== '' && result.message.indexOf('已在别处登录') !== -1) {
          this.isLoading = false;
          this.showForceLoginDialog();
        } else {
          this.isLoading = false;
          this.errorMsg = result.message !== '' ? result.message : '登录失败，请重试';
        }
      })
      .catch(() => {
        this.isLoading = false;
        AlertDialog.show({
          title: '登录失败',
          message: '网络错误，请检查网络连接后重试',
          confirm: {
            value: '确定',
            action: () => {}
          }
        });
      });
  }

  showForceLoginDialog(): void {
    AlertDialog.show({
      title: '提示',
      message: '当前帐号已在别处登录，是否在本设备登录？别处账号将自动退出。',
      primaryButton: {
        value: '确定登录',
        fontColor: '#007AFF',
        action: () => {
          this.handleForceLogin();
        }
      },
      secondaryButton: {
        value: '取消',
        fontColor: '#999999',
        action: () => {}
      }
    });
  }

  handleForceLogin(): void {
    this.isLoading = true;
    PatientService.loginForce(this.username, this.password)
      .then((result: Result<SysUser>) => {
        this.isLoading = false;
        if (result.success && result.data !== null) {
          this.finishLogin(result.data);
        } else {
          this.errorMsg = result.message !== '' ? result.message : '登录失败，请重试';
        }
      })
      .catch(() => {
        this.isLoading = false;
        AlertDialog.show({
          title: '登录失败',
          message: '网络错误，请检查网络连接后重试',
          confirm: {
            value: '确定',
            action: () => {}
          }
        });
      });
  }

  finishLogin(user: SysUser): void {
    AppState.userId = user.userId;
    AppState.username = user.username;
    router.replaceUrl({ url: 'pages/HomePage' });
  }
}
