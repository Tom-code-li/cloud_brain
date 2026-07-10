import { router } from '@kit.ArkUI';
import { NavParams } from './DepartmentsPage';
import { MedicalRecordService } from '../services/MedicalRecordService';
import { Result } from '../common/Result';
import { MedicalRecord } from '../models/MedicalRecord';

interface SectionItem {
  label: string;
  value: string;
}

@Entry
@Component
struct RecordDetailPage {
  @State record: MedicalRecord | null = null;
  @State isLoading: boolean = true;

  aboutToAppear(): void {
    this.loadDetail();
  }

  loadDetail(): void {
    MedicalRecordService.getRecordDetail(NavParams.recordId)
      .then((result: Result<MedicalRecord>) => {
        if (result.success && result.data !== null) {
          this.record = result.data;
        }
        this.isLoading = false;
      })
      .catch(() => {
        this.isLoading = false;
      });
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

        Text('就诊详情')
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
      } else if (this.record === null) {
        Column() {
          Text('加载失败')
            .fontSize(16)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else {
        Scroll() {
          Column() {
            this.sectionBlock('基本信息', [
              { label: '诊断', value: this.record.diagnosis },
              { label: '主诉', value: this.record.chiefComplaint }
            ])

            this.sectionBlock('主诉与现病史', [
              { label: '现病史', value: this.record.presentIllness },
              { label: '既往史', value: this.record.pastHistory },
              { label: '过敏史', value: this.record.allergyHistory }
            ])

            this.sectionBlock('检查', [
              { label: '体格检查', value: this.record.physicalExam },
              { label: '辅助检查', value: this.record.auxiliaryExam }
            ])

            this.sectionBlock('治疗', [
              { label: '当前治疗', value: this.record.currentTreatment },
              { label: '治疗建议', value: this.record.treatmentAdvice }
            ])

            this.sectionBlock('诊断', [
              { label: '最终诊断', value: this.record.finalDiagnosis },
              { label: '最终意见', value: this.record.finalOpinion }
            ])

            this.sectionBlock('医生', [
              { label: '医生备注', value: this.record.doctorNote }
            ])

            this.sectionBlock('日期', [
              { label: '完成时间', value: this.record.completedAt }
            ])

            Column()
              .height(24)
              .width('100%')
          }
          .width('100%')
          .alignItems(HorizontalAlign.Center)
        }
        .width('100%')
        .layoutWeight(1)
        .scrollable(ScrollDirection.Vertical)
        .backgroundColor('#F5F5F5')
      }
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }

  @Builder
  sectionBlock(title: string, fields: SectionItem[]) {
    Column() {
      Text(title)
        .fontSize(15)
        .fontWeight(FontWeight.Bold)
        .fontColor('#007AFF')
        .width('100%')
        .padding({ left: 16, right: 16, top: 12, bottom: 8 })

      Column() {
        ForEach(fields, (field: SectionItem) => {
          Column() {
            Text(field.label)
              .fontSize(13)
              .fontColor('#999999')
              .margin({ bottom: 4 })

            Text(field.value !== '' ? field.value : '无')
              .fontSize(15)
              .fontColor('#333333')
              .width('100%')
          }
          .width('100%')
          .padding({ top: 10, bottom: 10 })
          .alignItems(HorizontalAlign.Start)

          Divider()
            .strokeWidth(0.5)
            .color('#EEEEEE')
        }, (item: SectionItem) => {
          return item.label + '_' + item.value;
        })
      }
      .width('100%')
      .padding({ left: 16, right: 16, bottom: 8 })
    }
    .width('100%')
    .backgroundColor('#FFFFFF')
    .borderRadius(8)
    .margin({ left: 16, right: 16, top: 12 })
    .alignItems(HorizontalAlign.Start)
  }
}
