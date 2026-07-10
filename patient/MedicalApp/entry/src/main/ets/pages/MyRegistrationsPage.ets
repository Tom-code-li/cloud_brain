import { router, promptAction } from '@kit.ArkUI';
import { BusinessError } from '@kit.BasicServicesKit';
import { Registration } from '../models/Registration';
import { RegistrationService } from '../services/RegistrationService';
import { PatientService } from '../services/PatientService';
import { Patient } from '../models/Patient';
import { AppState } from './LoginPage';
import { Result } from '../common/Result';

function getFeeStatusColor(status: string): string {
  if (status === '待支付') {
    return '#FF9800';
  } else if (status === '已支付') {
    return '#4CAF50';
  } else if (status === '已退费') {
    return '#999999';
  }
  return '#999999';
}

function getStatusColor(status: string): string {
  if (status === '待确认') {
    return '#2196F3';
  } else if (status === '已完成') {
    return '#4CAF50';
  } else if (status === '已取消') {
    return '#F44336';
  }
  return '#999999';
}

function isCancellable(feeStatus: string, status: string): boolean {
  return feeStatus === '待支付' || status === '待确认';
}

@Entry
@Component
struct MyRegistrationsPage {
  @State registrations: Registration[] = [];
  @State isLoading: boolean = true;
  @State isRefreshing: boolean = false;
  @State hasError: boolean = false;
  @State errorDetail: string = '';

  aboutToAppear(): void {
    this.loadRegistrations();
  }

  loadRegistrations(): void {
    this.isLoading = true;
    this.isRefreshing = false;
    this.hasError = false;
    this.errorDetail = '';
    if (AppState.userId === 0) {
      this.isLoading = false;
      return;
    }
    PatientService.getPatientInfo(AppState.userId)
      .then((infoResult: Result<Patient>) => {
        if (infoResult.success && infoResult.data !== null) {
          const patientId: number = infoResult.data.patientId;
          RegistrationService.getMyRegistrations(patientId)
            .then((result: Result<Registration[]>) => {
              if (result.success && result.data !== null) {
                this.registrations = result.data;
              }
              this.isLoading = false;
            })
            .catch((err: Error) => {
              this.isLoading = false;
              this.hasError = true;
              this.errorDetail = '挂号数据加载失败: ' + err.message;
            });
        } else {
          this.isLoading = false;
          this.hasError = true;
          this.errorDetail = '患者信息获取失败: ' + infoResult.message;
        }
      })
      .catch((err: Error) => {
        this.isLoading = false;
        this.hasError = true;
        this.errorDetail = '网络错误: ' + err.message;
      });
  }

  handleRefresh(): void {
    this.isRefreshing = true;
    this.hasError = false;
    if (AppState.userId === 0) {
      this.isRefreshing = false;
      return;
    }
    PatientService.getPatientInfo(AppState.userId)
      .then((infoResult: Result<Patient>) => {
        if (infoResult.success && infoResult.data !== null) {
          const patientId: number = infoResult.data.patientId;
          RegistrationService.getMyRegistrations(patientId)
            .then((result: Result<Registration[]>) => {
              if (result.success && result.data !== null) {
                this.registrations = result.data;
              }
              this.isRefreshing = false;
            })
            .catch(() => {
              this.isRefreshing = false;
              this.hasError = true;
            });
        } else {
          this.isRefreshing = false;
          this.hasError = true;
        }
      })
      .catch(() => {
        this.isRefreshing = false;
        this.hasError = true;
      });
  }

  showCancelConfirm(reg: Registration): void {
    promptAction.showDialog({
      title: '取消挂号',
      message: '确定要取消该挂号吗？',
      buttons: [
        { text: '暂不取消', color: '#999999' },
        { text: '确定取消', color: '#F44336' }
      ]
    }).then((data) => {
      if (data.index === 1) {
        this.doCancel(reg);
      }
    });
  }

  doCancel(reg: Registration): void {
    RegistrationService.cancelRegistration(reg.registrationId)
      .then((result: Result<string>) => {
        if (result.success) {
          promptAction.showToast({
            message: '取消成功',
            duration: 2000
          });
          this.loadRegistrations();
        }
      })
      .catch((error: BusinessError) => {
        promptAction.showDialog({
          title: '取消失败',
          message: error.message,
          buttons: [
            { text: '确定', color: '#007AFF' }
          ]
        });
      });
  }

  build() {
    Column() {
      Row() {
        Text('<')
          .fontSize(22)
          .fontColor('#007AFF')
          .onClick(() => {
            router.back();
          })
        Text('我的挂号')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')
          .layoutWeight(1)
          .textAlign(TextAlign.Center)
        Row()
          .width(24)
          .height(24)
      }
      .width('100%')
      .height(56)
      .padding({ left: 16, right: 16 })
      .backgroundColor('#FFFFFF')

      if (this.isLoading && this.registrations.length === 0) {
        Column() {
          LoadingProgress()
            .width(40)
            .height(40)
            .color('#007AFF')
          Text('加载中...')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ top: 8 })
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else if (this.hasError && this.registrations.length === 0) {
        Column() {
          Text('加载失败')
            .fontSize(16)
            .fontColor('#FF3B30')

          Text(this.errorDetail !== '' ? this.errorDetail : '未知错误')
            .fontSize(12)
            .fontColor('#999999')
            .maxLines(3)
            .textOverflow({ overflow: TextOverflow.Ellipsis })
            .margin({ top: 8, left: 32, right: 32 })
            .textAlign(TextAlign.Center)

          Text('点击重试')
            .fontSize(14)
            .fontColor('#007AFF')
            .margin({ top: 12 })
            .onClick(() => {
              this.loadRegistrations();
            })
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else if (this.registrations.length === 0 && !this.isRefreshing) {
        Column() {
          Text('暂无挂号记录')
            .fontSize(16)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else {
        Refresh({ refreshing: $$this.isRefreshing }) {
          List() {
            ForEach(this.registrations, (reg: Registration) => {
              ListItem() {
                Column() {
                  Row() {
                    Text(reg.deptName)
                      .fontSize(16)
                      .fontWeight(FontWeight.Bold)
                      .fontColor('#333333')
                    Text(reg.doctorName)
                      .fontSize(14)
                      .fontColor('#666666')
                      .margin({ left: 8 })
                  }
                  .width('100%')

                  Row() {
                    Text('挂号单号: ' + reg.registrationNo)
                      .fontSize(13)
                      .fontColor('#999999')
                    Text('排队号: ' + reg.queueNo.toString())
                      .fontSize(13)
                      .fontColor('#999999')
                      .margin({ left: 16 })
                  }
                  .width('100%')
                  .margin({ top: 8 })

                  Text('挂号时间: ' + reg.registeredAt)
                    .fontSize(13)
                    .fontColor('#999999')
                    .margin({ top: 6 })

                  Row() {
                    Text(reg.feeStatus)
                      .fontSize(12)
                      .fontColor('#FFFFFF')
                      .backgroundColor(getFeeStatusColor(reg.feeStatus))
                      .borderRadius(4)
                      .padding({ left: 6, right: 6, top: 2, bottom: 2 })

                    Text(reg.status)
                      .fontSize(12)
                      .fontColor('#FFFFFF')
                      .backgroundColor(getStatusColor(reg.status))
                      .borderRadius(4)
                      .padding({ left: 6, right: 6, top: 2, bottom: 2 })
                      .margin({ left: 8 })

                    Text('¥' + reg.registrationFee.toFixed(2))
                      .fontSize(16)
                      .fontColor('#FF6B35')
                      .fontWeight(FontWeight.Bold)
                      .layoutWeight(1)
                      .textAlign(TextAlign.End)

                    if (isCancellable(reg.feeStatus, reg.status)) {
                      Text('取消')
                        .fontSize(12)
                        .fontColor('#F44336')
                        .padding({ left: 12, right: 12, top: 6, bottom: 6 })
                        .border({ width: 1, color: '#F44336' })
                        .borderRadius(4)
                        .margin({ left: 12 })
                        .onClick(() => {
                          this.showCancelConfirm(reg);
                        })
                    }
                  }
                  .width('100%')
                  .margin({ top: 10 })
                  .alignItems(VerticalAlign.Center)
                }
                .width('100%')
                .padding(16)
                .alignItems(HorizontalAlign.Start)
              }
              .backgroundColor('#FFFFFF')
              .borderRadius(8)
              .margin({ left: 16, right: 16, top: 8, bottom: 4 })
            }, (reg: Registration) => reg.registrationId.toString())
          }
          .width('100%')
        }
        .width('100%')
        .layoutWeight(1)
        .backgroundColor('#F5F5F5')
        .padding({ top: 4 })
        .onRefreshing(() => {
          this.handleRefresh();
        })
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }
}
