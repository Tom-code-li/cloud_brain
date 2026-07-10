import { HttpClient } from './HttpClient';
import { ApiConfig } from '../common/ApiConfig';
import { Result } from '../common/Result';
import { FeeOrder } from '../models/FeeOrder';

export class FeeService {
  static async getMyFees(patientId: number): Promise<Result<FeeOrder[]>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.MY_FEES + patientId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<FeeOrder[]> = new Result<FeeOrder[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataArr: Object[] = parsed['data'];
      const items: FeeOrder[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: FeeOrder = new FeeOrder();
        const feeOrderIdVal: Object = itemObj['feeOrderId'];
        item.feeOrderId = feeOrderIdVal !== undefined && feeOrderIdVal !== null ? Number(feeOrderIdVal.toString()) : 0;
        const orderNoVal: Object = itemObj['orderNo'];
        item.orderNo = orderNoVal !== undefined && orderNoVal !== null ? orderNoVal.toString() : '';
        const patientIdVal: Object = itemObj['patientId'];
        item.patientId = patientIdVal !== undefined && patientIdVal !== null ? Number(patientIdVal.toString()) : 0;
        const registrationIdVal: Object = itemObj['registrationId'];
        item.registrationId = registrationIdVal !== undefined && registrationIdVal !== null ? Number(registrationIdVal.toString()) : 0;
        const visitIdVal: Object = itemObj['visitId'];
        item.visitId = visitIdVal !== undefined && visitIdVal !== null ? Number(visitIdVal.toString()) : 0;
        const businessTypeVal: Object = itemObj['businessType'];
        item.businessType = businessTypeVal !== undefined && businessTypeVal !== null ? businessTypeVal.toString() : '';
        const businessIdVal: Object = itemObj['businessId'];
        item.businessId = businessIdVal !== undefined && businessIdVal !== null ? Number(businessIdVal.toString()) : 0;
        const totalAmountVal: Object = itemObj['totalAmount'];
        item.totalAmount = totalAmountVal !== undefined && totalAmountVal !== null ? Number(totalAmountVal.toString()) : 0;
        const paidAmountVal: Object = itemObj['paidAmount'];
        item.paidAmount = paidAmountVal !== undefined && paidAmountVal !== null ? Number(paidAmountVal.toString()) : 0;
        const refundAmountVal: Object = itemObj['refundAmount'];
        item.refundAmount = refundAmountVal !== undefined && refundAmountVal !== null ? Number(refundAmountVal.toString()) : 0;
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        const createdByVal: Object = itemObj['createdBy'];
        item.createdBy = createdByVal !== undefined && createdByVal !== null ? Number(createdByVal.toString()) : 0;
        const paidAtVal: Object = itemObj['paidAt'];
        item.paidAt = paidAtVal !== undefined && paidAtVal !== null ? paidAtVal.toString() : '';
        const itemTypeVal: Object = itemObj['itemType'];
        item.itemType = itemTypeVal !== undefined && itemTypeVal !== null ? itemTypeVal.toString() : '';
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }

  static async getUnpaidFees(patientId: number): Promise<Result<FeeOrder[]>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.UNPAID_FEES + patientId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<FeeOrder[]> = new Result<FeeOrder[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataArr: Object[] = parsed['data'];
      const items: FeeOrder[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: FeeOrder = new FeeOrder();
        const feeOrderIdVal: Object = itemObj['feeOrderId'];
        item.feeOrderId = feeOrderIdVal !== undefined && feeOrderIdVal !== null ? Number(feeOrderIdVal.toString()) : 0;
        const orderNoVal: Object = itemObj['orderNo'];
        item.orderNo = orderNoVal !== undefined && orderNoVal !== null ? orderNoVal.toString() : '';
        const patientIdVal: Object = itemObj['patientId'];
        item.patientId = patientIdVal !== undefined && patientIdVal !== null ? Number(patientIdVal.toString()) : 0;
        const registrationIdVal: Object = itemObj['registrationId'];
        item.registrationId = registrationIdVal !== undefined && registrationIdVal !== null ? Number(registrationIdVal.toString()) : 0;
        const visitIdVal: Object = itemObj['visitId'];
        item.visitId = visitIdVal !== undefined && visitIdVal !== null ? Number(visitIdVal.toString()) : 0;
        const businessTypeVal: Object = itemObj['businessType'];
        item.businessType = businessTypeVal !== undefined && businessTypeVal !== null ? businessTypeVal.toString() : '';
        const businessIdVal: Object = itemObj['businessId'];
        item.businessId = businessIdVal !== undefined && businessIdVal !== null ? Number(businessIdVal.toString()) : 0;
        const totalAmountVal: Object = itemObj['totalAmount'];
        item.totalAmount = totalAmountVal !== undefined && totalAmountVal !== null ? Number(totalAmountVal.toString()) : 0;
        const paidAmountVal: Object = itemObj['paidAmount'];
        item.paidAmount = paidAmountVal !== undefined && paidAmountVal !== null ? Number(paidAmountVal.toString()) : 0;
        const refundAmountVal: Object = itemObj['refundAmount'];
        item.refundAmount = refundAmountVal !== undefined && refundAmountVal !== null ? Number(refundAmountVal.toString()) : 0;
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        const createdByVal: Object = itemObj['createdBy'];
        item.createdBy = createdByVal !== undefined && createdByVal !== null ? Number(createdByVal.toString()) : 0;
        const paidAtVal: Object = itemObj['paidAt'];
        item.paidAt = paidAtVal !== undefined && paidAtVal !== null ? paidAtVal.toString() : '';
        const itemTypeVal: Object = itemObj['itemType'];
        item.itemType = itemTypeVal !== undefined && itemTypeVal !== null ? itemTypeVal.toString() : '';
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }

  static async payFee(feeOrderId: number): Promise<Result<string>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.PAY_FEE + feeOrderId.toString();
    const response: string = await HttpClient.post(url, '{}');
    const parsed: object = JSON.parse(response);
    const result: Result<string> = new Result<string>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataVal: Object = parsed['data'];
      result.data = dataVal !== undefined && dataVal !== null ? dataVal.toString() : '';
    }
    return result;
  }
}
