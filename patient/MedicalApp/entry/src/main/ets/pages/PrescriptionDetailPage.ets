import { router } from '@kit.ArkUI';
import { NavParams } from './DepartmentsPage';
import { PrescriptionService } from '../services/PrescriptionService';
import { Result } from '../common/Result';
import { Prescription } from '../models/Prescription';
import { PrescriptionItem } from '../models/PrescriptionItem';

@Entry
@Component
struct PrescriptionDetailPage {
  @State prescription: Prescription | null = null;
  @State items: PrescriptionItem[] = [];
  @State isLoading: boolean = true;

  aboutToAppear(): void {
    this.loadDetail();
  }

  loadDetail(): void {
    let prescriptionId: number = NavParams.prescriptionId;
    let detailLoaded: boolean = false;
    let itemsLoaded: boolean = false;

    PrescriptionService.getPrescriptionDetail(prescriptionId)
      .then((result: Result<Prescription>) => {
        if (result.success && result.data !== null) {
          this.prescription = result.data;
        }
        detailLoaded = true;
        if (itemsLoaded) {
          this.isLoading = false;
        }
      })
      .catch(() => {
        detailLoaded = true;
        if (itemsLoaded) {
          this.isLoading = false;
        }
      });

    PrescriptionService.getPrescriptionItems(prescriptionId)
      .then((result: Result<PrescriptionItem[]>) => {
        if (result.success && result.data !== null) {
          this.items = result.data;
        }
        itemsLoaded = true;
        if (detailLoaded) {
          this.isLoading = false;
        }
      })
      .catch(() => {
        itemsLoaded = true;
        if (detailLoaded) {
          this.isLoading = false;
        }
      });
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

        Text('处方详情')
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
      } else if (this.prescription === null) {
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
            Column() {
              Row() {
                Text('处方号: ' + this.prescription.prescriptionNo)
                  .fontSize(16)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#333333')
                  .layoutWeight(1)

                Text(this.getAuditStatusText(this.prescription.auditStatus))
                  .fontSize(12)
                  .fontColor(this.getAuditStatusColor(this.prescription.auditStatus))
                  .backgroundColor(this.getAuditStatusBgColor(this.prescription.auditStatus))
                  .borderRadius(4)
                  .padding({ left: 8, right: 8, top: 2, bottom: 2 })
              }
              .width('100%')
              .alignItems(VerticalAlign.Center)

              if (this.prescription.diagnosis !== '') {
                Row() {
                  Text('诊断:')
                    .fontSize(13)
                    .fontColor('#999999')
                    .width(60)

                  Text(this.prescription.diagnosis)
                    .fontSize(13)
                    .fontColor('#333333')
                    .layoutWeight(1)
                }
                .width('100%')
                .margin({ top: 6 })
              }

              if (this.prescription.usageNote !== '') {
                Row() {
                  Text('用法:')
                    .fontSize(13)
                    .fontColor('#999999')
                    .width(60)

                  Text(this.prescription.usageNote)
                    .fontSize(13)
                    .fontColor('#333333')
                    .layoutWeight(1)
                }
                .width('100%')
                .margin({ top: 6 })
              }

              if (this.prescription.auditedAt !== '') {
                Row() {
                  Text('审核时间:')
                    .fontSize(13)
                    .fontColor('#999999')
                    .width(60)

                  Text(this.prescription.auditedAt)
                    .fontSize(13)
                    .fontColor('#333333')
                    .layoutWeight(1)
                }
                .width('100%')
                .margin({ top: 6 })
              }

              Divider()
                .strokeWidth(0.5)
                .color('#EEEEEE')
                .margin({ top: 10, bottom: 10 })

              Row() {
                Text('合计金额')
                  .fontSize(14)
                  .fontColor('#333333')

                Blank()

                Text('¥' + this.prescription.totalAmount.toString())
                  .fontSize(20)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#FF6B00')
              }
              .width('100%')
              .alignItems(VerticalAlign.Center)
            }
            .width('100%')
            .backgroundColor('#FFFFFF')
            .borderRadius(8)
            .padding(16)
            .margin({ left: 16, right: 16, top: 12 })

            Text('处方明细')
              .fontSize(15)
              .fontWeight(FontWeight.Bold)
              .fontColor('#007AFF')
              .width('100%')
              .padding({ left: 16, right: 16, top: 16, bottom: 8 })

            if (this.items.length === 0) {
              Column() {
                Text('暂无处方明细')
                  .fontSize(14)
                  .fontColor('#999999')
              }
              .width('100%')
              .backgroundColor('#FFFFFF')
              .borderRadius(8)
              .padding(16)
              .margin({ left: 16, right: 16 })
            } else {
              ForEach(this.items, (item: PrescriptionItem) => {
                Column() {
                  Row() {
                    Text(item.drugName)
                      .fontSize(15)
                      .fontWeight(FontWeight.Bold)
                      .fontColor('#333333')
                      .layoutWeight(1)

                    Text('¥' + item.amount.toString())
                      .fontSize(15)
                      .fontColor('#FF6B00')
                  }
                  .width('100%')
                  .alignItems(VerticalAlign.Center)

                  if (item.specification !== '') {
                    Text(item.specification)
                      .fontSize(12)
                      .fontColor('#999999')
                      .margin({ top: 4 })
                  }

                  Row() {
                    Text(item.dosage + ' × ' + item.frequency + ', ' + item.days.toString() + '天')
                      .fontSize(13)
                      .fontColor('#666666')

                    Text('¥' + item.unitPrice.toString() + ' × ' + item.quantity.toString())
                      .fontSize(13)
                      .fontColor('#999999')
                      .margin({ left: 16 })
                  }
                  .width('100%')
                  .margin({ top: 6 })
                }
                .width('100%')
                .padding({ top: 12, bottom: 12 })
                .alignItems(HorizontalAlign.Start)
                .backgroundColor('#FFFFFF')
                .borderRadius(8)
                .padding(16)
                .margin({ left: 16, right: 16, top: 4, bottom: 4 })
              }, (item: PrescriptionItem) => item.prescriptionItemId.toString())
            }

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
}
