import { router } from '@kit.ArkUI';
import { PatientService } from '../services/PatientService';
import { Result } from '../common/Result';

@Entry
@Component
struct RegisterPage {
  @State username: string = '';
  @State password: string = '';
  @State confirmPassword: string = '';
  @State patientName: string = '';
  @State phone: string = '';
  @State idCard: string = '';
  @State gender: string = '1';
  @State birthday: string = '';
  @State errorMsg: string = '';
  @State successMsg: string = '';
  @State isLoading: boolean = false;

  build() {
    Column() {
      Row() {
        Text('←')
          .fontSize(24)
          .fontColor('#007AFF')
          .width(40)
          .onClick(() => {
            router.back();
          })

        Text('注册账号')
          .fontSize(20)
          .fontColor('#333333')
          .fontWeight(FontWeight.Bold)
          .layoutWeight(1)
          .textAlign(TextAlign.Center)

        Blank().width(40)
      }
      .width('100%')
      .height(56)
      .padding({ left: 16, right: 16 })

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
          .margin({ bottom: 16 })
          .onChange((value: string) => {
            this.password = value;
          })

        TextInput({ placeholder: '请确认密码', text: this.confirmPassword })
          .width('85%')
          .height(48)
          .fontSize(16)
          .backgroundColor('#F5F5F5')
          .borderRadius(8)
          .padding({ left: 16, right: 16 })
          .type(InputType.Password)
          .margin({ bottom: 16 })
          .onChange((value: string) => {
            this.confirmPassword = value;
          })

        TextInput({ placeholder: '请输入姓名', text: this.patientName })
          .width('85%')
          .height(48)
          .fontSize(16)
          .backgroundColor('#F5F5F5')
          .borderRadius(8)
          .padding({ left: 16, right: 16 })
          .margin({ bottom: 16 })
          .onChange((value: string) => {
            this.patientName = value;
          })

        TextInput({ placeholder: '请输入手机号', text: this.phone })
          .width('85%')
          .height(48)
          .fontSize(16)
          .backgroundColor('#F5F5F5')
          .borderRadius(8)
          .padding({ left: 16, right: 16 })
          .type(InputType.PhoneNumber)
          .margin({ bottom: 16 })
          .onChange((value: string) => {
            this.phone = value;
          })

        TextInput({ placeholder: '请输入身份证号', text: this.idCard })
          .width('85%')
          .height(48)
          .fontSize(16)
          .backgroundColor('#F5F5F5')
          .borderRadius(8)
          .padding({ left: 16, right: 16 })
          .margin({ bottom: 16 })
          .onChange((value: string) => {
            this.idCard = value;
          })

        Row() {
          Text('性别')
            .fontSize(16)
            .fontColor('#333333')
            .margin({ right: 16 })

          Row() {
            Radio({ value: '1', group: 'gender' })
              .checked(this.gender === '1')
              .onChange((checked: boolean) => {
                if (checked) {
                  this.gender = '1';
                }
              })
            Text('男')
              .fontSize(16)
              .fontColor('#333333')
              .margin({ left: 4, right: 24 })
          }

          Row() {
            Radio({ value: '2', group: 'gender' })
              .checked(this.gender === '2')
              .onChange((checked: boolean) => {
                if (checked) {
                  this.gender = '2';
                }
              })
            Text('女')
              .fontSize(16)
              .fontColor('#333333')
              .margin({ left: 4 })
          }
        }
        .width('85%')
        .height(48)
        .alignItems(VerticalAlign.Center)
        .margin({ bottom: 16 })

        TextInput({ placeholder: '生日（选填，如 1990-01-01）', text: this.birthday })
          .width('85%')
          .height(48)
          .fontSize(16)
          .backgroundColor('#F5F5F5')
          .borderRadius(8)
          .padding({ left: 16, right: 16 })
          .margin({ bottom: 24 })
          .onChange((value: string) => {
            this.birthday = value;
          })

        if (this.errorMsg !== '') {
          Text(this.errorMsg)
            .fontSize(14)
            .fontColor('#FF3B30')
            .margin({ bottom: 12 })
            .width('85%')
        }

        if (this.successMsg !== '') {
          Text(this.successMsg)
            .fontSize(14)
            .fontColor('#34C759')
            .margin({ bottom: 12 })
            .width('85%')
        }

        Button(this.isLoading ? '注册中...' : '注册')
          .width('85%')
          .height(48)
          .fontSize(18)
          .fontColor('#FFFFFF')
          .backgroundColor(this.isLoading ? '#80BFFF' : '#007AFF')
          .borderRadius(8)
          .enabled(!this.isLoading)
          .onClick(() => {
            this.handleRegister();
          })
      }
      .width('100%')
      .alignItems(HorizontalAlign.Center)
      .margin({ top: 20 })
      .layoutWeight(1)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }

  handleRegister(): void {
    this.errorMsg = '';
    this.successMsg = '';

    if (this.username.trim() === '') {
      this.errorMsg = '请输入用户名';
      return;
    }
    if (this.password.trim() === '') {
      this.errorMsg = '请输入密码';
      return;
    }
    if (this.password.length < 6) {
      this.errorMsg = '密码长度不能少于6位';
      return;
    }
    if (this.password !== this.confirmPassword) {
      this.errorMsg = '两次输入的密码不一致';
      return;
    }
    if (this.patientName.trim() === '') {
      this.errorMsg = '请输入姓名';
      return;
    }
    if (this.phone.trim() === '') {
      this.errorMsg = '请输入手机号';
      return;
    }
    if (this.idCard.trim() === '') {
      this.errorMsg = '请输入身份证号';
      return;
    }

    this.isLoading = true;

    let birthdayToSend: string = this.birthday.trim();
    if (birthdayToSend === '') {
      birthdayToSend = '';
    }

    PatientService.register(this.username, this.password, this.patientName, this.phone, this.gender, birthdayToSend, this.idCard)
      .then((result: Result<string>) => {
        this.isLoading = false;
        if (result.success) {
          AlertDialog.show({
            title: '注册成功',
            message: '账号注册成功，请返回登录',
            confirm: {
              value: '确定',
              action: () => {
                router.back();
              }
            }
          });
        } else {
          this.errorMsg = result.message !== '' ? result.message : '注册失败，请重试';
        }
      })
      .catch(() => {
        this.isLoading = false;
        AlertDialog.show({
          title: '注册失败',
          message: '网络错误，请检查网络连接后重试',
          confirm: {
            value: '确定',
            action: () => {}
          }
        });
      });
  }
}
