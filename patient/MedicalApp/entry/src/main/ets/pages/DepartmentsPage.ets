import { router } from '@kit.ArkUI';
import { Department } from '../models/Department';
import { RegistrationService } from '../services/RegistrationService';
import { Result } from '../common/Result';

export class NavParams {
  static deptId: number = 0;
  static deptName: string = '';
  static doctorId: number = 0;
  static doctorName: string = '';
  static recordId: number = 0;
  static prescriptionId: number = 0;
  static drugId: number = 0;
  static reportId: number = 0;
}

@Entry
@Component
struct DepartmentsPage {
  @State departments: Department[] = [];
  @State isLoading: boolean = true;

  aboutToAppear(): void {
    this.loadDepartments();
  }

  loadDepartments(): void {
    this.isLoading = true;
    RegistrationService.getAllDepartments().then((result: Result<Department[]>) => {
      if (result.success && result.data !== null) {
        this.departments = result.data;
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
        Text('选择科室')
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
      } else if (this.departments.length === 0) {
        Column() {
          Text('暂无科室信息')
            .fontSize(16)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else {
        List() {
          ForEach(this.departments, (dept: Department) => {
            ListItem() {
              Column() {
                Text(dept.deptName)
                  .fontSize(18)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#333333')
                Text(dept.deptType)
                  .fontSize(13)
                  .fontColor('#999999')
                  .margin({ top: 4 })
                Row() {
                  Text(dept.location)
                    .fontSize(13)
                    .fontColor('#999999')
                  Text(dept.phone)
                    .fontSize(13)
                    .fontColor('#999999')
                    .margin({ left: 16 })
                }
                .width('100%')
                .margin({ top: 4 })
              }
              .width('100%')
              .padding(16)
              .alignItems(HorizontalAlign.Start)
            }
            .backgroundColor('#FFFFFF')
            .borderRadius(8)
            .margin({ left: 16, right: 16, top: 8, bottom: 4 })
            .onClick(() => {
              NavParams.deptId = dept.deptId;
              NavParams.deptName = dept.deptName;
              router.pushUrl({ url: 'pages/DoctorsPage' });
            })
          }, (dept: Department) => dept.deptId.toString())
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
