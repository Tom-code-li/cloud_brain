import { HttpClient } from './HttpClient';
import { ApiConfig } from '../common/ApiConfig';
import { Result } from '../common/Result';
import { Prescription } from '../models/Prescription';
import { PrescriptionItem } from '../models/PrescriptionItem';

export class PrescriptionService {
  static async getMyPrescriptions(patientId: number): Promise<Result<Prescription[]>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.MY_PRESCRIPTIONS + patientId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<Prescription[]> = new Result<Prescription[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataArr: Object[] = parsed['data'];
      const items: Prescription[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: Prescription = new Prescription();
        const prescriptionIdVal: Object = itemObj['prescriptionId'];
        item.prescriptionId = prescriptionIdVal !== undefined && prescriptionIdVal !== null ? Number(prescriptionIdVal.toString()) : 0;
        const prescriptionNoVal: Object = itemObj['prescriptionNo'];
        item.prescriptionNo = prescriptionNoVal !== undefined && prescriptionNoVal !== null ? prescriptionNoVal.toString() : '';
        const visitIdVal: Object = itemObj['visitId'];
        item.visitId = visitIdVal !== undefined && visitIdVal !== null ? Number(visitIdVal.toString()) : 0;
        const recordIdVal: Object = itemObj['recordId'];
        item.recordId = recordIdVal !== undefined && recordIdVal !== null ? Number(recordIdVal.toString()) : 0;
        const patientIdVal: Object = itemObj['patientId'];
        item.patientId = patientIdVal !== undefined && patientIdVal !== null ? Number(patientIdVal.toString()) : 0;
        const doctorIdVal: Object = itemObj['doctorId'];
        item.doctorId = doctorIdVal !== undefined && doctorIdVal !== null ? Number(doctorIdVal.toString()) : 0;
        const totalAmountVal: Object = itemObj['totalAmount'];
        item.totalAmount = totalAmountVal !== undefined && totalAmountVal !== null ? Number(totalAmountVal.toString()) : 0;
        const feeStatusVal: Object = itemObj['feeStatus'];
        item.feeStatus = feeStatusVal !== undefined && feeStatusVal !== null ? feeStatusVal.toString() : '';
        const auditStatusVal: Object = itemObj['auditStatus'];
        item.auditStatus = auditStatusVal !== undefined && auditStatusVal !== null ? auditStatusVal.toString() : '';
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        const diagnosisVal: Object = itemObj['diagnosis'];
        item.diagnosis = diagnosisVal !== undefined && diagnosisVal !== null ? diagnosisVal.toString() : '';
        const usageNoteVal: Object = itemObj['usageNote'];
        item.usageNote = usageNoteVal !== undefined && usageNoteVal !== null ? usageNoteVal.toString() : '';
        const auditDoctorIdVal: Object = itemObj['auditDoctorId'];
        item.auditDoctorId = auditDoctorIdVal !== undefined && auditDoctorIdVal !== null ? Number(auditDoctorIdVal.toString()) : 0;
        const auditNoteVal: Object = itemObj['auditNote'];
        item.auditNote = auditNoteVal !== undefined && auditNoteVal !== null ? auditNoteVal.toString() : '';
        const auditedAtVal: Object = itemObj['auditedAt'];
        item.auditedAt = auditedAtVal !== undefined && auditedAtVal !== null ? auditedAtVal.toString() : '';
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }

  static async getPrescriptionDetail(prescriptionId: number): Promise<Result<Prescription>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.PRESCRIPTION_DETAIL + prescriptionId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<Prescription> = new Result<Prescription>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const itemObj: object = parsed['data'];
      const item: Prescription = new Prescription();
      const prescriptionIdVal: Object = itemObj['prescriptionId'];
      item.prescriptionId = prescriptionIdVal !== undefined && prescriptionIdVal !== null ? Number(prescriptionIdVal.toString()) : 0;
      const prescriptionNoVal: Object = itemObj['prescriptionNo'];
      item.prescriptionNo = prescriptionNoVal !== undefined && prescriptionNoVal !== null ? prescriptionNoVal.toString() : '';
      const visitIdVal: Object = itemObj['visitId'];
      item.visitId = visitIdVal !== undefined && visitIdVal !== null ? Number(visitIdVal.toString()) : 0;
      const recordIdVal: Object = itemObj['recordId'];
      item.recordId = recordIdVal !== undefined && recordIdVal !== null ? Number(recordIdVal.toString()) : 0;
      const patientIdVal: Object = itemObj['patientId'];
      item.patientId = patientIdVal !== undefined && patientIdVal !== null ? Number(patientIdVal.toString()) : 0;
      const doctorIdVal: Object = itemObj['doctorId'];
      item.doctorId = doctorIdVal !== undefined && doctorIdVal !== null ? Number(doctorIdVal.toString()) : 0;
      const totalAmountVal: Object = itemObj['totalAmount'];
      item.totalAmount = totalAmountVal !== undefined && totalAmountVal !== null ? Number(totalAmountVal.toString()) : 0;
      const feeStatusVal: Object = itemObj['feeStatus'];
      item.feeStatus = feeStatusVal !== undefined && feeStatusVal !== null ? feeStatusVal.toString() : '';
      const auditStatusVal: Object = itemObj['auditStatus'];
      item.auditStatus = auditStatusVal !== undefined && auditStatusVal !== null ? auditStatusVal.toString() : '';
      const statusVal: Object = itemObj['status'];
      item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
      const diagnosisVal: Object = itemObj['diagnosis'];
      item.diagnosis = diagnosisVal !== undefined && diagnosisVal !== null ? diagnosisVal.toString() : '';
      const usageNoteVal: Object = itemObj['usageNote'];
      item.usageNote = usageNoteVal !== undefined && usageNoteVal !== null ? usageNoteVal.toString() : '';
      const auditDoctorIdVal: Object = itemObj['auditDoctorId'];
      item.auditDoctorId = auditDoctorIdVal !== undefined && auditDoctorIdVal !== null ? Number(auditDoctorIdVal.toString()) : 0;
      const auditNoteVal: Object = itemObj['auditNote'];
      item.auditNote = auditNoteVal !== undefined && auditNoteVal !== null ? auditNoteVal.toString() : '';
      const auditedAtVal: Object = itemObj['auditedAt'];
      item.auditedAt = auditedAtVal !== undefined && auditedAtVal !== null ? auditedAtVal.toString() : '';
      result.data = item;
    }
    return result;
  }

  static async getPrescriptionItems(prescriptionId: number): Promise<Result<PrescriptionItem[]>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.PRESCRIPTION_ITEMS + prescriptionId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<PrescriptionItem[]> = new Result<PrescriptionItem[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataArr: Object[] = parsed['data'];
      const items: PrescriptionItem[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: PrescriptionItem = new PrescriptionItem();
        const prescriptionItemIdVal: Object = itemObj['prescriptionItemId'];
        item.prescriptionItemId = prescriptionItemIdVal !== undefined && prescriptionItemIdVal !== null ? Number(prescriptionItemIdVal.toString()) : 0;
        const prescriptionIdVal: Object = itemObj['prescriptionId'];
        item.prescriptionId = prescriptionIdVal !== undefined && prescriptionIdVal !== null ? Number(prescriptionIdVal.toString()) : 0;
        const drugIdVal: Object = itemObj['drugId'];
        item.drugId = drugIdVal !== undefined && drugIdVal !== null ? Number(drugIdVal.toString()) : 0;
        const drugNameVal: Object = itemObj['drugName'];
        item.drugName = drugNameVal !== undefined && drugNameVal !== null ? drugNameVal.toString() : '';
        const specificationVal: Object = itemObj['specification'];
        item.specification = specificationVal !== undefined && specificationVal !== null ? specificationVal.toString() : '';
        const unitPriceVal: Object = itemObj['unitPrice'];
        item.unitPrice = unitPriceVal !== undefined && unitPriceVal !== null ? Number(unitPriceVal.toString()) : 0;
        const quantityVal: Object = itemObj['quantity'];
        item.quantity = quantityVal !== undefined && quantityVal !== null ? Number(quantityVal.toString()) : 0;
        const amountVal: Object = itemObj['amount'];
        item.amount = amountVal !== undefined && amountVal !== null ? Number(amountVal.toString()) : 0;
        const dosageVal: Object = itemObj['dosage'];
        item.dosage = dosageVal !== undefined && dosageVal !== null ? dosageVal.toString() : '';
        const frequencyVal: Object = itemObj['frequency'];
        item.frequency = frequencyVal !== undefined && frequencyVal !== null ? frequencyVal.toString() : '';
        const usageMethodVal: Object = itemObj['usageMethod'];
        item.usageMethod = usageMethodVal !== undefined && usageMethodVal !== null ? usageMethodVal.toString() : '';
        const daysVal: Object = itemObj['days'];
        item.days = daysVal !== undefined && daysVal !== null ? Number(daysVal.toString()) : 0;
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }
}
