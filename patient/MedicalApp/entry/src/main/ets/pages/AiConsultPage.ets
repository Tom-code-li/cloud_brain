import { router } from '@kit.ArkUI';
import { AppState } from './LoginPage';
import { AiConsultationService } from '../services/AiConsultationService';
import { PatientService } from '../services/PatientService';
import { Patient } from '../models/Patient';
import { Result } from '../common/Result';
import { AiConsultation } from '../models/AiConsultation';
import { AiConsultResponse } from '../models/AiConsultResponse';

@Entry
@Component
struct AiConsultPage {
  @State chiefComplaint: string = '';
  @State symptomDetail: string = '';
  @State mode: string = 'basic';
  @State isLoading: boolean = false;
  @State hasResult: boolean = false;
  @State resultSummary: string = '';
  @State resultRiskLevel: string = '';
  @State resultDept: string = '';
  @State resultDoctor: string = '';
  @State resultText: string = '';
  @State resultNote: string = '';

  getRiskColor(riskLevel: string): string {
    if (riskLevel === '紧急') {
      return '#FF3B30';
    }
    return '#34C759';
  }

  handleSubmit(): void {
    if (this.chiefComplaint.trim() === '') {
      AlertDialog.show({
        title: '提示',
        message: '请输入主诉内容',
        confirm: {
          value: '确定',
          action: () => {}
        }
      });
      return;
    }

    if (AppState.userId === 0) {
      AlertDialog.show({
        title: '导诊失败',
        message: '请先登录',
        confirm: {
          value: '确定',
          action: () => {}
        }
      });
      return;
    }

    this.isLoading = true;
    this.hasResult = false;
    this.resultSummary = '';
    this.resultRiskLevel = '';
    this.resultDept = '';
    this.resultDoctor = '';
    this.resultText = '';
    this.resultNote = '';

    PatientService.getPatientInfo(AppState.userId)
      .then((infoResult: Result<Patient>) => {
        if (!infoResult.success || infoResult.data === null) {
          this.isLoading = false;
          AlertDialog.show({
            title: '导诊失败',
            message: '患者信息获取失败: ' + infoResult.message,
            confirm: {
              value: '确定',
              action: () => {}
            }
          });
          return;
        }
        const patientId: number = infoResult.data.patientId;
        this.doConsult(patientId);
      })
      .catch((err: Error) => {
        this.isLoading = false;
        AlertDialog.show({
          title: '导诊失败',
          message: '网络错误: ' + err.message,
          confirm: {
            value: '确定',
            action: () => {}
          }
        });
      });
  }

  doConsult(patientId: number): void {
    if (this.mode === 'basic') {
      AiConsultationService.consult(patientId, this.chiefComplaint, this.symptomDetail)
        .then((result: Result<AiConsultation>) => {
          this.isLoading = false;
          if (result.success && result.data !== null) {
            this.hasResult = true;
            this.resultSummary = result.data.aiSummary;
            this.resultRiskLevel = result.data.riskLevel;
            this.resultDept = result.data.recommendedDeptName;
            this.resultText = result.data.aiResult;
          } else {
            AlertDialog.show({
              title: '导诊失败',
              message: result.message !== '' ? result.message : '导诊失败，请重试',
              confirm: {
                value: '确定',
                action: () => {}
              }
            });
          }
        })
        .catch((err: Error) => {
          this.isLoading = false;
          AlertDialog.show({
            title: '导诊失败',
            message: '请求失败: ' + err.message,
            confirm: {
              value: '确定',
              action: () => {}
            }
          });
        });
    } else {
      let content: string = '主诉：' + this.chiefComplaint + '\n详细描述：' + this.symptomDetail;
      AiConsultationService.smartConsult(patientId, this.mode, content)
        .then((result: Result<AiConsultResponse>) => {
          this.isLoading = false;
          if (result.success && result.data !== null) {
            this.hasResult = true;
            this.resultSummary = result.data.summary;
            this.resultRiskLevel = result.data.riskLevel;
            this.resultDept = result.data.recommendedDept;
            this.resultDoctor = result.data.recommendedDoctor;
            this.resultText = result.data.aiResult;
            this.resultNote = result.data.note;
          } else {
            AlertDialog.show({
              title: '导诊失败',
              message: result.message !== '' ? result.message : '导诊失败，请重试',
              confirm: {
                value: '确定',
                action: () => {}
              }
            });
          }
        })
        .catch((err: Error) => {
          this.isLoading = false;
          AlertDialog.show({
            title: '导诊失败',
            message: '请求失败: ' + err.message,
            confirm: {
              value: '确定',
              action: () => {}
            }
          });
        });
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
        Text('AI智能导诊')
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

      Scroll() {
        Column() {
          Text('主诉')
            .fontSize(14)
            .fontColor('#333333')
            .fontWeight(FontWeight.Medium)
            .width('100%')
            .margin({ bottom: 8 })

          TextArea({ placeholder: '请输入主要症状，如：头疼、发烧', text: this.chiefComplaint })
            .height(80)
            .fontSize(14)
            .backgroundColor('#F5F5F5')
            .borderRadius(8)
            .padding(12)
            .width('100%')
            .margin({ bottom: 20 })
            .onChange((value: string) => {
              this.chiefComplaint = value;
            })

          Text('详细描述')
            .fontSize(14)
            .fontColor('#333333')
            .fontWeight(FontWeight.Medium)
            .width('100%')
            .margin({ bottom: 8 })

          TextArea({ placeholder: '请详细描述症状，如：持续时间、部位、程度等', text: this.symptomDetail })
            .height(100)
            .fontSize(14)
            .backgroundColor('#F5F5F5')
            .borderRadius(8)
            .padding(12)
            .width('100%')
            .margin({ bottom: 20 })
            .onChange((value: string) => {
              this.symptomDetail = value;
            })

          Text('导诊模式')
            .fontSize(14)
            .fontColor('#333333')
            .fontWeight(FontWeight.Medium)
            .width('100%')
            .margin({ bottom: 8 })

          Row() {
            Row() {
              Radio({ value: 'basic', group: 'aiMode' })
                .checked(this.mode === 'basic')
                .onChange((checked: boolean) => {
                  if (checked) {
                    this.mode = 'basic';
                  }
                })
              Text('基础导诊')
                .fontSize(14)
                .fontColor('#333333')
                .margin({ left: 4, right: 32 })
            }

            Row() {
              Radio({ value: 'smart', group: 'aiMode' })
                .checked(this.mode === 'smart')
                .onChange((checked: boolean) => {
                  if (checked) {
                    this.mode = 'smart';
                  }
                })
              Text('智能导诊')
                .fontSize(14)
                .fontColor('#333333')
                .margin({ left: 4 })
            }
          }
          .width('100%')
          .margin({ bottom: 24 })

          Button(this.isLoading ? '导诊中...' : '开始导诊')
            .width('100%')
            .height(48)
            .fontSize(16)
            .fontColor('#FFFFFF')
            .backgroundColor(this.isLoading ? '#80BFFF' : '#007AFF')
            .borderRadius(8)
            .enabled(!this.isLoading)
            .onClick(() => {
              this.handleSubmit();
            })

          if (this.hasResult) {
            Column() {
              Divider()
                .margin({ top: 24, bottom: 16 })

              Text('导诊结果')
                .fontSize(18)
                .fontWeight(FontWeight.Bold)
                .fontColor('#333333')
                .width('100%')
                .margin({ bottom: 12 })

              Row() {
                Text('风险等级')
                  .fontSize(14)
                  .fontColor('#999999')
                  .margin({ right: 8 })
                Text(this.resultRiskLevel)
                  .fontSize(14)
                  .fontColor('#FFFFFF')
                  .backgroundColor(this.getRiskColor(this.resultRiskLevel))
                  .borderRadius(4)
                  .padding({ left: 8, right: 8, top: 2, bottom: 2 })
              }
              .width('100%')
              .margin({ bottom: 8 })

              if (this.resultDept !== '') {
                Row() {
                  Text('推荐科室')
                    .fontSize(14)
                    .fontColor('#999999')
                    .margin({ right: 8 })
                  Text(this.resultDept)
                    .fontSize(14)
                    .fontColor('#333333')
                    .fontWeight(FontWeight.Medium)
                }
                .width('100%')
                .margin({ bottom: 8 })
              }

              if (this.resultDoctor !== '') {
                Row() {
                  Text('推荐医生')
                    .fontSize(14)
                    .fontColor('#999999')
                    .margin({ right: 8 })
                  Text(this.resultDoctor)
                    .fontSize(14)
                    .fontColor('#333333')
                    .fontWeight(FontWeight.Medium)
                }
                .width('100%')
                .margin({ bottom: 8 })
              }

              if (this.resultSummary !== '') {
                Text('AI摘要')
                  .fontSize(14)
                  .fontColor('#999999')
                  .width('100%')
                  .margin({ bottom: 4 })

                Text(this.resultSummary)
                  .fontSize(14)
                  .fontColor('#333333')
                  .width('100%')
                  .lineHeight(22)
                  .padding(12)
                  .backgroundColor('#F5F5F5')
                  .borderRadius(8)
                  .margin({ bottom: 12 })
              }

              if (this.resultText !== '') {
                Text('详细分析')
                  .fontSize(14)
                  .fontColor('#999999')
                  .width('100%')
                  .margin({ bottom: 4 })

                Text(this.resultText)
                  .fontSize(14)
                  .fontColor('#666666')
                  .width('100%')
                  .lineHeight(22)
                  .padding(12)
                  .backgroundColor('#F5F5F5')
                  .borderRadius(8)
                  .margin({ bottom: 12 })
              }

              if (this.resultNote !== '') {
                Text('注意事项')
                  .fontSize(14)
                  .fontColor('#999999')
                  .width('100%')
                  .margin({ bottom: 4 })

                Text(this.resultNote)
                  .fontSize(14)
                  .fontColor('#999999')
                  .width('100%')
                  .lineHeight(22)
                  .margin({ bottom: 12 })
              }
            }
            .width('100%')
            .padding(16)
            .backgroundColor('#FAFAFA')
            .borderRadius(8)
            .margin({ top: 8 })
            .alignItems(HorizontalAlign.Start)
          }

          Button() {
            Text('查看历史导诊记录')
              .fontSize(14)
              .fontColor('#007AFF')
          }
          .width('100%')
          .height(44)
          .fontSize(14)
          .backgroundColor('#FFFFFF')
          .borderRadius(8)
          .margin({ top: 20 })
          .onClick(() => {
            router.pushUrl({ url: 'pages/AiConsultHistoryPage' });
          })
        }
        .width('100%')
        .padding(16)
        .alignItems(HorizontalAlign.Start)
      }
      .width('100%')
      .layoutWeight(1)
      .backgroundColor('#FFFFFF')
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }
}
