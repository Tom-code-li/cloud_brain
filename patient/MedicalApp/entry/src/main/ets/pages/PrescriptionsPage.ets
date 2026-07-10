import { router } from '@kit.ArkUI';
import { AppState } from './LoginPage';
import { NavParams } from './DepartmentsPage';
import { PrescriptionService } from '../services/PrescriptionService';
import { PatientService } from '../services/PatientService';
import { Patient } from '../models/Patient';
import { Result } from '../common/Result';
import { Prescription } from '../models/Prescription';

@Entry
@Component
struct PrescriptionsPage {
  @State prescriptions: Prescription[] = [];
  @State isLoading: boolean = true;
  @State hasError: boolean = false;

  aboutToAppear(): void {
    this.loadPrescriptions();
  }

  loadPrescriptions(): void {
    this.hasError = false;
    if (AppState.userId === 0) {
      this.isLoading = false;
      return;
    }
    PatientService.getPatientInfo(AppState.userId)
      .then((infoResult: Result<Patient>) => {
        if (infoResult.success && infoResult.data !== null) {
          const patientId: number = infoResult.data.patientId;
          PrescriptionService.getMyPrescriptions(patientId)
            .then((result: Result<Prescription[]>) => {
              if (result.success && result.data !== null) {
                this.prescriptions = result.data;
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

  getFeeStatusText(feeStatus: string): string {
    if (feeStatus === '已支付') {
      return '已支付';
    }
    return '待支付';
  }

  getFeeStatusColor(feeStatus: string): string {
    if (feeStatus === '已支付') {
      return '#4CAF50';
    }
    return '#FF9800';
  }

  getFeeStatusBgColor(feeStatus: string): string {
    if (feeStatus === '已支付') {
      return '#E8F5E9';
    }
    return '#FFF3E0';
  }

  getAuditStatusText(auditStatus: string): string {
    if (auditStatus === '审核通过') {
      return '审核通过';
    } else if (auditStatus === '审核驳回') {
      return '审核驳回';
    }
    return '待审核';
  }

  getAuditStatusColor(auditStatus: string): string {
    if (auditStatus === '审核通过') {
      return '#4CAF50';
    } else if (auditStatus === '审核驳回') {
      return '#F44336';
    }
    return '#FBC02D';
  }

  getAuditStatusBgColor(auditStatus: string): string {
    if (auditStatus === '审核通过') {
      return '#E8F5E9';
    } else if (auditStatus === '审核驳回') {
      return '#FFEBEE';
    }
    return '#FFFDE7';
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

        Text('我的处方')
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

          Text('点击重试')
            .fontSize(14)
            .fontColor('#007AFF')
            .margin({ top: 8 })
            .onClick(() => {
              this.loadPrescriptions();
            })
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else if (this.prescriptions.length === 0) {
        Column() {
          Text('暂无处方')
            .fontSize(16)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else {
        List() {
          ForEach(this.prescriptions, (prescription: Prescription) => {
            ListItem() {
              Column() {
                Row() {
                  Text('处方号: ' + prescription.prescriptionNo)
                    .fontSize(14)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#333333')
                    .layoutWeight(1)
                    .maxLines(1)
                    .textOverflow({ overflow: TextOverflow.Ellipsis })

                  Text('¥' + prescription.totalAmount.toString())
                    .fontSize(16)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FF6B00')
                }
                .width('100%')
                .alignItems(VerticalAlign.Center)

                if (prescription.diagnosis !== '') {
                  Text(prescription.diagnosis)
                    .fontSize(13)
                    .fontColor('#999999')
                    .maxLines(1)
                    .textOverflow({ overflow: TextOverflow.Ellipsis })
                    .margin({ top: 6 })
                    .width('100%')
                }

                Row() {
                  Text(this.getFeeStatusText(prescription.feeStatus))
                    .fontSize(11)
                    .fontColor(this.getFeeStatusColor(prescription.feeStatus))
                    .backgroundColor(this.getFeeStatusBgColor(prescription.feeStatus))
                    .borderRadius(4)
                    .padding({ left: 6, right: 6, top: 2, bottom: 2 })

                  Text(this.getAuditStatusText(prescription.auditStatus))
                    .fontSize(11)
                    .fontColor(this.getAuditStatusColor(prescription.auditStatus))
                    .backgroundColor(this.getAuditStatusBgColor(prescription.auditStatus))
                    .borderRadius(4)
                    .padding({ left: 6, right: 6, top: 2, bottom: 2 })
                    .margin({ left: 8 })
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
              NavParams.prescriptionId = prescription.prescriptionId;
              router.pushUrl({ url: 'pages/PrescriptionDetailPage' });
            })
          }, (prescription: Prescription) => prescription.prescriptionId.toString())
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
