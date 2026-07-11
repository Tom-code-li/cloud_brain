import { router } from '@kit.ArkUI';
import { NavParams } from './DepartmentsPage';
import { DrugService } from '../services/DrugService';
import { Result } from '../common/Result';
import { Drug } from '../models/Drug';

@Entry
@Component
struct DrugDetailPage {
  @State drug: Drug | null = null;
  @State isLoading: boolean = true;

  aboutToAppear(): void {
    this.loadDrug();
  }

  loadDrug(): void {
    this.isLoading = true;
    DrugService.getDrugDetail(NavParams.drugId)
      .then((result: Result<Drug>) => {
        this.isLoading = false;
        if (result.success && result.data !== null) {
          this.drug = result.data;
        }
      })
      .catch(() => {
        this.isLoading = false;
      });
  }

  getFieldValue(value: string): string {
    if (value !== '') {
      return value;
    }
    return '暂无';
  }

  getStatusLabel(status: string): string {
    if (status !== '') {
      return status;
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
        Text('药品详情')
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
      } else if (this.drug === null) {
        Column() {
          Text('暂无药品数据')
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
              Text(this.drug.drugName)
                .fontSize(22)
                .fontWeight(FontWeight.Bold)
                .fontColor('#333333')
                .layoutWeight(1)
                .maxLines(2)
                .textOverflow({ overflow: TextOverflow.Ellipsis })

              Text('¥' + this.drug.salePrice.toString())
                .fontSize(22)
                .fontWeight(FontWeight.Bold)
                .fontColor('#FF3B30')
            }
            .width('100%')
            .alignItems(VerticalAlign.Center)
            .margin({ bottom: 4 })

            Text(this.drug.drugCode)
              .fontSize(13)
              .fontColor('#AAAAAA')
              .width('100%')
              .margin({ bottom: 16 })

            Divider()
              .margin({ bottom: 16 })

            Column() {
              Row() {
                Text('规格')
                  .fontSize(14)
                  .fontColor('#999999')
                  .width(80)
                Text(this.getFieldValue(this.drug.specification))
                  .fontSize(14)
                  .fontColor('#333333')
                  .layoutWeight(1)
              }
              .width('100%')
              .margin({ bottom: 12 })

              Row() {
                Text('剂型')
                  .fontSize(14)
                  .fontColor('#999999')
                  .width(80)
                Text(this.getFieldValue(this.drug.dosageForm))
                  .fontSize(14)
                  .fontColor('#333333')
                  .layoutWeight(1)
              }
              .width('100%')
              .margin({ bottom: 12 })

              Row() {
                Text('生产厂家')
                  .fontSize(14)
                  .fontColor('#999999')
                  .width(80)
                Text(this.getFieldValue(this.drug.manufacturer))
                  .fontSize(14)
                  .fontColor('#333333')
                  .layoutWeight(1)
              }
              .width('100%')
              .margin({ bottom: 12 })

              Row() {
                Text('单位')
                  .fontSize(14)
                  .fontColor('#999999')
                  .width(80)
                Text(this.getFieldValue(this.drug.unit))
                  .fontSize(14)
                  .fontColor('#333333')
                  .layoutWeight(1)
              }
              .width('100%')
              .margin({ bottom: 12 })

              Row() {
                Text('销售价格')
                  .fontSize(14)
                  .fontColor('#999999')
                  .width(80)
                Text('¥' + this.drug.salePrice.toString())
                  .fontSize(16)
                  .fontWeight(FontWeight.Bold)
                  .fontColor('#FF3B30')
                  .layoutWeight(1)
              }
              .width('100%')
              .margin({ bottom: 12 })

              Row() {
                Text('库存数量')
                  .fontSize(14)
                  .fontColor('#999999')
                  .width(80)
                Text(this.drug.stockQuantity.toString())
                  .fontSize(14)
                  .fontColor('#333333')
                  .layoutWeight(1)
              }
              .width('100%')
              .margin({ bottom: 12 })

              Row() {
                Text('状态')
                  .fontSize(14)
                  .fontColor('#999999')
                  .width(80)
                Text(this.getStatusLabel(this.drug.status))
                  .fontSize(14)
                  .fontColor('#333333')
                  .layoutWeight(1)
              }
              .width('100%')
            }
            .width('100%')
            .padding(16)
            .backgroundColor('#FAFAFA')
            .borderRadius(8)
            .margin({ bottom: 16 })

            if (this.drug.contraindication !== '') {
              Text('禁忌症')
                .fontSize(16)
                .fontWeight(FontWeight.Bold)
                .fontColor('#333333')
                .width('100%')
                .margin({ bottom: 8 })

              Text(this.drug.contraindication)
                .fontSize(14)
                .fontColor('#666666')
                .width('100%')
                .lineHeight(22)
                .padding(12)
                .backgroundColor('#FFF5F5')
                .borderRadius(8)
            }
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
