import { router } from '@kit.ArkUI';
import { AppState } from './LoginPage';
import { AiConsultationService } from '../services/AiConsultationService';
import { PatientService } from '../services/PatientService';
import { Patient } from '../models/Patient';
import { Result } from '../common/Result';
import { AiConsultation } from '../models/AiConsultation';

@Entry
@Component
struct AiConsultHistoryPage {
  @State consultations: AiConsultation[] = [];
  @State isLoading: boolean = true;
  @State hasError: boolean = false;
  @State expandedId: number = -1;

  aboutToAppear(): void {
    this.loadConsultations();
  }

  loadConsultations(): void {
    this.isLoading = true;
    this.hasError = false;
    if (AppState.userId === 0) {
      this.isLoading = false;
      return;
    }
    PatientService.getPatientInfo(AppState.userId)
      .then((infoResult: Result<Patient>) => {
        if (infoResult.success && infoResult.data !== null) {
          const patientId: number = infoResult.data.patientId;
          AiConsultationService.getMyConsultations(patientId)
            .then((result: Result<AiConsultation[]>) => {
              this.isLoading = false;
              if (result.success && result.data !== null) {
                this.consultations = result.data;
              }
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

  getRiskColor(riskLevel: string): string {
    if (riskLevel === '紧急') {
      return '#FF3B30';
    }
    return '#34C759';
  }

  getStatusColor(status: string): string {
    if (status === '已采纳') {
      return '#34C759';
    }
    if (status === '已忽略') {
      return '#999999';
    }
    return '#007AFF';
  }

  toggleExpand(consultationId: number): void {
    if (this.expandedId === consultationId) {
      this.expandedId = -1;
    } else {
      this.expandedId = consultationId;
    }
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
        Text('导诊历史')
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
      } else if (this.hasError) {
        Column() {
          Text('加载失败')
            .fontSize(16)
            .fontColor('#FF3B30')
          Text('点击重试')
            .fontSize(14)
            .fontColor('#007AFF')
            .margin({ top: 8 })
            .onClick(() => {
              this.loadConsultations();
            })
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else if (this.consultations.length === 0) {
        Column() {
          Text('暂无导诊记录')
            .fontSize(16)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else {
        List() {
          ForEach(this.consultations, (consult: AiConsultation) => {
            ListItem() {
              Column() {
                Row() {
                  Text(consult.chiefComplaint)
                    .fontSize(16)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#333333')
                    .layoutWeight(1)

                  Text(consult.riskLevel)
                    .fontSize(12)
                    .fontColor('#FFFFFF')
                    .backgroundColor(this.getRiskColor(consult.riskLevel))
                    .borderRadius(4)
                    .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                }
                .width('100%')

                if (consult.recommendedDeptName !== '') {
                  Row() {
                    Text('推荐科室：')
                      .fontSize(13)
                      .fontColor('#999999')
                    Text(consult.recommendedDeptName)
                      .fontSize(13)
                      .fontColor('#333333')
                  }
                  .width('100%')
                  .margin({ top: 6 })
                }

                if (consult.aiSummary !== '') {
                  Text(consult.aiSummary)
                    .fontSize(13)
                    .fontColor('#666666')
                    .width('100%')
                    .maxLines(2)
                    .textOverflow({ overflow: TextOverflow.Ellipsis })
                    .margin({ top: 6 })
                }

                Row() {
                  Text(consult.status)
                    .fontSize(12)
                    .fontColor('#FFFFFF')
                    .backgroundColor(this.getStatusColor(consult.status))
                    .borderRadius(4)
                    .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                }
                .width('100%')
                .margin({ top: 8 })

                if (this.expandedId === consult.consultationId) {
                  Column() {
                    Divider()
                      .margin({ top: 12, bottom: 8 })

                    if (consult.symptomDetail !== '') {
                      Text('详细描述')
                        .fontSize(13)
                        .fontColor('#999999')
                        .width('100%')
                        .margin({ bottom: 4 })

                      Text(consult.symptomDetail)
                        .fontSize(13)
                        .fontColor('#333333')
                        .width('100%')
                        .lineHeight(20)
                        .margin({ bottom: 10 })
                    }

                    if (consult.aiSummary !== '') {
                      Text('AI摘要')
                        .fontSize(13)
                        .fontColor('#999999')
                        .width('100%')
                        .margin({ bottom: 4 })

                      Text(consult.aiSummary)
                        .fontSize(13)
                        .fontColor('#333333')
                        .width('100%')
                        .lineHeight(20)
                        .margin({ bottom: 10 })
                    }

                    if (consult.aiResult !== '') {
                      Text('AI分析结果')
                        .fontSize(13)
                        .fontColor('#999999')
                        .width('100%')
                        .margin({ bottom: 4 })

                      Text(consult.aiResult)
                        .fontSize(13)
                        .fontColor('#333333')
                        .width('100%')
                        .lineHeight(20)
                    }
                  }
                  .width('100%')
                }
              }
              .width('100%')
              .padding(16)
              .alignItems(HorizontalAlign.Start)
            }
            .backgroundColor('#FFFFFF')
            .borderRadius(8)
            .margin({ left: 16, right: 16, top: 8, bottom: 4 })
            .onClick(() => {
              this.toggleExpand(consult.consultationId);
            })
          }, (consult: AiConsultation) => consult.consultationId.toString())
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
