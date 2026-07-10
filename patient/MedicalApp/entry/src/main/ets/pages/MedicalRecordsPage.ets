import { router } from '@kit.ArkUI';
import { AppState } from './LoginPage';
import { NavParams } from './DepartmentsPage';
import { MedicalRecordService } from '../services/MedicalRecordService';
import { PatientService } from '../services/PatientService';
import { Patient } from '../models/Patient';
import { Result } from '../common/Result';
import { MedicalRecord } from '../models/MedicalRecord';

@Entry
@Component
struct MedicalRecordsPage {
  @State records: MedicalRecord[] = [];
  @State isLoading: boolean = true;
  @State isRefreshing: boolean = false;
  @State hasError: boolean = false;

  aboutToAppear(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.hasError = false;
    if (AppState.userId === 0) {
      this.isLoading = false;
      return;
    }
    PatientService.getPatientInfo(AppState.userId)
      .then((infoResult: Result<Patient>) => {
        if (infoResult.success && infoResult.data !== null) {
          const patientId: number = infoResult.data.patientId;
          MedicalRecordService.getMyRecords(patientId)
            .then((result: Result<MedicalRecord[]>) => {
              if (result.success && result.data !== null) {
                this.records = result.data;
              }
              this.isLoading = false;
            })
            .catch(() => {
              this.isLoading = false;
              this.hasError = true;
            });
        } else {
          this.isLoading = false;
          this.hasError = true;
        }
      })
      .catch(() => {
        this.isLoading = false;
        this.hasError = true;
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
          MedicalRecordService.getMyRecords(patientId)
            .then((result: Result<MedicalRecord[]>) => {
              if (result.success && result.data !== null) {
                this.records = result.data;
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

  getStatusText(status: string): string {
    if (status === 'completed') {
      return '已完成';
    }
    return '进行中';
  }

  getStatusColor(status: string): string {
    if (status === 'completed') {
      return '#4CAF50';
    }
    return '#2196F3';
  }

  getStatusBgColor(status: string): string {
    if (status === 'completed') {
      return '#E8F5E9';
    }
    return '#E3F2FD';
  }

  getDisplayDate(record: MedicalRecord): string {
    if (record.completedAt !== '') {
      return record.completedAt;
    }
    return record.initialSavedAt;
  }

  build() {
    Column() {
      Row() {
        Text('←')
          .fontSize(24)
          .fontColor('#007AFF')
          .width(24)
          .onClick(() => {
            router.back();
          })

        Text('就诊记录')
          .fontSize(20)
          .fontWeight(FontWeight.Bold)
          .fontColor('#333333')
          .layoutWeight(1)
          .textAlign(TextAlign.Center)

        Blank()
          .width(24)
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
      } else if (this.hasError) {
        Column() {
          Text('加载失败')
            .fontSize(16)
            .fontColor('#999999')

          Text('下拉刷新重试')
            .fontSize(14)
            .fontColor('#007AFF')
            .margin({ top: 8 })
            .onClick(() => {
              this.loadRecords();
            })
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else if (this.records.length === 0) {
        Column() {
          Text('暂无就诊记录')
            .fontSize(16)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else {
        Refresh({ refreshing: this.isRefreshing }) {
          List() {
            ForEach(this.records, (record: MedicalRecord) => {
              ListItem() {
                Column() {
                  Row() {
                    Text(record.diagnosis !== '' ? record.diagnosis : '未诊断')
                      .fontSize(16)
                      .fontWeight(FontWeight.Bold)
                      .fontColor('#333333')
                      .layoutWeight(1)
                      .maxLines(1)
                      .textOverflow({ overflow: TextOverflow.Ellipsis })

                    Text(this.getStatusText(record.status))
                      .fontSize(12)
                      .fontColor(this.getStatusColor(record.status))
                      .backgroundColor(this.getStatusBgColor(record.status))
                      .borderRadius(4)
                      .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                  }
                  .width('100%')
                  .alignItems(VerticalAlign.Center)

                  if (record.chiefComplaint !== '') {
                    Text(record.chiefComplaint)
                      .fontSize(13)
                      .fontColor('#999999')
                      .maxLines(1)
                      .textOverflow({ overflow: TextOverflow.Ellipsis })
                      .margin({ top: 8 })
                      .width('100%')
                  }

                  Text(this.getDisplayDate(record))
                    .fontSize(12)
                    .fontColor('#CCCCCC')
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
                NavParams.recordId = record.recordId;
                router.pushUrl({ url: 'pages/RecordDetailPage' });
              })
            }, (record: MedicalRecord) => record.recordId.toString())
          }
          .width('100%')
          .layoutWeight(1)
          .backgroundColor('#F5F5F5')
          .padding({ top: 4 })
        }
        .onRefreshing(() => {
          this.handleRefresh();
        })
        .layoutWeight(1)
        .width('100%')
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }
}
