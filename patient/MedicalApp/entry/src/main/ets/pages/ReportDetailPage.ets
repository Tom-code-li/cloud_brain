import { router } from '@kit.ArkUI';
import { NavParams } from './DepartmentsPage';
import { ExamLabService } from '../services/ExamLabService';
import { Result } from '../common/Result';
import { ExamLabReport } from '../models/ExamLabReport';

@Entry
@Component
struct ReportDetailPage {
  @State report: ExamLabReport | null = null;
  @State isLoading: boolean = true;

  aboutToAppear(): void {
    this.loadReport();
  }

  loadReport(): void {
    this.isLoading = true;
    ExamLabService.getReportDetail(NavParams.reportId)
      .then((result: Result<ExamLabReport>) => {
        this.isLoading = false;
        if (result.success && result.data !== null) {
          this.report = result.data;
        }
      })
      .catch(() => {
        this.isLoading = false;
      });
  }

  getReportTypeColor(reportType: string): string {
    if (reportType === '检查') {
      return '#007AFF';
    }
    if (reportType === '检验') {
      return '#34C759';
    }
    return '#999999';
  }

  getFieldValue(value: string): string {
    if (value !== '') {
      return value;
    }
    return '暂无';
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
        Text('报告详情')
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
      } else if (this.report === null) {
        Column() {
          Text('暂无报告数据')
            .fontSize(16)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else {
        Scroll() {
          Column() {
            Row() {
              Text('报告编号')
                .fontSize(14)
                .fontColor('#999999')
                .width(80)
              Text(this.report.reportNo)
                .fontSize(16)
                .fontWeight(FontWeight.Bold)
                .fontColor('#333333')
                .layoutWeight(1)

              Text(this.report.reportType)
                .fontSize(12)
                .fontColor('#FFFFFF')
                .backgroundColor(this.getReportTypeColor(this.report.reportType))
                .borderRadius(4)
                .padding({ left: 8, right: 8, top: 2, bottom: 2 })
            }
            .width('100%')
            .alignItems(VerticalAlign.Center)
            .margin({ bottom: 16 })

            Divider()
              .margin({ bottom: 16 })

            Text('检查所见')
              .fontSize(16)
              .fontWeight(FontWeight.Bold)
              .fontColor('#333333')
              .margin({ bottom: 8 })
              .width('100%')

            Text(this.getFieldValue(this.report.findings))
              .fontSize(14)
              .fontColor('#666666')
              .width('100%')
              .lineHeight(22)
              .margin({ bottom: 20 })

            Text('诊断结论')
              .fontSize(16)
              .fontWeight(FontWeight.Bold)
              .fontColor('#333333')
              .margin({ bottom: 8 })
              .width('100%')

            Text(this.getFieldValue(this.report.conclusion))
              .fontSize(14)
              .fontColor('#333333')
              .fontWeight(FontWeight.Medium)
              .width('100%')
              .lineHeight(22)
              .padding(12)
              .backgroundColor('#FFF9E6')
              .borderRadius(8)
              .margin({ bottom: 20 })

            if (this.report.aiDraft !== '') {
              Column() {
                Text('AI初稿')
                  .fontSize(14)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#888888')
                  .margin({ bottom: 8 })
                  .width('100%')

                Text(this.report.aiDraft)
                  .fontSize(14)
                  .fontColor('#888888')
                  .width('100%')
                  .lineHeight(22)
              }
              .width('100%')
              .padding(12)
              .backgroundColor('#F5F5F5')
              .borderRadius(8)
              .margin({ bottom: 20 })
            }

            Text('医生审核意见')
              .fontSize(16)
              .fontWeight(FontWeight.Bold)
              .fontColor('#333333')
              .margin({ bottom: 8 })
              .width('100%')

            Text(this.getFieldValue(this.report.doctorReview))
              .fontSize(14)
              .fontColor('#333333')
              .fontWeight(FontWeight.Medium)
              .width('100%')
              .lineHeight(22)
              .margin({ bottom: 20 })

            Divider()
              .margin({ bottom: 12 })

            Row() {
              Text('发布状态')
                .fontSize(14)
                .fontColor('#999999')
              Text(this.getFieldValue(this.report.status))
                .fontSize(14)
                .fontColor('#333333')
                .margin({ left: 8 })
              Blank()
              Text('发布时间')
                .fontSize(14)
                .fontColor('#999999')
              Text(this.getFieldValue(this.report.publishedAt))
                .fontSize(14)
                .fontColor('#333333')
                .margin({ left: 8 })
            }
            .width('100%')
          }
          .width('100%')
          .padding(16)
          .alignItems(HorizontalAlign.Start)
        }
        .width('100%')
        .layoutWeight(1)
        .backgroundColor('#FFFFFF')
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#FFFFFF')
  }
}
