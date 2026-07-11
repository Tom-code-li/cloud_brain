import { router } from '@kit.ArkUI';
import { AppState } from './LoginPage';
import { PatientService } from '../services/PatientService';
import { Result } from '../common/Result';
import { Patient } from '../models/Patient';

interface FeatureItem {
  id: string;
  title: string;
  icon: string;
  page: string;
}

@Entry
@Component
struct HomePage {
  @State features: FeatureItem[] = [
    { id: '1', title: '预约挂号', icon: '📅', page: 'pages/DepartmentsPage' },
    { id: '2', title: '我的挂号', icon: '📋', page: 'pages/MyRegistrationsPage' },
    { id: '3', title: '就诊记录', icon: '📝', page: 'pages/MedicalRecordsPage' },
    { id: '4', title: '我的处方', icon: '💊', page: 'pages/PrescriptionsPage' },
    { id: '5', title: '药品查询', icon: '🔍', page: 'pages/DrugSearchPage' },
    { id: '6', title: '检查检验', icon: '🔬', page: 'pages/ExamOrdersPage' },
    { id: '7', title: '费用管理', icon: '💰', page: 'pages/FeeOrdersPage' },
    { id: '8', title: 'AI导诊', icon: '🤖', page: 'pages/AiConsultPage' }
  ];

  @State currentTabIndex: number = 0;
  @State patientLoaded: boolean = false;
  @State patientName: string = '';
  @State patientNo: string = '';
  @State patientPhone: string = '';
  @State patientGender: string = '';
  @State patientBirthday: string = '';

  aboutToAppear(): void {
    this.loadPatientInfo();
  }

  loadPatientInfo(): void {
    if (AppState.userId === 0) {
      return;
    }

    PatientService.getPatientInfo(AppState.userId)
      .then((result: Result<Patient>) => {
        if (result.success && result.data !== null) {
          let patient: Patient = result.data;
          AppState.patientId = patient.patientId;
          AppState.patientName = patient.patientName;
          this.patientName = patient.patientName;
          this.patientNo = patient.patientNo;
          this.patientPhone = patient.phone;
          this.patientBirthday = patient.birthday;
          if (patient.gender === '1') {
            this.patientGender = '男';
          } else if (patient.gender === '2') {
            this.patientGender = '女';
          } else {
            this.patientGender = '未知';
          }
        } else {
          this.patientName = AppState.patientName;
        }
        this.patientLoaded = true;
      })
      .catch(() => {
        this.patientName = AppState.patientName;
        this.patientLoaded = true;
      });
  }

  build() {
    Column() {
      Row() {
        Text('医疗助手')
          .fontSize(20)
          .fontColor('#007AFF')
          .fontWeight(FontWeight.Bold)

        Blank()

        Text(AppState.username)
          .fontSize(14)
          .fontColor('#666666')
      }
      .width('100%')
      .height(56)
      .padding({ left: 16, right: 16 })
      .backgroundColor('#FFFFFF')

      Tabs({ barPosition: BarPosition.End, index: this.currentTabIndex }) {
        TabContent() {
          Scroll() {
            Grid() {
              ForEach(this.features, (item: FeatureItem) => {
                GridItem() {
                  Column() {
                    Text(item.icon)
                      .fontSize(36)

                    Text(item.title)
                      .fontSize(14)
                      .fontColor('#333333')
                      .margin({ top: 8 })
                  }
                  .width('100%')
                  .height('100%')
                  .justifyContent(FlexAlign.Center)
                  .alignItems(HorizontalAlign.Center)
                  .backgroundColor('#FAFAFA')
                  .borderRadius(12)
                  .onClick(() => {
                    router.pushUrl({ url: item.page });
                  })
                }
                .height(100)
              }, (item: FeatureItem) => item.id)
            }
            .columnsTemplate('1fr 1fr')
            .rowsGap(12)
            .columnsGap(12)
            .padding({ left: 16, right: 16, top: 16, bottom: 16 })
          }
          .width('100%')
          .height('100%')
          .scrollable(ScrollDirection.Vertical)
        }
        .tabBar('首页')

        TabContent() {
          Column() {
            Text('挂号服务')
              .fontSize(22)
              .fontWeight(FontWeight.Bold)
              .fontColor('#333333')
              .margin({ top: 32, bottom: 32 })

            Row() {
              Column() {
                Text('📅')
                  .fontSize(36)

                Text('科室列表')
                  .fontSize(14)
                  .fontColor('#333333')
                  .margin({ top: 8 })
              }
              .width('44%')
              .height(120)
              .justifyContent(FlexAlign.Center)
              .alignItems(HorizontalAlign.Center)
              .backgroundColor('#FAFAFA')
              .borderRadius(12)
              .onClick(() => {
                router.pushUrl({ url: 'pages/DepartmentsPage' });
              })

              Blank()

              Column() {
                Text('📋')
                  .fontSize(36)

                Text('我的挂号')
                  .fontSize(14)
                  .fontColor('#333333')
                  .margin({ top: 8 })
              }
              .width('44%')
              .height(120)
              .justifyContent(FlexAlign.Center)
              .alignItems(HorizontalAlign.Center)
              .backgroundColor('#FAFAFA')
              .borderRadius(12)
              .onClick(() => {
                router.pushUrl({ url: 'pages/MyRegistrationsPage' });
              })
            }
            .width('100%')
            .justifyContent(FlexAlign.Center)
          }
          .width('100%')
          .height('100%')
          .alignItems(HorizontalAlign.Center)
          .backgroundColor('#FFFFFF')
        }
        .tabBar('挂号')

        TabContent() {
          Scroll() {
            Column() {
              Row() {
                Blank()
                  .width(60)
                  .height(60)
                  .borderRadius(30)
                  .backgroundColor('#007AFF')
                  .margin({ top: 24, bottom: 12 })

                Blank()
              }
              .width('100%')
              .justifyContent(FlexAlign.Center)

              Text(this.patientName !== '' ? this.patientName : '未设置姓名')
                .fontSize(20)
                .fontWeight(FontWeight.Bold)
                .fontColor('#333333')
                .margin({ bottom: 24 })

              Column() {
                this.profileItem('姓名', this.patientName)
                this.profileItem('就诊号', this.patientNo)
                this.profileItem('手机号', this.patientPhone)
                this.profileItem('性别', this.patientGender)
                this.profileItem('生日', this.patientBirthday)
              }
              .width('90%')
              .backgroundColor('#FAFAFA')
              .borderRadius(12)
              .padding({ top: 16, bottom: 16, left: 16, right: 16 })
              .margin({ bottom: 32 })

              Button('退出登录')
                .width('90%')
                .height(48)
                .fontSize(16)
                .fontColor('#FF3B30')
                .backgroundColor('#FFFFFF')
                .borderRadius(8)
                .border({ width: 1, color: '#FF3B30' })
                .onClick(() => {
                  this.handleLogout();
                })
            }
            .width('100%')
            .alignItems(HorizontalAlign.Center)
          }
          .width('100%')
          .height('100%')
          .scrollable(ScrollDirection.Vertical)
          .backgroundColor('#FFFFFF')
        }
        .tabBar('我的')
      }
      .onChange((index: number) => {
        this.currentTabIndex = index;
      })
      .layoutWeight(1)
      .barHeight(56)
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F0F0F0')
  }

  @Builder
  profileItem(label: string, value: string) {
    Row() {
      Text(label)
        .fontSize(14)
        .fontColor('#999999')
        .width(80)

      Text(value !== '' ? value : '未设置')
        .fontSize(14)
        .fontColor('#333333')
        .layoutWeight(1)
        .textAlign(TextAlign.End)
    }
    .width('100%')
    .height(40)
    .alignItems(VerticalAlign.Center)
  }

  handleLogout(): void {
    AlertDialog.show({
      title: '退出登录',
      message: '确定要退出登录吗？',
      primaryButton: {
        value: '确定',
        action: () => {
          AppState.userId = 0;
          AppState.username = '';
          AppState.patientId = 0;
          AppState.patientName = '';
          router.replaceUrl({ url: 'pages/LoginPage' });
        }
      },
      secondaryButton: {
        value: '取消',
        action: () => {}
      }
    });
  }
}
