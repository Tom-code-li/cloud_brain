import { HttpClient } from './HttpClient';
import { ApiConfig } from '../common/ApiConfig';
import { Result } from '../common/Result';
import { Department } from '../models/Department';
import { Doctor } from '../models/Doctor';
import { DoctorSchedule } from '../models/DoctorSchedule';
import { Registration } from '../models/Registration';

interface BookRequestBody {
  patientId: number;
  doctorId: number;
  scheduleId: number;
  deptId: number;
  registrationFee: number;
}

export class RegistrationService {
  static async getAllDepartments(): Promise<Result<Department[]>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.DEPARTMENTS;
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<Department[]> = new Result<Department[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataArr: Object[] = parsed['data'];
      const items: Department[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: Department = new Department();
        const deptIdVal: Object = itemObj['deptId'];
        item.deptId = deptIdVal !== undefined && deptIdVal !== null ? Number(deptIdVal.toString()) : 0;
        const parentIdVal: Object = itemObj['parentId'];
        item.parentId = parentIdVal !== undefined && parentIdVal !== null ? Number(parentIdVal.toString()) : 0;
        const deptCodeVal: Object = itemObj['deptCode'];
        item.deptCode = deptCodeVal !== undefined && deptCodeVal !== null ? deptCodeVal.toString() : '';
        const deptNameVal: Object = itemObj['deptName'];
        item.deptName = deptNameVal !== undefined && deptNameVal !== null ? deptNameVal.toString() : '';
        const deptTypeVal: Object = itemObj['deptType'];
        item.deptType = deptTypeVal !== undefined && deptTypeVal !== null ? deptTypeVal.toString() : '';
        const floorVal: Object = itemObj['floor'];
        item.floor = floorVal !== undefined && floorVal !== null ? floorVal.toString() : '';
        const phoneVal: Object = itemObj['phone'];
        item.phone = phoneVal !== undefined && phoneVal !== null ? phoneVal.toString() : '';
        const locationVal: Object = itemObj['location'];
        item.location = locationVal !== undefined && locationVal !== null ? locationVal.toString() : '';
        const descriptionVal: Object = itemObj['description'];
        item.description = descriptionVal !== undefined && descriptionVal !== null ? descriptionVal.toString() : '';
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        const sortOrderVal: Object = itemObj['sortOrder'];
        item.sortOrder = sortOrderVal !== undefined && sortOrderVal !== null ? Number(sortOrderVal.toString()) : 0;
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }

  static async getDoctorsByDept(deptId: number): Promise<Result<Doctor[]>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.DOCTORS + deptId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<Doctor[]> = new Result<Doctor[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataArr: Object[] = parsed['data'];
      const items: Doctor[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: Doctor = new Doctor();
        const doctorIdVal: Object = itemObj['doctorId'];
        item.doctorId = doctorIdVal !== undefined && doctorIdVal !== null ? Number(doctorIdVal.toString()) : 0;
        const userIdVal: Object = itemObj['userId'];
        item.userId = userIdVal !== undefined && userIdVal !== null ? Number(userIdVal.toString()) : 0;
        const deptIdVal: Object = itemObj['deptId'];
        item.deptId = deptIdVal !== undefined && deptIdVal !== null ? Number(deptIdVal.toString()) : 0;
        const doctorNoVal: Object = itemObj['doctorNo'];
        item.doctorNo = doctorNoVal !== undefined && doctorNoVal !== null ? doctorNoVal.toString() : '';
        const doctorTypeVal: Object = itemObj['doctorType'];
        item.doctorType = doctorTypeVal !== undefined && doctorTypeVal !== null ? doctorTypeVal.toString() : '';
        const titleVal: Object = itemObj['title'];
        item.title = titleVal !== undefined && titleVal !== null ? titleVal.toString() : '';
        const specialtyVal: Object = itemObj['specialty'];
        item.specialty = specialtyVal !== undefined && specialtyVal !== null ? specialtyVal.toString() : '';
        const introductionVal: Object = itemObj['introduction'];
        item.introduction = introductionVal !== undefined && introductionVal !== null ? introductionVal.toString() : '';
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        const realNameVal: Object = itemObj['realName'];
        item.realName = realNameVal !== undefined && realNameVal !== null ? realNameVal.toString() : '';
        const appointmentStatusVal: Object = itemObj['appointmentStatus'];
        item.appointmentStatus = appointmentStatusVal !== undefined && appointmentStatusVal !== null ? appointmentStatusVal.toString() : '';
        const availableQuotaVal: Object = itemObj['availableQuota'];
        item.availableQuota = availableQuotaVal !== undefined && availableQuotaVal !== null ? Number(availableQuotaVal.toString()) : 0;
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }

  static async getDoctorSchedules(doctorId: number): Promise<Result<DoctorSchedule[]>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.SCHEDULES + doctorId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<DoctorSchedule[]> = new Result<DoctorSchedule[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataArr: Object[] = parsed['data'];
      const items: DoctorSchedule[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: DoctorSchedule = new DoctorSchedule();
        const scheduleIdVal: Object = itemObj['scheduleId'];
        item.scheduleId = scheduleIdVal !== undefined && scheduleIdVal !== null ? Number(scheduleIdVal.toString()) : 0;
        const doctorIdVal: Object = itemObj['doctorId'];
        item.doctorId = doctorIdVal !== undefined && doctorIdVal !== null ? Number(doctorIdVal.toString()) : 0;
        const deptIdVal: Object = itemObj['deptId'];
        item.deptId = deptIdVal !== undefined && deptIdVal !== null ? Number(deptIdVal.toString()) : 0;
        const workDateVal: Object = itemObj['workDate'];
        item.workDate = workDateVal !== undefined && workDateVal !== null ? workDateVal.toString() : '';
        const timePeriodVal: Object = itemObj['timePeriod'];
        item.timePeriod = timePeriodVal !== undefined && timePeriodVal !== null ? timePeriodVal.toString() : '';
        const startTimeVal: Object = itemObj['startTime'];
        item.startTime = startTimeVal !== undefined && startTimeVal !== null ? startTimeVal.toString() : '';
        const endTimeVal: Object = itemObj['endTime'];
        item.endTime = endTimeVal !== undefined && endTimeVal !== null ? endTimeVal.toString() : '';
        const totalQuotaVal: Object = itemObj['totalQuota'];
        item.totalQuota = totalQuotaVal !== undefined && totalQuotaVal !== null ? Number(totalQuotaVal.toString()) : 0;
        const remainQuotaVal: Object = itemObj['remainQuota'];
        item.remainQuota = remainQuotaVal !== undefined && remainQuotaVal !== null ? Number(remainQuotaVal.toString()) : 0;
        const registrationFeeVal: Object = itemObj['registrationFee'];
        item.registrationFee = registrationFeeVal !== undefined && registrationFeeVal !== null ? Number(registrationFeeVal.toString()) : 0;
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        const sourceVal: Object = itemObj['source'];
        item.source = sourceVal !== undefined && sourceVal !== null ? sourceVal.toString() : '';
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }

  static async bookAppointment(patientId: number, doctorId: number, scheduleId: number, deptId: number, registrationFee: number): Promise<Result<string>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.BOOK;
    const bodyObj: BookRequestBody = { patientId: patientId, doctorId: doctorId, scheduleId: scheduleId, deptId: deptId, registrationFee: registrationFee };
    const body: string = JSON.stringify(bodyObj);
    const response: string = await HttpClient.post(url, body);
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

  static async getMyRegistrations(patientId: number): Promise<Result<Registration[]>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.MY_REGISTRATIONS + patientId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<Registration[]> = new Result<Registration[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataArr: Object[] = parsed['data'];
      const items: Registration[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: Registration = new Registration();
        const registrationIdVal: Object = itemObj['registrationId'];
        item.registrationId = registrationIdVal !== undefined && registrationIdVal !== null ? Number(registrationIdVal.toString()) : 0;
        const patientIdVal: Object = itemObj['patientId'];
        item.patientId = patientIdVal !== undefined && patientIdVal !== null ? Number(patientIdVal.toString()) : 0;
        const consultationIdVal: Object = itemObj['consultationId'];
        item.consultationId = consultationIdVal !== undefined && consultationIdVal !== null ? Number(consultationIdVal.toString()) : 0;
        const deptIdVal: Object = itemObj['deptId'];
        item.deptId = deptIdVal !== undefined && deptIdVal !== null ? Number(deptIdVal.toString()) : 0;
        const doctorIdVal: Object = itemObj['doctorId'];
        item.doctorId = doctorIdVal !== undefined && doctorIdVal !== null ? Number(doctorIdVal.toString()) : 0;
        const scheduleIdVal: Object = itemObj['scheduleId'];
        item.scheduleId = scheduleIdVal !== undefined && scheduleIdVal !== null ? Number(scheduleIdVal.toString()) : 0;
        const operatorUserIdVal: Object = itemObj['operatorUserId'];
        item.operatorUserId = operatorUserIdVal !== undefined && operatorUserIdVal !== null ? Number(operatorUserIdVal.toString()) : 0;
        const sourceVal: Object = itemObj['source'];
        item.source = sourceVal !== undefined && sourceVal !== null ? sourceVal.toString() : '';
        const registrationNoVal: Object = itemObj['registrationNo'];
        item.registrationNo = registrationNoVal !== undefined && registrationNoVal !== null ? registrationNoVal.toString() : '';
        const queueNoVal: Object = itemObj['queueNo'];
        item.queueNo = queueNoVal !== undefined && queueNoVal !== null ? queueNoVal.toString() : '';
        const registrationFeeVal: Object = itemObj['registrationFee'];
        item.registrationFee = registrationFeeVal !== undefined && registrationFeeVal !== null ? Number(registrationFeeVal.toString()) : 0;
        const feeStatusVal: Object = itemObj['feeStatus'];
        item.feeStatus = feeStatusVal !== undefined && feeStatusVal !== null ? feeStatusVal.toString() : '';
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        const registeredAtVal: Object = itemObj['registeredAt'];
        item.registeredAt = registeredAtVal !== undefined && registeredAtVal !== null ? registeredAtVal.toString() : '';
        const calledAtVal: Object = itemObj['calledAt'];
        item.calledAt = calledAtVal !== undefined && calledAtVal !== null ? calledAtVal.toString() : '';
        const doctorNameVal: Object = itemObj['doctorName'];
        item.doctorName = doctorNameVal !== undefined && doctorNameVal !== null ? doctorNameVal.toString() : '';
        const deptNameVal: Object = itemObj['deptName'];
        item.deptName = deptNameVal !== undefined && deptNameVal !== null ? deptNameVal.toString() : '';
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }

  static async cancelRegistration(registrationId: number): Promise<Result<string>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.CANCEL_REGISTRATION + registrationId.toString();
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
