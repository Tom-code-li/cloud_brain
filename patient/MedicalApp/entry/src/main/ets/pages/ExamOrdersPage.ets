import { router } from '@kit.ArkUI';
import { AppState } from './LoginPage';
import { NavParams } from './DepartmentsPage';
import { ExamLabService } from '../services/ExamLabService';
import { PatientService } from '../services/PatientService';
import { Patient } from '../models/Patient';
import { Result } from '../common/Result';
import { ExamLabOrder } from '../models/ExamLabOrder';
import { ExamLabReport } from '../models/ExamLabReport';

@Entry
@Component
struct ExamOrdersPage {
  @State currentTab: number = 0;
  @State orders: ExamLabOrder[] = [];
  @State reports: ExamLabReport[] = [];
  @State isLoadingOrders: boolean = false;
  @State isLoadingReports: boolean = false;
  @State hasErrorOrders: boolean = false;
  @State hasErrorReports: boolean = false;
  @State expandedOrderId: number = -1;
  private resolvedPatientId: number = 0;

  aboutToAppear(): void {
    this.loadPatientAndData();
  }

  loadPatientAndData(): void {
    this.isLoadingOrders = true;
    this.isLoadingReports = true;
    this.hasErrorOrders = false;
    this.hasErrorReports = false;
    if (AppState.userId === 0) {
      this.isLoadingOrders = false;
      this.isLoadingReports = false;
      return;
    }
    PatientService.getPatientInfo(AppState.userId)
      .then((infoResult: Result<Patient>) => {
        if (infoResult.success && infoResult.data !== null) {
          this.resolvedPatientId = infoResult.data.patientId;
          this.loadOrders();
          this.loadReports();
        } else {
          this.isLoadingOrders = false;
          this.isLoadingReports = false;
          this.hasErrorOrders = true;
          this.hasErrorReports = true;
        }
      })
      .catch(() => {
        this.isLoadingOrders = false;
        this.isLoadingReports = false;
        this.hasErrorOrders = true;
        this.hasErrorReports = true;
      });
  }

  loadOrders(): void {
    this.isLoadingOrders = true;
    this.hasErrorOrders = false;
    ExamLabService.getMyOrders(this.resolvedPatientId)
      .then((result: Result<ExamLabOrder[]>) => {
        this.isLoadingOrders = false;
        if (result.success && result.data !== null) {
          this.orders = result.data;
        }
      })
      .catch(() => {
        this.isLoadingOrders = false;
        this.hasErrorOrders = true;
      });
  }

  loadReports(): void {
    this.isLoadingReports = true;
    this.hasErrorReports = false;
    ExamLabService.getMyReports(this.resolvedPatientId)
      .then((result: Result<ExamLabReport[]>) => {
        this.isLoadingReports = false;
        if (result.success && result.data !== null) {
          this.reports = result.data;
        }
      })
      .catch(() => {
        this.isLoadingReports = false;
        this.hasErrorReports = true;
      });
  }

  getOrderTypeColor(orderType: string): string {
    if (orderType === '检查') {
      return '#007AFF';
    }
    if (orderType === '检验') {
      return '#34C759';
    }
    return '#999999';
  }

  getOrderStatusColor(status: string): string {
    if (status === '待缴费' || status === '待支付') {
      return '#FF9500';
    }
    if (status === '已缴费' || status === '已支付') {
      return '#007AFF';
    }
    if (status === '已完成') {
      return '#34C759';
    }
    return '#999999';
  }

  getReportStatusColor(status: string): string {
    if (status === '已发布') {
      return '#34C759';
    }
    if (status === '草稿') {
      return '#FF9500';
    }
    return '#999999';
  }

  toggleExpand(orderId: number): void {
    if (this.expandedOrderId === orderId) {
      this.expandedOrderId = -1;
    } else {
      this.expandedOrderId = orderId;
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
        Text('检查检验')
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
          if (this.isLoadingOrders) {
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
          } else if (this.hasErrorOrders) {
            Column() {
              Text('加载失败')
                .fontSize(16)
                .fontColor('#999999')
              Text('点击重试')
                .fontSize(14)
                .fontColor('#007AFF')
                .margin({ top: 8 })
                .onClick(() => {
                  this.loadOrders();
                })
            }
            .width('100%')
            .layoutWeight(1)
            .justifyContent(FlexAlign.Center)
          } else if (this.orders.length === 0) {
            Column() {
              Text('暂无检查检验')
                .fontSize(16)
                .fontColor('#999999')
            }
            .width('100%')
            .layoutWeight(1)
            .justifyContent(FlexAlign.Center)
          } else {
            List() {
              ForEach(this.orders, (order: ExamLabOrder) => {
                ListItem() {
                  Column() {
                    Row() {
                      Text(order.orderNo)
                        .fontSize(16)
                        .fontWeight(FontWeight.Bold)
                        .fontColor('#333333')
                        .layoutWeight(1)
                      Text(order.orderType)
                        .fontSize(12)
                        .fontColor('#FFFFFF')
                        .backgroundColor(this.getOrderTypeColor(order.orderType))
                        .borderRadius(4)
                        .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                    }
                    .width('100%')

                    Text(order.clinicalDiagnosis)
                      .fontSize(14)
                      .fontColor('#666666')
                      .margin({ top: 8 })
                      .width('100%')
                      .maxLines(2)
                      .textOverflow({ overflow: TextOverflow.Ellipsis })

                    Row() {
                      Text(order.examSite)
                        .fontSize(13)
                        .fontColor('#999999')
                        .layoutWeight(1)
                      Text('¥' + order.totalAmount.toString())
                        .fontSize(16)
                        .fontColor('#FF3B30')
                        .fontWeight(FontWeight.Bold)
                    }
                    .width('100%')
                    .margin({ top: 8 })

                    Row() {
                      Text(order.status)
                        .fontSize(12)
                        .fontColor('#FFFFFF')
                        .backgroundColor(this.getOrderStatusColor(order.status))
                        .borderRadius(4)
                        .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                      Text(order.appliedAt)
                        .fontSize(12)
                        .fontColor('#999999')
                        .margin({ left: 12 })
                    }
                    .width('100%')
                    .margin({ top: 8 })

                    if (this.expandedOrderId === order.orderId) {
                      Column() {
                        Divider()
                          .margin({ top: 12, bottom: 8 })

                        Row() {
                          Text('检查目的：')
                            .fontSize(13)
                            .fontColor('#999999')
                          Text(order.purpose !== '' ? order.purpose : '暂无')
                            .fontSize(13)
                            .fontColor('#333333')
                            .layoutWeight(1)
                        }
                        .width('100%')
                        .margin({ top: 4 })

                        Row() {
                          Text('标本类型：')
                            .fontSize(13)
                            .fontColor('#999999')
                          Text(order.specimenType !== '' ? order.specimenType : '暂无')
                            .fontSize(13)
                            .fontColor('#333333')
                            .layoutWeight(1)
                        }
                        .width('100%')
                        .margin({ top: 4 })

                        Row() {
                          Text('采集方式：')
                            .fontSize(13)
                            .fontColor('#999999')
                          Text(order.collectionWay !== '' ? order.collectionWay : '暂无')
                            .fontSize(13)
                            .fontColor('#333333')
                            .layoutWeight(1)
                        }
                        .width('100%')
                        .margin({ top: 4 })

                        Row() {
                          Text('备注：')
                            .fontSize(13)
                            .fontColor('#999999')
                          Text(order.remark !== '' ? order.remark : '暂无')
                            .fontSize(13)
                            .fontColor('#333333')
                            .layoutWeight(1)
                        }
                        .width('100%')
                        .margin({ top: 4 })

                        Row() {
                          Text('费用状态：')
                            .fontSize(13)
                            .fontColor('#999999')
                          Text(order.feeStatus !== '' ? order.feeStatus : '暂无')
                            .fontSize(13)
                            .fontColor('#333333')
                            .layoutWeight(1)
                        }
                        .width('100%')
                        .margin({ top: 4 })

                        Row() {
                          Text('执行时间：')
                            .fontSize(13)
                            .fontColor('#999999')
                          Text(order.executedAt !== '' ? order.executedAt : '暂无')
                            .fontSize(13)
                            .fontColor('#333333')
                            .layoutWeight(1)
                        }
                        .width('100%')
                        .margin({ top: 4 })

                        Row() {
                          Text('完成时间：')
                            .fontSize(13)
                            .fontColor('#999999')
                          Text(order.completedAt !== '' ? order.completedAt : '暂无')
                            .fontSize(13)
                            .fontColor('#333333')
                            .layoutWeight(1)
                        }
                        .width('100%')
                        .margin({ top: 4 })
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
                  this.toggleExpand(order.orderId);
                })
              }, (order: ExamLabOrder) => order.orderId.toString())
            }
            .width('100%')
            .layoutWeight(1)
            .backgroundColor('#F5F5F5')
            .padding({ top: 4 })
          }
        }
        .tabBar('检查申请')

        TabContent() {
          if (this.isLoadingReports) {
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
          } else if (this.hasErrorReports) {
            Column() {
              Text('加载失败')
                .fontSize(16)
                .fontColor('#999999')
              Text('点击重试')
                .fontSize(14)
                .fontColor('#007AFF')
                .margin({ top: 8 })
                .onClick(() => {
                  this.loadReports();
                })
            }
            .width('100%')
            .layoutWeight(1)
            .justifyContent(FlexAlign.Center)
          } else if (this.reports.length === 0) {
            Column() {
              Text('暂无报告')
                .fontSize(16)
                .fontColor('#999999')
            }
            .width('100%')
            .layoutWeight(1)
            .justifyContent(FlexAlign.Center)
          } else {
            List() {
              ForEach(this.reports, (report: ExamLabReport) => {
                ListItem() {
                  Column() {
                    Row() {
                      Text(report.reportNo)
                        .fontSize(16)
                        .fontWeight(FontWeight.Bold)
                        .fontColor('#333333')
                        .layoutWeight(1)
                      Text(report.reportType)
                        .fontSize(12)
                        .fontColor('#FFFFFF')
                        .backgroundColor(this.getOrderTypeColor(report.reportType))
                        .borderRadius(4)
                        .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                    }
                    .width('100%')

                    Text(report.conclusion)
                      .fontSize(14)
                      .fontColor('#666666')
                      .margin({ top: 8 })
                      .width('100%')
                      .maxLines(2)
                      .textOverflow({ overflow: TextOverflow.Ellipsis })

                    Row() {
                      Text(report.status)
                        .fontSize(12)
                        .fontColor('#FFFFFF')
                        .backgroundColor(this.getReportStatusColor(report.status))
                        .borderRadius(4)
                        .padding({ left: 8, right: 8, top: 2, bottom: 2 })
                      Text(report.publishedAt)
                        .fontSize(12)
                        .fontColor('#999999')
                        .margin({ left: 12 })
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
                  NavParams.reportId = report.reportId;
                  router.pushUrl({ url: 'pages/ReportDetailPage' });
                })
              }, (report: ExamLabReport) => report.reportId.toString())
            }
            .width('100%')
            .layoutWeight(1)
            .backgroundColor('#F5F5F5')
            .padding({ top: 4 })
          }
        }
        .tabBar('检查报告')
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
