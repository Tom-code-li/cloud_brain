import { HttpClient } from './HttpClient';
import { ApiConfig } from '../common/ApiConfig';
import { Result } from '../common/Result';
import { Patient } from '../models/Patient';
import { SysUser } from '../models/SysUser';

interface LoginResponseShape {
  success: boolean;
  message: string;
  data: SysUser;
}

interface PatientInfoResponseShape {
  success: boolean;
  message: string;
  data: Patient;
}

interface SimpleResponseShape {
  success: boolean;
  message: string;
  data: string;
}

interface LoginRequestBody {
  username: string;
  password: string;
}

interface RegisterRequestBody {
  username: string;
  password: string;
  realName: string;
  phone: string;
  gender: string;
  birthday: string;
  idCard: string;
  emergencyContact: string;
  emergencyPhone: string;
  address: string;
  allergyHistory: string;
}

interface ChangePasswordRequestBody {
  userId: number;
  oldPassword: string;
  newPassword: string;
}

export class PatientService {
  static async login(username: string, password: string): Promise<Result<SysUser>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.LOGIN;
    const bodyObj: LoginRequestBody = { username: username, password: password };
    const body: string = JSON.stringify(bodyObj);
    const response: string = await HttpClient.post(url, body);
    const parsed: object = JSON.parse(response);
    const result: Result<SysUser> = new Result<SysUser>();
    const successCheck: Object = parsed['success'];
    result.success = successCheck === true;
    const msgCheck: Object = parsed['message'];
    result.message = msgCheck !== null && msgCheck !== undefined ? msgCheck.toString() : '';
    if (result.success) {
      const dataObj: object = parsed['data'];
      const user: SysUser = new SysUser();
      user.userId = dataObj['userId'];
      user.roleId = dataObj['roleId'];
      user.username = dataObj['username'];
      user.password = dataObj['password'];
      user.realName = dataObj['realName'];
      user.phone = dataObj['phone'];
      user.email = dataObj['email'];
      user.status = dataObj['status'];
      user.lastLoginAt = dataObj['lastLoginAt'];
      user.createdAt = dataObj['createdAt'];
      user.updatedAt = dataObj['updatedAt'];
      result.data = user;
    }
    return result;
  }

  static async register(username: string, password: string, realName: string, phone: string, gender: string, birthday: string, idCard: string): Promise<Result<string>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.REGISTER;
    const bodyObj: RegisterRequestBody = {
      username: username,
      password: password,
      realName: realName,
      phone: phone,
      gender: gender,
      birthday: birthday,
      idCard: idCard,
      emergencyContact: '',
      emergencyPhone: '',
      address: '',
      allergyHistory: '无'
    };
    const body: string = JSON.stringify(bodyObj);
    const response: string = await HttpClient.post(url, body);
    const parsed: object = JSON.parse(response);
    const result: Result<string> = new Result<string>();
    const successCheck: Object = parsed['success'];
    result.success = successCheck === true;
    const msgCheck: Object = parsed['message'];
    result.message = msgCheck !== null && msgCheck !== undefined ? msgCheck.toString() : '';
    if (result.success) {
      const dataCheck: Object = parsed['data'];
      if (dataCheck !== null && dataCheck !== undefined) {
        result.data = dataCheck.toString();
      }
    }
    return result;
  }

  static async loginForce(username: string, password: string): Promise<Result<SysUser>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.LOGIN_FORCE;
    const bodyObj: LoginRequestBody = { username: username, password: password };
    const body: string = JSON.stringify(bodyObj);
    const response: string = await HttpClient.post(url, body);
    const parsed: object = JSON.parse(response);
    const result: Result<SysUser> = new Result<SysUser>();
    const successCheck: Object = parsed['success'];
    result.success = successCheck === true;
    const msgCheck: Object = parsed['message'];
    result.message = msgCheck !== null && msgCheck !== undefined ? msgCheck.toString() : '';
    if (result.success) {
      const dataObj: object = parsed['data'];
      const user: SysUser = new SysUser();
      user.userId = dataObj['userId'];
      user.roleId = dataObj['roleId'];
      user.username = dataObj['username'];
      user.password = dataObj['password'];
      user.realName = dataObj['realName'];
      user.phone = dataObj['phone'];
      user.email = dataObj['email'];
      user.status = dataObj['status'];
      user.lastLoginAt = dataObj['lastLoginAt'];
      user.createdAt = dataObj['createdAt'];
      user.updatedAt = dataObj['updatedAt'];
      result.data = user;
    }
    return result;
  }

  static async getPatientInfo(userId: number): Promise<Result<Patient>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.PATIENT_INFO + userId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<Patient> = new Result<Patient>();
    const successCheck: Object = parsed['success'];
    result.success = successCheck === true;
    const msgCheck: Object = parsed['message'];
    result.message = msgCheck !== null && msgCheck !== undefined ? msgCheck.toString() : '';
    if (result.success) {
      const dataObj: object = parsed['data'];
      const patient: Patient = new Patient();
      patient.patientId = dataObj['patientId'];
      patient.userId = dataObj['userId'];
      patient.patientNo = dataObj['patientNo'];
      patient.patientName = dataObj['patientName'];
      patient.gender = dataObj['gender'];
      patient.birthday = dataObj['birthday'];
      patient.idCard = dataObj['idCard'];
      patient.phone = dataObj['phone'];
      patient.emergencyContact = dataObj['emergencyContact'];
      patient.emergencyPhone = dataObj['emergencyPhone'];
      patient.address = dataObj['address'];
      patient.allergyHistory = dataObj['allergyHistory'];
      patient.pastHistory = dataObj['pastHistory'];
      patient.status = dataObj['status'];
      result.data = patient;
    }
    return result;
  }

  static async updatePatient(patient: Patient): Promise<Result<string>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.PATIENT_UPDATE;
    const body: string = JSON.stringify(patient);
    const response: string = await HttpClient.put(url, body);
    const parsed: object = JSON.parse(response);
    const result: Result<string> = new Result<string>();
    const successCheck: Object = parsed['success'];
    result.success = successCheck === true;
    const msgCheck: Object = parsed['message'];
    result.message = msgCheck !== null && msgCheck !== undefined ? msgCheck.toString() : '';
    return result;
  }

  static async changePassword(userId: number, oldPassword: string, newPassword: string): Promise<Result<string>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.CHANGE_PASSWORD;
    const bodyObj: ChangePasswordRequestBody = {
      userId: userId,
      oldPassword: oldPassword,
      newPassword: newPassword
    };
    const body: string = JSON.stringify(bodyObj);
    const response: string = await HttpClient.post(url, body);
    const parsed: object = JSON.parse(response);
    const result: Result<string> = new Result<string>();
    const successCheck: Object = parsed['success'];
    result.success = successCheck === true;
    const msgCheck: Object = parsed['message'];
    result.message = msgCheck !== null && msgCheck !== undefined ? msgCheck.toString() : '';
    return result;
  }

  static async logout(userId: number): Promise<Result<string>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.LOGOUT + userId.toString();
    const response: string = await HttpClient.post(url, '{}');
    const parsed: object = JSON.parse(response);
    const result: Result<string> = new Result<string>();
    const successCheck: Object = parsed['success'];
    result.success = successCheck === true;
    const msgCheck: Object = parsed['message'];
    result.message = msgCheck !== null && msgCheck !== undefined ? msgCheck.toString() : '';
    return result;
  }
}
