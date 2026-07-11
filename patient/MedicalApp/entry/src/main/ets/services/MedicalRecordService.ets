import { HttpClient } from './HttpClient';
import { ApiConfig } from '../common/ApiConfig';
import { Result } from '../common/Result';
import { MedicalRecord } from '../models/MedicalRecord';

export class MedicalRecordService {
  static async getMyRecords(patientId: number): Promise<Result<MedicalRecord[]>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.MY_RECORDS + patientId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<MedicalRecord[]> = new Result<MedicalRecord[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataArr: Object[] = parsed['data'];
      const items: MedicalRecord[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: MedicalRecord = new MedicalRecord();
        const recordIdVal: Object = itemObj['recordId'];
        item.recordId = recordIdVal !== undefined && recordIdVal !== null ? Number(recordIdVal.toString()) : 0;
        const visitIdVal: Object = itemObj['visitId'];
        item.visitId = visitIdVal !== undefined && visitIdVal !== null ? Number(visitIdVal.toString()) : 0;
        const patientIdVal: Object = itemObj['patientId'];
        item.patientId = patientIdVal !== undefined && patientIdVal !== null ? Number(patientIdVal.toString()) : 0;
        const doctorIdVal: Object = itemObj['doctorId'];
        item.doctorId = doctorIdVal !== undefined && doctorIdVal !== null ? Number(doctorIdVal.toString()) : 0;
        const chiefComplaintVal: Object = itemObj['chiefComplaint'];
        item.chiefComplaint = chiefComplaintVal !== undefined && chiefComplaintVal !== null ? chiefComplaintVal.toString() : '';
        const presentIllnessVal: Object = itemObj['presentIllness'];
        item.presentIllness = presentIllnessVal !== undefined && presentIllnessVal !== null ? presentIllnessVal.toString() : '';
        const currentTreatmentVal: Object = itemObj['currentTreatment'];
        item.currentTreatment = currentTreatmentVal !== undefined && currentTreatmentVal !== null ? currentTreatmentVal.toString() : '';
        const pastHistoryVal: Object = itemObj['pastHistory'];
        item.pastHistory = pastHistoryVal !== undefined && pastHistoryVal !== null ? pastHistoryVal.toString() : '';
        const allergyHistoryVal: Object = itemObj['allergyHistory'];
        item.allergyHistory = allergyHistoryVal !== undefined && allergyHistoryVal !== null ? allergyHistoryVal.toString() : '';
        const physicalExamVal: Object = itemObj['physicalExam'];
        item.physicalExam = physicalExamVal !== undefined && physicalExamVal !== null ? physicalExamVal.toString() : '';
        const auxiliaryExamVal: Object = itemObj['auxiliaryExam'];
        item.auxiliaryExam = auxiliaryExamVal !== undefined && auxiliaryExamVal !== null ? auxiliaryExamVal.toString() : '';
        const diagnosisVal: Object = itemObj['diagnosis'];
        item.diagnosis = diagnosisVal !== undefined && diagnosisVal !== null ? diagnosisVal.toString() : '';
        const treatmentAdviceVal: Object = itemObj['treatmentAdvice'];
        item.treatmentAdvice = treatmentAdviceVal !== undefined && treatmentAdviceVal !== null ? treatmentAdviceVal.toString() : '';
        const doctorNoteVal: Object = itemObj['doctorNote'];
        item.doctorNote = doctorNoteVal !== undefined && doctorNoteVal !== null ? doctorNoteVal.toString() : '';
        const finalDiagnosisVal: Object = itemObj['finalDiagnosis'];
        item.finalDiagnosis = finalDiagnosisVal !== undefined && finalDiagnosisVal !== null ? finalDiagnosisVal.toString() : '';
        const finalOpinionVal: Object = itemObj['finalOpinion'];
        item.finalOpinion = finalOpinionVal !== undefined && finalOpinionVal !== null ? finalOpinionVal.toString() : '';
        const confirmedDoctorIdVal: Object = itemObj['confirmedDoctorId'];
        item.confirmedDoctorId = confirmedDoctorIdVal !== undefined && confirmedDoctorIdVal !== null ? Number(confirmedDoctorIdVal.toString()) : 0;
        const confirmedAtVal: Object = itemObj['confirmedAt'];
        item.confirmedAt = confirmedAtVal !== undefined && confirmedAtVal !== null ? confirmedAtVal.toString() : '';
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        const initialSavedAtVal: Object = itemObj['initialSavedAt'];
        item.initialSavedAt = initialSavedAtVal !== undefined && initialSavedAtVal !== null ? initialSavedAtVal.toString() : '';
        const completedAtVal: Object = itemObj['completedAt'];
        item.completedAt = completedAtVal !== undefined && completedAtVal !== null ? completedAtVal.toString() : '';
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }

  static async getRecordDetail(recordId: number): Promise<Result<MedicalRecord>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.RECORD_DETAIL + recordId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<MedicalRecord> = new Result<MedicalRecord>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const itemObj: object = parsed['data'];
      const item: MedicalRecord = new MedicalRecord();
      const recordIdVal: Object = itemObj['recordId'];
      item.recordId = recordIdVal !== undefined && recordIdVal !== null ? Number(recordIdVal.toString()) : 0;
      const visitIdVal: Object = itemObj['visitId'];
      item.visitId = visitIdVal !== undefined && visitIdVal !== null ? Number(visitIdVal.toString()) : 0;
      const patientIdVal: Object = itemObj['patientId'];
      item.patientId = patientIdVal !== undefined && patientIdVal !== null ? Number(patientIdVal.toString()) : 0;
      const doctorIdVal: Object = itemObj['doctorId'];
      item.doctorId = doctorIdVal !== undefined && doctorIdVal !== null ? Number(doctorIdVal.toString()) : 0;
      const chiefComplaintVal: Object = itemObj['chiefComplaint'];
      item.chiefComplaint = chiefComplaintVal !== undefined && chiefComplaintVal !== null ? chiefComplaintVal.toString() : '';
      const presentIllnessVal: Object = itemObj['presentIllness'];
      item.presentIllness = presentIllnessVal !== undefined && presentIllnessVal !== null ? presentIllnessVal.toString() : '';
      const currentTreatmentVal: Object = itemObj['currentTreatment'];
      item.currentTreatment = currentTreatmentVal !== undefined && currentTreatmentVal !== null ? currentTreatmentVal.toString() : '';
      const pastHistoryVal: Object = itemObj['pastHistory'];
      item.pastHistory = pastHistoryVal !== undefined && pastHistoryVal !== null ? pastHistoryVal.toString() : '';
      const allergyHistoryVal: Object = itemObj['allergyHistory'];
      item.allergyHistory = allergyHistoryVal !== undefined && allergyHistoryVal !== null ? allergyHistoryVal.toString() : '';
      const physicalExamVal: Object = itemObj['physicalExam'];
      item.physicalExam = physicalExamVal !== undefined && physicalExamVal !== null ? physicalExamVal.toString() : '';
      const auxiliaryExamVal: Object = itemObj['auxiliaryExam'];
      item.auxiliaryExam = auxiliaryExamVal !== undefined && auxiliaryExamVal !== null ? auxiliaryExamVal.toString() : '';
      const diagnosisVal: Object = itemObj['diagnosis'];
      item.diagnosis = diagnosisVal !== undefined && diagnosisVal !== null ? diagnosisVal.toString() : '';
      const treatmentAdviceVal: Object = itemObj['treatmentAdvice'];
      item.treatmentAdvice = treatmentAdviceVal !== undefined && treatmentAdviceVal !== null ? treatmentAdviceVal.toString() : '';
      const doctorNoteVal: Object = itemObj['doctorNote'];
      item.doctorNote = doctorNoteVal !== undefined && doctorNoteVal !== null ? doctorNoteVal.toString() : '';
      const finalDiagnosisVal: Object = itemObj['finalDiagnosis'];
      item.finalDiagnosis = finalDiagnosisVal !== undefined && finalDiagnosisVal !== null ? finalDiagnosisVal.toString() : '';
      const finalOpinionVal: Object = itemObj['finalOpinion'];
      item.finalOpinion = finalOpinionVal !== undefined && finalOpinionVal !== null ? finalOpinionVal.toString() : '';
      const confirmedDoctorIdVal: Object = itemObj['confirmedDoctorId'];
      item.confirmedDoctorId = confirmedDoctorIdVal !== undefined && confirmedDoctorIdVal !== null ? Number(confirmedDoctorIdVal.toString()) : 0;
      const confirmedAtVal: Object = itemObj['confirmedAt'];
      item.confirmedAt = confirmedAtVal !== undefined && confirmedAtVal !== null ? confirmedAtVal.toString() : '';
      const statusVal: Object = itemObj['status'];
      item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
      const initialSavedAtVal: Object = itemObj['initialSavedAt'];
      item.initialSavedAt = initialSavedAtVal !== undefined && initialSavedAtVal !== null ? initialSavedAtVal.toString() : '';
      const completedAtVal: Object = itemObj['completedAt'];
      item.completedAt = completedAtVal !== undefined && completedAtVal !== null ? completedAtVal.toString() : '';
      result.data = item;
    }
    return result;
  }
}
