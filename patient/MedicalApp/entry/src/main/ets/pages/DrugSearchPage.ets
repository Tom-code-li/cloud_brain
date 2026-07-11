import { router } from '@kit.ArkUI';
import { NavParams } from './DepartmentsPage';
import { DrugService } from '../services/DrugService';
import { Result } from '../common/Result';
import { Drug } from '../models/Drug';

@Entry
@Component
struct DrugSearchPage {
  @State keyword: string = '';
  @State drugs: Drug[] = [];
  @State isLoading: boolean = false;
  @State hasSearched: boolean = false;

  handleSearch(): void {
    if (this.keyword.trim() === '') {
      return;
    }
    this.isLoading = true;
    this.hasSearched = true;
    DrugService.searchDrugs(this.keyword.trim())
      .then((result: Result<Drug[]>) => {
        if (result.success && result.data !== null) {
          this.drugs = result.data;
        } else {
          this.drugs = [];
        }
        this.isLoading = false;
      })
      .catch(() => {
        this.isLoading = false;
        this.drugs = [];
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

        Text('药品查询')
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

      Row() {
        TextInput({ placeholder: '请输入药品名称', text: this.keyword })
          .fontSize(15)
          .backgroundColor('#F5F5F5')
          .borderRadius(8)
          .height(42)
          .layoutWeight(1)
          .padding({ left: 12, right: 12 })
          .onChange((value: string) => {
            this.keyword = value;
          })
          .onSubmit(() => {
            this.handleSearch();
          })

        Button('搜索')
          .fontSize(14)
          .fontColor('#FFFFFF')
          .backgroundColor('#007AFF')
          .borderRadius(8)
          .height(42)
          .width(60)
          .margin({ left: 10 })
          .onClick(() => {
            this.handleSearch();
          })
      }
      .width('100%')
      .padding({ left: 16, right: 16, top: 12, bottom: 12 })
      .backgroundColor('#FFFFFF')

      if (this.isLoading) {
        Column() {
          LoadingProgress()
            .width(40)
            .height(40)
            .color('#007AFF')

          Text('搜索中...')
            .fontSize(14)
            .fontColor('#999999')
            .margin({ top: 8 })
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else if (!this.hasSearched) {
        Column() {
          Text('请输入药品名称进行搜索')
            .fontSize(14)
            .fontColor('#CCCCCC')
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else if (this.drugs.length === 0) {
        Column() {
          Text('未找到相关药品')
            .fontSize(16)
            .fontColor('#999999')
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
      } else {
        List() {
          ForEach(this.drugs, (drug: Drug) => {
            ListItem() {
              Column() {
                Row() {
                  Text(drug.drugName)
                    .fontSize(16)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#333333')
                    .layoutWeight(1)
                    .maxLines(1)
                    .textOverflow({ overflow: TextOverflow.Ellipsis })

                  Text('¥' + drug.salePrice.toString())
                    .fontSize(16)
                    .fontWeight(FontWeight.Bold)
                    .fontColor('#FF6B00')
                }
                .width('100%')
                .alignItems(VerticalAlign.Center)

                Row() {
                  Text('编码: ' + drug.drugCode)
                    .fontSize(12)
                    .fontColor('#999999')

                  if (drug.specification !== '') {
                    Text(drug.specification)
                      .fontSize(12)
                      .fontColor('#999999')
                      .margin({ left: 12 })
                  }
                }
                .width('100%')
                .margin({ top: 6 })

                Row() {
                  if (drug.dosageForm !== '') {
                    Text(drug.dosageForm)
                      .fontSize(12)
                      .fontColor('#999999')
                  }

                  if (drug.manufacturer !== '') {
                    Text(drug.manufacturer)
                      .fontSize(12)
                      .fontColor('#999999')
                      .margin({ left: 12 })
                  }
                }
                .width('100%')
                .margin({ top: 4 })

                Text('库存: ' + drug.stockQuantity.toString())
                  .fontSize(12)
                  .fontColor('#CCCCCC')
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
              NavParams.drugId = drug.drugId;
              router.pushUrl({ url: 'pages/DrugDetailPage' });
            })
          }, (drug: Drug) => drug.drugId.toString())
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
