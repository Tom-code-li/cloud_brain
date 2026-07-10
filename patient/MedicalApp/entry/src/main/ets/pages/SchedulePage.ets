import { router, promptAction } from '@kit.ArkUI';
import { BusinessError } from '@kit.BasicServicesKit';
import { DoctorSchedule } from '../models/DoctorSchedule';
import { RegistrationService } from '../services/RegistrationService';
import { PatientService } from '../services/PatientService';
import { Patient } from '../models/Patient';
import { NavParams } from './DepartmentsPage';
import { AppState } from './LoginPage';
import { Result } from '../common/Result';

@Entry
@Component
struct SchedulePage {
  @State schedules: DoctorSchedule[] = [];
  @State isLoading: boolean = true;

  aboutToAppear(): void {
    this.loadSchedules();
  }

  loadSchedules(): void {
    this.isLoading = true;
    RegistrationService.getDoctorSchedules(NavParams.doctorId).then((result: Result<DoctorSchedule[]>) => {
      if (result.success && result.data !== null) {
        this.schedules = result.data;
      }
      this.isLoading = false;
    }).catch(() => {
      this.isLoading = false;
    });
  }

  showBookingConfirm(schedule: DoctorSchedule): void {
    let message = '就诊日期: ' + schedule.workDate +
      '\n时间段: ' + schedule.timePeriod + ' ' + schedule.startTime + '-' + schedule.endTime +
      '\n费用: ¥' + schedule.registrationFee.toFixed(2);
    promptAction.showDialog({
      title: '确认预约',
      message: message,
      buttons: [
        { text: '取消', color: '#999999' },
        { text: '确认预约', color: '#007AFF' }
      ]
    }).then((data) => {
      if (data.index === 1) {
        this.doBooking(schedule);
      }
    });
  }

  doBooking(schedule: DoctorSchedule): void {
    if (AppState.userId === 0) {
      promptAction.showDialog({
        title: '预约失败',
        message: '请先登录',
        buttons: [
          { text: '确定', color: '#007AFF' }
        ]
      });
      return;
    }
    PatientService.getPatientInfo(AppState.userId)
      .then((infoResult: Result<Patient>) => {
        if (infoResult.success && infoResult.data !== null) {
          const patientId: number = infoResult.data.patientId;
          RegistrationService.bookAppointment(patientId, NavParams.doctorId, schedule.scheduleId, NavParams.deptId, schedule.registrationFee)
            .then((result: Result<string>) => {
              if (result.success) {
                promptAction.showToast({
                  message: '预约成功',
                  duration: 2000
                });
                router.back();
              } else {
                promptAction.showDialog({
                  title: '预约失败',
                  message: result.message !== '' ? result.message : '预约失败，请重试',
                  buttons: [
                    { text: '确定', color: '#007AFF' }
                  ]
                });
              }
            })
            .catch((error: BusinessError) => {
              promptAction.showDialog({
                title: '预约失败',
                message: error.message,
                buttons: [
                  { text: '确定', color: '#007AFF' }
                ]
              });
            });
        } else {
          promptAction.showDialog({
            title: '预约失败',
            message: '患者信息获取失败: ' + infoResult.message,
            buttons: [
              { text: '确定', color: '#007AFF' }
            ]
          });
        }
      })
      .catch((error: BusinessError) => {
        promptAction.showDialog({
          title: '预约失败',
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
        Text('选择时段')
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

      if (this.isLoading) {
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
      } else if (this.schedules.length === 0) {
        Column() {
          Text('暂无排班信息')
            .fontSize(16)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else {
        List() {
          ForEach(this.schedules, (schedule: DoctorSchedule) => {
            ListItem() {
              Column() {
                Row() {
                  Text(schedule.workDate)
                    .fontSize(16)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#333333')
                  Text(schedule.timePeriod)
                    .fontSize(12)
                    .fontColor('#007AFF')
                    .backgroundColor('#E8F4FD')
                    .borderRadius(4)
                    .padding({ left: 6, right: 6, top: 2, bottom: 2 })
                    .margin({ left: 8 })
                }
                .width('100%')

                Text(schedule.startTime + '-' + schedule.endTime)
                  .fontSize(14)
                  .fontColor('#666666')
                  .margin({ top: 8 })

                Row() {
                  Text('剩余: ' + schedule.remainQuota.toString() + '/' + schedule.totalQuota.toString())
                    .fontSize(13)
                    .fontColor('#999999')
                  Text('¥' + schedule.registrationFee.toFixed(2))
                    .fontSize(18)
                    .fontColor('#FF6B35')
                    .fontWeight(FontWeight.Bold)
                    .margin({ left: 16 })
                }
                .width('100%')
                .margin({ top: 8 })
                .justifyContent(FlexAlign.SpaceBetween)

                Button('预约')
                  .fontSize(14)
                  .fontColor('#FFFFFF')
                  .backgroundColor(schedule.remainQuota > 0 ? '#007AFF' : '#CCCCCC')
                  .borderRadius(20)
                  .height(36)
                  .width(80)
                  .margin({ top: 12 })
                  .enabled(schedule.remainQuota > 0)
                  .onClick(() => {
                    this.showBookingConfirm(schedule);
                  })
              }
              .width('100%')
              .padding(16)
              .alignItems(HorizontalAlign.Start)
            }
            .backgroundColor('#FFFFFF')
            .borderRadius(8)
            .margin({ left: 16, right: 16, top: 8, bottom: 4 })
          }, (schedule: DoctorSchedule) => schedule.scheduleId.toString())
        }
        .width('100%')
        .layoutWeight(1)
        .backgroundColor('#F5F5F5')
        .padding({ top: 4 })
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }
}
