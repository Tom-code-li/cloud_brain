import { router } from '@kit.ArkUI';
import { AppState } from './LoginPage';
import { FeeService } from '../services/FeeService';
import { RegistrationService } from '../services/RegistrationService';
import { PatientService } from '../services/PatientService';
import { Patient } from '../models/Patient';
import { Registration } from '../models/Registration';
import { Result } from '../common/Result';
import { FeeOrder } from '../models/FeeOrder';

@Entry
@Component
struct FeeOrdersPage {
  @State currentTab: number = 0;
  @State allFees: FeeOrder[] = [];
  @State unpaidFees: FeeOrder[] = [];
  @State isLoadingAll: boolean = false;
  @State isLoadingUnpaid: boolean = false;
  @State hasErrorAll: boolean = false;
  @State hasErrorUnpaid: boolean = false;
  private resolvedPatientId: number = 0;
  private registrationMap: Record<number, Registration> = {};

  aboutToAppear(): void {
    this.loadPatientAndFees();
  }

  loadPatientAndFees(): void {
    this.isLoadingAll = true;
    this.isLoadingUnpaid = true;
    this.hasErrorAll = false;
    this.hasErrorUnpaid = false;
    if (AppState.userId === 0) {
      this.isLoadingAll = false;
      this.isLoadingUnpaid = false;
      return;
    }
    PatientService.getPatientInfo(AppState.userId)
      .then((infoResult: Result<Patient>) => {
        if (infoResult.success && infoResult.data !== null) {
          this.resolvedPatientId = infoResult.data.patientId;
          this.loadRegistrationsAndFees();
        } else {
          this.isLoadingAll = false;
          this.isLoadingUnpaid = false;
          this.hasErrorAll = true;
          this.hasErrorUnpaid = true;
        }
      })
      .catch(() => {
        this.isLoadingAll = false;
        this.isLoadingUnpaid = false;
        this.hasErrorAll = true;
        this.hasErrorUnpaid = true;
      });
  }

  loadRegistrationsAndFees(): void {
    RegistrationService.getMyRegistrations(this.resolvedPatientId)
      .then((regResult: Result<Registration[]>) => {
        if (regResult.success && regResult.data !== null) {
          const map: Record<number, Registration> = {};
          for (let i: number = 0; i < regResult.data.length; i++) {
            const reg: Registration = regResult.data[i];
            map[reg.registrationId] = reg;
          }
          this.registrationMap = map;
        }
        this.loadAllFees();
        this.loadUnpaidFees();
      })
      .catch(() => {
        this.loadAllFees();
        this.loadUnpaidFees();
      });
  }

  getRegDoctorDept(order: FeeOrder): string {
    const reg: Registration = this.registrationMap[order.registrationId];
    if (reg !== undefined && reg !== null) {
      let text: string = '';
      if (reg.doctorName !== '') {
        text += reg.doctorName;
      }
      if (reg.deptName !== '') {
        if (text !== '') {
          text += ' | ';
        }
        text += reg.deptName;
      }
      if (text !== '') {
        return text;
      }
    }
    return '';
  }

  loadAllFees(): void {
    this.isLoadingAll = true;
    this.hasErrorAll = false;
    FeeService.getMyFees(this.resolvedPatientId)
      .then((result: Result<FeeOrder[]>) => {
        this.isLoadingAll = false;
        if (result.success && result.data !== null) {
          this.allFees = result.data;
        }
      })
      .catch(() => {
        this.isLoadingAll = false;
        this.hasErrorAll = true;
      });
  }

  loadUnpaidFees(): void {
    this.isLoadingUnpaid = true;
    this.hasErrorUnpaid = false;
    FeeService.getUnpaidFees(this.resolvedPatientId)
      .then((result: Result<FeeOrder[]>) => {
        this.isLoadingUnpaid = false;
        if (result.success && result.data !== null) {
          this.unpaidFees = result.data;
        }
      })
      .catch(() => {
        this.isLoadingUnpaid = false;
        this.hasErrorUnpaid = true;
      });
  }

  refreshAll(): void {
    this.loadAllFees();
    this.loadUnpaidFees();
  }

  getStatusColor(status: string): string {
    if (status === '待支付') {
      return '#FF9500';
    }
    if (status === '已支付') {
      return '#34C759';
    }
    if (status === '已退费') {
      return '#999999';
    }
    return '#999999';
  }

  getAmountColor(status: string): string {
    if (status === '待支付') {
      return '#FF3B30';
    }
    return '#333333';
  }

  handlePay(feeOrder: FeeOrder): void {
    AlertDialog.show({
      title: '确认支付',
      message: '确认支付 ¥' + feeOrder.totalAmount.toString() + '？',
      confirm: {
        value: '确认支付',
        fontColor: '#FF3B30',
        action: () => {
          FeeService.payFee(feeOrder.feeOrderId)
            .then((result: Result<string>) => {
              if (result.success) {
                AlertDialog.show({
                  title: '支付成功',
                  message: '支付成功',
                  confirm: {
                    value: '确定',
                    action: () => {
                      this.refreshAll();
                    }
                  }
                });
              } else {
                AlertDialog.show({
                  title: '支付失败',
                  message: result.message !== '' ? result.message : '支付失败，请重试',
                  confirm: {
                    value: '确定',
                    action: () => {}
                  }
                });
              }
            })
            .catch(() => {
              AlertDialog.show({
                title: '支付失败',
                message: '网络错误，请检查网络连接后重试',
                confirm: {
                  value: '确定',
                  action: () => {}
                }
              });
            });
        }
      },
      cancel: () => {}
    });
  }

  getTypeLabel(feeOrder: FeeOrder): string {
    if (feeOrder.itemType !== '') {
      return feeOrder.itemType;
    }
    if (feeOrder.businessType !== '') {
      return feeOrder.businessType;
    }
    return '其他';
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
        Text('费用管理')
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

      Tabs({ index: this.currentTab }) {
        TabContent() {
          if (this.isLoadingAll) {
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
          } else if (this.hasErrorAll) {
            Column() {
              Text('加载失败')
                .fontSize(16)
                .fontColor('#999999')
              Text('点击重试')
                .fontSize(14)
                .fontColor('#007AFF')
                .margin({ top: 8 })
                .onClick(() => {
                  this.loadAllFees();
                })
            }
            .width('100%')
            .layoutWeight(1)
            .justifyContent(FlexAlign.Center)
          } else if (this.allFees.length === 0) {
            Column() {
              Text('暂无费用记录')
                .fontSize(16)
                .fontColor('#999999')
            }
            .width('100%')
            .layoutWeight(1)
            .justifyContent(FlexAlign.Center)
          } else {
            List() {
              ForEach(this.allFees, (feeOrder: FeeOrder) => {
                ListItem() {
                  Column() {
                    Row() {
                      Text(feeOrder.orderNo)
                        .fontSize(16)
                        .fontWeight(FontWeight.Bold)
                        .fontColor('#333333')
                        .layoutWeight(1)

                      Text(this.getTypeLabel(feeOrder))
                        .fontSize(12)
                        .fontColor('#FFFFFF')
                        .backgroundColor('#007AFF')
                        .borderRadius(4)
                        .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                    }
                    .width('100%')
                    .alignItems(VerticalAlign.Center)

                    if (this.getRegDoctorDept(feeOrder) !== '') {
                      Text(this.getRegDoctorDept(feeOrder))
                        .fontSize(13)
                        .fontColor('#666666')
                        .margin({ top: 6 })
                        .width('100%')
                    }

                    Row() {
                      Text('合计')
                        .fontSize(13)
                        .fontColor('#999999')
                        .margin({ right: 8 })
                      Text('¥' + feeOrder.totalAmount.toString())
                        .fontSize(18)
                        .fontWeight(FontWeight.Bold)
                        .fontColor(this.getAmountColor(feeOrder.status))

                      Text(feeOrder.status)
                        .fontSize(12)
                        .fontColor('#FFFFFF')
                        .backgroundColor(this.getStatusColor(feeOrder.status))
                        .borderRadius(4)
                        .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                        .margin({ left: 12 })
                    }
                    .width('100%')
                    .margin({ top: 8 })
                    .alignItems(VerticalAlign.Center)

                    Row() {
                      Text('已付：¥' + feeOrder.paidAmount.toString())
                        .fontSize(12)
                        .fontColor('#34C759')
                      Text('退款：¥' + feeOrder.refundAmount.toString())
                        .fontSize(12)
                        .fontColor('#999999')
                        .margin({ left: 16 })
                      Blank()
                      Text(feeOrder.paidAt)
                        .fontSize(12)
                        .fontColor('#999999')
                    }
                    .width('100%')
                    .margin({ top: 4 })

                    if (feeOrder.status === '待支付') {
                      Button('立即支付')
                        .width('100%')
                        .height(40)
                        .fontSize(14)
                        .fontColor('#FFFFFF')
                        .backgroundColor('#FF3B30')
                        .borderRadius(8)
                        .margin({ top: 12 })
                        .onClick(() => {
                          this.handlePay(feeOrder);
                        })
                    }
                  }
                  .width('100%')
                  .padding(16)
                  .alignItems(HorizontalAlign.Start)
                }
                .backgroundColor('#FFFFFF')
                .borderRadius(8)
                .margin({ left: 16, right: 16, top: 8, bottom: 4 })
              }, (feeOrder: FeeOrder) => feeOrder.feeOrderId.toString())
            }
            .width('100%')
            .layoutWeight(1)
            .backgroundColor('#F5F5F5')
            .padding({ top: 4 })
          }
        }
        .tabBar('全部费用')

        TabContent() {
          if (this.isLoadingUnpaid) {
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
          } else if (this.hasErrorUnpaid) {
            Column() {
              Text('加载失败')
                .fontSize(16)
                .fontColor('#999999')
              Text('点击重试')
                .fontSize(14)
                .fontColor('#007AFF')
                .margin({ top: 8 })
                .onClick(() => {
                  this.loadUnpaidFees();
                })
            }
            .width('100%')
            .layoutWeight(1)
            .justifyContent(FlexAlign.Center)
          } else if (this.unpaidFees.length === 0) {
            Column() {
              Text('暂无费用记录')
                .fontSize(16)
                .fontColor('#999999')
            }
            .width('100%')
            .layoutWeight(1)
            .justifyContent(FlexAlign.Center)
          } else {
            List() {
              ForEach(this.unpaidFees, (feeOrder: FeeOrder) => {
                ListItem() {
                  Column() {
                    Row() {
                      Text(feeOrder.orderNo)
                        .fontSize(16)
                        .fontWeight(FontWeight.Bold)
                        .fontColor('#333333')
                        .layoutWeight(1)

                      Text(this.getTypeLabel(feeOrder))
                        .fontSize(12)
                        .fontColor('#FFFFFF')
                        .backgroundColor('#007AFF')
                        .borderRadius(4)
                        .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                    }
                    .width('100%')
                    .alignItems(VerticalAlign.Center)

                    if (this.getRegDoctorDept(feeOrder) !== '') {
                      Text(this.getRegDoctorDept(feeOrder))
                        .fontSize(13)
                        .fontColor('#666666')
                        .margin({ top: 6 })
                        .width('100%')
                    }

                    Row() {
                      Text('合计')
                        .fontSize(13)
                        .fontColor('#999999')
                        .margin({ right: 8 })
                      Text('¥' + feeOrder.totalAmount.toString())
                        .fontSize(18)
                        .fontWeight(FontWeight.Bold)
                        .fontColor('#FF3B30')

                      Text(feeOrder.status)
                        .fontSize(12)
                        .fontColor('#FFFFFF')
                        .backgroundColor('#FF9500')
                        .borderRadius(4)
                        .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                        .margin({ left: 12 })
                    }
                    .width('100%')
                    .margin({ top: 8 })
                    .alignItems(VerticalAlign.Center)

                    Button('立即支付')
                      .width('100%')
                      .height(40)
                      .fontSize(14)
                      .fontColor('#FFFFFF')
                      .backgroundColor('#FF3B30')
                      .borderRadius(8)
                      .margin({ top: 12 })
                      .onClick(() => {
                        this.handlePay(feeOrder);
                      })
                  }
                  .width('100%')
                  .padding(16)
                  .alignItems(HorizontalAlign.Start)
                }
                .backgroundColor('#FFFFFF')
                .borderRadius(8)
                .margin({ left: 16, right: 16, top: 8, bottom: 4 })
              }, (feeOrder: FeeOrder) => feeOrder.feeOrderId.toString())
            }
            .width('100%')
            .layoutWeight(1)
            .backgroundColor('#F5F5F5')
            .padding({ top: 4 })
          }
        }
        .tabBar('待支付')
      }
      .width('100%')
      .layoutWeight(1)
      .onChange((index: number) => {
        this.currentTab = index;
      })
    }
    .width('100%')
    .height('100%')
    .backgroundColor('#F5F5F5')
  }
}
