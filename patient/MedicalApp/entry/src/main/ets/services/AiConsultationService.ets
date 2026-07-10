import { HttpClient } from './HttpClient';
import { ApiConfig } from '../common/ApiConfig';
import { Result } from '../common/Result';
import { AiConsultation } from '../models/AiConsultation';
import { AiConsultResponse } from '../models/AiConsultResponse';

interface ConsultRequestBody {
  patientId: number;
  chiefComplaint: string;
  symptomDetail: string;
}

interface SmartConsultRequestBody {
  patientId: number;
  mode: string;
  content: string;
}

export class AiConsultationService {
  static async consult(patientId: number, chiefComplaint: string, symptomDetail: string): Promise<Result<AiConsultation>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.AI_CONSULT;
    const bodyObj: ConsultRequestBody = { patientId: patientId, chiefComplaint: chiefComplaint, symptomDetail: symptomDetail };
    const body: string = JSON.stringify(bodyObj);
    const response: string = await HttpClient.post(url, body);
    const parsed: object = JSON.parse(response);
    const result: Result<AiConsultation> = new Result<AiConsultation>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const itemObj: object = parsed['data'];
      if (itemObj === null || itemObj === undefined) {
        return result;
      }
      const item: AiConsultation = new AiConsultation();
      const consultationIdVal: Object = itemObj['consultationId'];
      item.consultationId = consultationIdVal !== undefined && consultationIdVal !== null ? Number(consultationIdVal.toString()) : 0;
      const patientIdVal: Object = itemObj['patientId'];
      item.patientId = patientIdVal !== undefined && patientIdVal !== null ? Number(patientIdVal.toString()) : 0;
      const chiefComplaintVal: Object = itemObj['chiefComplaint'];
      item.chiefComplaint = chiefComplaintVal !== undefined && chiefComplaintVal !== null ? chiefComplaintVal.toString() : '';
      const symptomDetailVal: Object = itemObj['symptomDetail'];
      item.symptomDetail = symptomDetailVal !== undefined && symptomDetailVal !== null ? symptomDetailVal.toString() : '';
      const aiSummaryVal: Object = itemObj['aiSummary'];
      item.aiSummary = aiSummaryVal !== undefined && aiSummaryVal !== null ? aiSummaryVal.toString() : '';
      const recommendedDeptIdVal: Object = itemObj['recommendedDeptId'];
      item.recommendedDeptId = recommendedDeptIdVal !== undefined && recommendedDeptIdVal !== null ? Number(recommendedDeptIdVal.toString()) : 0;
      const riskLevelVal: Object = itemObj['riskLevel'];
      item.riskLevel = riskLevelVal !== undefined && riskLevelVal !== null ? riskLevelVal.toString() : '';
      const aiResultVal: Object = itemObj['aiResult'];
      item.aiResult = aiResultVal !== undefined && aiResultVal !== null ? aiResultVal.toString() : '';
      const statusVal: Object = itemObj['status'];
      item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
      const recommendedDeptNameVal: Object = itemObj['recommendedDeptName'];
      item.recommendedDeptName = recommendedDeptNameVal !== undefined && recommendedDeptNameVal !== null ? recommendedDeptNameVal.toString() : '';
      result.data = item;
    }
    return result;
  }

  static async smartConsult(patientId: number, mode: string, content: string): Promise<Result<AiConsultResponse>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.AI_SMART;
    const bodyObj: SmartConsultRequestBody = { patientId: patientId, mode: mode, content: content };
    const body: string = JSON.stringify(bodyObj);
    const response: string = await HttpClient.post(url, body);
    const parsed: object = JSON.parse(response);
    const result: Result<AiConsultResponse> = new Result<AiConsultResponse>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const itemObj: object = parsed['data'];
      if (itemObj === null || itemObj === undefined) {
        return result;
      }
      const item: AiConsultResponse = new AiConsultResponse();
      const modeVal: Object = itemObj['mode'];
      item.mode = modeVal !== undefined && modeVal !== null ? modeVal.toString() : '';
      const summaryVal: Object = itemObj['summary'];
      item.summary = summaryVal !== undefined && summaryVal !== null ? summaryVal.toString() : '';
      const riskLevelVal: Object = itemObj['riskLevel'];
      item.riskLevel = riskLevelVal !== undefined && riskLevelVal !== null ? riskLevelVal.toString() : '';
      const recommendedDeptVal: Object = itemObj['recommendedDept'];
      item.recommendedDept = recommendedDeptVal !== undefined && recommendedDeptVal !== null ? recommendedDeptVal.toString() : '';
      const recommendedDoctorVal: Object = itemObj['recommendedDoctor'];
      item.recommendedDoctor = recommendedDoctorVal !== undefined && recommendedDoctorVal !== null ? recommendedDoctorVal.toString() : '';
      const candidatesRaw: Object = itemObj['candidates'];
      const candidates: Array<string> = [];
      if (candidatesRaw !== null && candidatesRaw !== undefined) {
        const candidatesArr: Object[] = candidatesRaw as Object[];
        for (let j: number = 0; j < candidatesArr.length; j++) {
          const candidateVal: Object = candidatesArr[j];
          if (candidateVal !== null && candidateVal !== undefined) {
            candidates.push(candidateVal.toString());
          }
        }
      }
      item.candidates = candidates;
      const aiResultVal: Object = itemObj['aiResult'];
      item.aiResult = aiResultVal !== undefined && aiResultVal !== null ? aiResultVal.toString() : '';
      const noteVal: Object = itemObj['note'];
      item.note = noteVal !== undefined && noteVal !== null ? noteVal.toString() : '';
      result.data = item;
    }
    return result;
  }

  static async getMyConsultations(patientId: number): Promise<Result<AiConsultation[]>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.AI_MY + patientId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<AiConsultation[]> = new Result<AiConsultation[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataRaw: Object = parsed['data'];
      if (dataRaw === null || dataRaw === undefined) {
        return result;
      }
      const dataArr: Object[] = dataRaw as Object[];
      const items: AiConsultation[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: AiConsultation = new AiConsultation();
        const consultationIdVal: Object = itemObj['consultationId'];
        item.consultationId = consultationIdVal !== undefined && consultationIdVal !== null ? Number(consultationIdVal.toString()) : 0;
        const patientIdVal: Object = itemObj['patientId'];
        item.patientId = patientIdVal !== undefined && patientIdVal !== null ? Number(patientIdVal.toString()) : 0;
        const chiefComplaintVal: Object = itemObj['chiefComplaint'];
        item.chiefComplaint = chiefComplaintVal !== undefined && chiefComplaintVal !== null ? chiefComplaintVal.toString() : '';
        const symptomDetailVal: Object = itemObj['symptomDetail'];
        item.symptomDetail = symptomDetailVal !== undefined && symptomDetailVal !== null ? symptomDetailVal.toString() : '';
        const aiSummaryVal: Object = itemObj['aiSummary'];
        item.aiSummary = aiSummaryVal !== undefined && aiSummaryVal !== null ? aiSummaryVal.toString() : '';
        const recommendedDeptIdVal: Object = itemObj['recommendedDeptId'];
        item.recommendedDeptId = recommendedDeptIdVal !== undefined && recommendedDeptIdVal !== null ? Number(recommendedDeptIdVal.toString()) : 0;
        const riskLevelVal: Object = itemObj['riskLevel'];
        item.riskLevel = riskLevelVal !== undefined && riskLevelVal !== null ? riskLevelVal.toString() : '';
        const aiResultVal: Object = itemObj['aiResult'];
        item.aiResult = aiResultVal !== undefined && aiResultVal !== null ? aiResultVal.toString() : '';
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        const recommendedDeptNameVal: Object = itemObj['recommendedDeptName'];
        item.recommendedDeptName = recommendedDeptNameVal !== undefined && recommendedDeptNameVal !== null ? recommendedDeptNameVal.toString() : '';
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }
}
