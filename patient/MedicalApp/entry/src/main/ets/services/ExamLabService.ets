import { HttpClient } from './HttpClient';
import { ApiConfig } from '../common/ApiConfig';
import { Result } from '../common/Result';
import { ExamLabOrder } from '../models/ExamLabOrder';
import { ExamLabReport } from '../models/ExamLabReport';

export class ExamLabService {
  static async getMyOrders(patientId: number): Promise<Result<ExamLabOrder[]>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.EXAM_ORDERS + patientId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<ExamLabOrder[]> = new Result<ExamLabOrder[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataArr: Object[] = parsed['data'];
      const items: ExamLabOrder[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: ExamLabOrder = new ExamLabOrder();
        const orderIdVal: Object = itemObj['orderId'];
        item.orderId = orderIdVal !== undefined && orderIdVal !== null ? Number(orderIdVal.toString()) : 0;
        const orderNoVal: Object = itemObj['orderNo'];
        item.orderNo = orderNoVal !== undefined && orderNoVal !== null ? orderNoVal.toString() : '';
        const visitIdVal: Object = itemObj['visitId'];
        item.visitId = visitIdVal !== undefined && visitIdVal !== null ? Number(visitIdVal.toString()) : 0;
        const recordIdVal: Object = itemObj['recordId'];
        item.recordId = recordIdVal !== undefined && recordIdVal !== null ? Number(recordIdVal.toString()) : 0;
        const patientIdVal: Object = itemObj['patientId'];
        item.patientId = patientIdVal !== undefined && patientIdVal !== null ? Number(patientIdVal.toString()) : 0;
        const applyDoctorIdVal: Object = itemObj['applyDoctorId'];
        item.applyDoctorId = applyDoctorIdVal !== undefined && applyDoctorIdVal !== null ? Number(applyDoctorIdVal.toString()) : 0;
        const executeDeptIdVal: Object = itemObj['executeDeptId'];
        item.executeDeptId = executeDeptIdVal !== undefined && executeDeptIdVal !== null ? Number(executeDeptIdVal.toString()) : 0;
        const orderTypeVal: Object = itemObj['orderType'];
        item.orderType = orderTypeVal !== undefined && orderTypeVal !== null ? orderTypeVal.toString() : '';
        const clinicalDiagnosisVal: Object = itemObj['clinicalDiagnosis'];
        item.clinicalDiagnosis = clinicalDiagnosisVal !== undefined && clinicalDiagnosisVal !== null ? clinicalDiagnosisVal.toString() : '';
        const purposeVal: Object = itemObj['purpose'];
        item.purpose = purposeVal !== undefined && purposeVal !== null ? purposeVal.toString() : '';
        const examSiteVal: Object = itemObj['examSite'];
        item.examSite = examSiteVal !== undefined && examSiteVal !== null ? examSiteVal.toString() : '';
        const specimenTypeVal: Object = itemObj['specimenType'];
        item.specimenType = specimenTypeVal !== undefined && specimenTypeVal !== null ? specimenTypeVal.toString() : '';
        const remarkVal: Object = itemObj['remark'];
        item.remark = remarkVal !== undefined && remarkVal !== null ? remarkVal.toString() : '';
        const priorityVal: Object = itemObj['priority'];
        item.priority = priorityVal !== undefined && priorityVal !== null ? priorityVal.toString() : '';
        const collectionWayVal: Object = itemObj['collectionWay'];
        item.collectionWay = collectionWayVal !== undefined && collectionWayVal !== null ? collectionWayVal.toString() : '';
        const totalAmountVal: Object = itemObj['totalAmount'];
        item.totalAmount = totalAmountVal !== undefined && totalAmountVal !== null ? Number(totalAmountVal.toString()) : 0;
        const feeStatusVal: Object = itemObj['feeStatus'];
        item.feeStatus = feeStatusVal !== undefined && feeStatusVal !== null ? feeStatusVal.toString() : '';
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        const appliedAtVal: Object = itemObj['appliedAt'];
        item.appliedAt = appliedAtVal !== undefined && appliedAtVal !== null ? appliedAtVal.toString() : '';
        const executedAtVal: Object = itemObj['executedAt'];
        item.executedAt = executedAtVal !== undefined && executedAtVal !== null ? executedAtVal.toString() : '';
        const completedAtVal: Object = itemObj['completedAt'];
        item.completedAt = completedAtVal !== undefined && completedAtVal !== null ? completedAtVal.toString() : '';
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }

  static async getMyReports(patientId: number): Promise<Result<ExamLabReport[]>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.EXAM_REPORTS + patientId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<ExamLabReport[]> = new Result<ExamLabReport[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataArr: Object[] = parsed['data'];
      const items: ExamLabReport[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: ExamLabReport = new ExamLabReport();
        const reportIdVal: Object = itemObj['reportId'];
        item.reportId = reportIdVal !== undefined && reportIdVal !== null ? Number(reportIdVal.toString()) : 0;
        const orderIdVal: Object = itemObj['orderId'];
        item.orderId = orderIdVal !== undefined && orderIdVal !== null ? Number(orderIdVal.toString()) : 0;
        const orderItemIdVal: Object = itemObj['orderItemId'];
        item.orderItemId = orderItemIdVal !== undefined && orderItemIdVal !== null ? Number(orderItemIdVal.toString()) : 0;
        const patientIdVal: Object = itemObj['patientId'];
        item.patientId = patientIdVal !== undefined && patientIdVal !== null ? Number(patientIdVal.toString()) : 0;
        const reportDoctorIdVal: Object = itemObj['reportDoctorId'];
        item.reportDoctorId = reportDoctorIdVal !== undefined && reportDoctorIdVal !== null ? Number(reportDoctorIdVal.toString()) : 0;
        const reportNoVal: Object = itemObj['reportNo'];
        item.reportNo = reportNoVal !== undefined && reportNoVal !== null ? reportNoVal.toString() : '';
        const reportTypeVal: Object = itemObj['reportType'];
        item.reportType = reportTypeVal !== undefined && reportTypeVal !== null ? reportTypeVal.toString() : '';
        const findingsVal: Object = itemObj['findings'];
        item.findings = findingsVal !== undefined && findingsVal !== null ? findingsVal.toString() : '';
        const conclusionVal: Object = itemObj['conclusion'];
        item.conclusion = conclusionVal !== undefined && conclusionVal !== null ? conclusionVal.toString() : '';
        const aiDraftVal: Object = itemObj['aiDraft'];
        item.aiDraft = aiDraftVal !== undefined && aiDraftVal !== null ? aiDraftVal.toString() : '';
        const doctorReviewVal: Object = itemObj['doctorReview'];
        item.doctorReview = doctorReviewVal !== undefined && doctorReviewVal !== null ? doctorReviewVal.toString() : '';
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        const publishedAtVal: Object = itemObj['publishedAt'];
        item.publishedAt = publishedAtVal !== undefined && publishedAtVal !== null ? publishedAtVal.toString() : '';
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }

  static async getReportDetail(reportId: number): Promise<Result<ExamLabReport>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.EXAM_REPORT + reportId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<ExamLabReport> = new Result<ExamLabReport>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const itemObj: object = parsed['data'];
      const item: ExamLabReport = new ExamLabReport();
      const reportIdVal: Object = itemObj['reportId'];
      item.reportId = reportIdVal !== undefined && reportIdVal !== null ? Number(reportIdVal.toString()) : 0;
      const orderIdVal: Object = itemObj['orderId'];
      item.orderId = orderIdVal !== undefined && orderIdVal !== null ? Number(orderIdVal.toString()) : 0;
      const orderItemIdVal: Object = itemObj['orderItemId'];
      item.orderItemId = orderItemIdVal !== undefined && orderItemIdVal !== null ? Number(orderItemIdVal.toString()) : 0;
      const patientIdVal: Object = itemObj['patientId'];
      item.patientId = patientIdVal !== undefined && patientIdVal !== null ? Number(patientIdVal.toString()) : 0;
      const reportDoctorIdVal: Object = itemObj['reportDoctorId'];
      item.reportDoctorId = reportDoctorIdVal !== undefined && reportDoctorIdVal !== null ? Number(reportDoctorIdVal.toString()) : 0;
      const reportNoVal: Object = itemObj['reportNo'];
      item.reportNo = reportNoVal !== undefined && reportNoVal !== null ? reportNoVal.toString() : '';
      const reportTypeVal: Object = itemObj['reportType'];
      item.reportType = reportTypeVal !== undefined && reportTypeVal !== null ? reportTypeVal.toString() : '';
      const findingsVal: Object = itemObj['findings'];
      item.findings = findingsVal !== undefined && findingsVal !== null ? findingsVal.toString() : '';
      const conclusionVal: Object = itemObj['conclusion'];
      item.conclusion = conclusionVal !== undefined && conclusionVal !== null ? conclusionVal.toString() : '';
      const aiDraftVal: Object = itemObj['aiDraft'];
      item.aiDraft = aiDraftVal !== undefined && aiDraftVal !== null ? aiDraftVal.toString() : '';
      const doctorReviewVal: Object = itemObj['doctorReview'];
      item.doctorReview = doctorReviewVal !== undefined && doctorReviewVal !== null ? doctorReviewVal.toString() : '';
      const statusVal: Object = itemObj['status'];
      item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
      const publishedAtVal: Object = itemObj['publishedAt'];
      item.publishedAt = publishedAtVal !== undefined && publishedAtVal !== null ? publishedAtVal.toString() : '';
      result.data = item;
    }
    return result;
  }
}
