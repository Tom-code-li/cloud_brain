import { router } from '@kit.ArkUI';
import { Doctor } from '../models/Doctor';
import { RegistrationService } from '../services/RegistrationService';
import { Result } from '../common/Result';
import { NavParams } from './DepartmentsPage';

@Entry
@Component
struct DoctorsPage {
  @State doctors: Doctor[] = [];
  @State isLoading: boolean = true;

  aboutToAppear(): void {
    this.loadDoctors();
  }

  loadDoctors(): void {
    this.isLoading = true;
    RegistrationService.getDoctorsByDept(NavParams.deptId).then((result: Result<Doctor[]>) => {
      if (result.success && result.data !== null) {
        this.doctors = result.data;
      }
      this.isLoading = false;
    }).catch(() => {
      this.isLoading = false;
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
        Text(NavParams.deptName)
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
      } else if (this.doctors.length === 0) {
        Column() {
          Text('该科室暂无医生')
            .fontSize(16)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else {
        List() {
          ForEach(this.doctors, (doctor: Doctor) => {
            ListItem() {
              Column() {
                Row() {
                  Text(doctor.realName)
                    .fontSize(18)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#333333')
                  Text(doctor.title)
                    .fontSize(12)
                    .fontColor('#007AFF')
                    .backgroundColor('#E8F4FD')
                    .borderRadius(4)
                    .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                    .margin({ left: 8 })
                }
                .width('100%')

                Text(doctor.specialty)
                  .fontSize(14)
                  .fontColor('#666666')
                  .margin({ top: 8 })

                Row() {
                  if (doctor.appointmentStatus === '可预约') {
                    Text(doctor.appointmentStatus)
                      .fontSize(13)
                      .fontColor('#4CAF50')
                  } else {
                    Text(doctor.appointmentStatus)
                      .fontSize(13)
                      .fontColor('#999999')
                  }
                  Text('剩余号源: ' + doctor.availableQuota.toString())
                    .fontSize(13)
                    .fontColor('#999999')
                    .margin({ left: 16 })
                }
                .width('100%')
                .margin({ top: 8 })
              }
              .width('100%')
              .padding(16)
              .alignItems(HorizontalAlign.Start)
            }
            .backgroundColor('#FFFFFF')
            .borderRadius(8)
            .margin({ left: 16, right: 16, top: 8, bottom: 4 })
            .onClick(() => {
              NavParams.doctorId = doctor.doctorId;
              NavParams.doctorName = doctor.realName;
              router.pushUrl({ url: 'pages/SchedulePage' });
            })
          }, (doctor: Doctor) => doctor.doctorId.toString())
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
