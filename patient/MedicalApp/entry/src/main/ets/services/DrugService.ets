import { HttpClient } from './HttpClient';
import { ApiConfig } from '../common/ApiConfig';
import { Result } from '../common/Result';
import { Drug } from '../models/Drug';

export class DrugService {
  static async searchDrugs(keyword: string): Promise<Result<Drug[]>> {
    const encodedKeyword: string = encodeURIComponent(keyword);
    const url: string = ApiConfig.BASE_URL + ApiConfig.DRUG_SEARCH + '?keyword=' + encodedKeyword;
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<Drug[]> = new Result<Drug[]>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const dataArr: Object[] = parsed['data'];
      const items: Drug[] = [];
      for (let i: number = 0; i < dataArr.length; i++) {
        const itemObj: object = dataArr[i];
        const item: Drug = new Drug();
        const drugIdVal: Object = itemObj['drugId'];
        item.drugId = drugIdVal !== undefined && drugIdVal !== null ? Number(drugIdVal.toString()) : 0;
        const drugCodeVal: Object = itemObj['drugCode'];
        item.drugCode = drugCodeVal !== undefined && drugCodeVal !== null ? drugCodeVal.toString() : '';
        const drugNameVal: Object = itemObj['drugName'];
        item.drugName = drugNameVal !== undefined && drugNameVal !== null ? drugNameVal.toString() : '';
        const specificationVal: Object = itemObj['specification'];
        item.specification = specificationVal !== undefined && specificationVal !== null ? specificationVal.toString() : '';
        const dosageFormVal: Object = itemObj['dosageForm'];
        item.dosageForm = dosageFormVal !== undefined && dosageFormVal !== null ? dosageFormVal.toString() : '';
        const manufacturerVal: Object = itemObj['manufacturer'];
        item.manufacturer = manufacturerVal !== undefined && manufacturerVal !== null ? manufacturerVal.toString() : '';
        const unitVal: Object = itemObj['unit'];
        item.unit = unitVal !== undefined && unitVal !== null ? unitVal.toString() : '';
        const salePriceVal: Object = itemObj['salePrice'];
        item.salePrice = salePriceVal !== undefined && salePriceVal !== null ? Number(salePriceVal.toString()) : 0;
        const stockQuantityVal: Object = itemObj['stockQuantity'];
        item.stockQuantity = stockQuantityVal !== undefined && stockQuantityVal !== null ? Number(stockQuantityVal.toString()) : 0;
        const warningQuantityVal: Object = itemObj['warningQuantity'];
        item.warningQuantity = warningQuantityVal !== undefined && warningQuantityVal !== null ? Number(warningQuantityVal.toString()) : 0;
        const contraindicationVal: Object = itemObj['contraindication'];
        item.contraindication = contraindicationVal !== undefined && contraindicationVal !== null ? contraindicationVal.toString() : '';
        const statusVal: Object = itemObj['status'];
        item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
        items.push(item);
      }
      result.data = items;
    }
    return result;
  }

  static async getDrugDetail(drugId: number): Promise<Result<Drug>> {
    const url: string = ApiConfig.BASE_URL + ApiConfig.DRUG_DETAIL + drugId.toString();
    const response: string = await HttpClient.get(url);
    const parsed: object = JSON.parse(response);
    const result: Result<Drug> = new Result<Drug>();
    const successVal: Object = parsed['success'];
    result.success = successVal === true || successVal === 'true';
    const msgVal: Object = parsed['message'];
    result.message = msgVal !== undefined && msgVal !== null ? msgVal.toString() : '';
    if (result.success) {
      const itemObj: object = parsed['data'];
      const item: Drug = new Drug();
      const drugIdVal: Object = itemObj['drugId'];
      item.drugId = drugIdVal !== undefined && drugIdVal !== null ? Number(drugIdVal.toString()) : 0;
      const drugCodeVal: Object = itemObj['drugCode'];
      item.drugCode = drugCodeVal !== undefined && drugCodeVal !== null ? drugCodeVal.toString() : '';
      const drugNameVal: Object = itemObj['drugName'];
      item.drugName = drugNameVal !== undefined && drugNameVal !== null ? drugNameVal.toString() : '';
      const specificationVal: Object = itemObj['specification'];
      item.specification = specificationVal !== undefined && specificationVal !== null ? specificationVal.toString() : '';
      const dosageFormVal: Object = itemObj['dosageForm'];
      item.dosageForm = dosageFormVal !== undefined && dosageFormVal !== null ? dosageFormVal.toString() : '';
      const manufacturerVal: Object = itemObj['manufacturer'];
      item.manufacturer = manufacturerVal !== undefined && manufacturerVal !== null ? manufacturerVal.toString() : '';
      const unitVal: Object = itemObj['unit'];
      item.unit = unitVal !== undefined && unitVal !== null ? unitVal.toString() : '';
      const salePriceVal: Object = itemObj['salePrice'];
      item.salePrice = salePriceVal !== undefined && salePriceVal !== null ? Number(salePriceVal.toString()) : 0;
      const stockQuantityVal: Object = itemObj['stockQuantity'];
      item.stockQuantity = stockQuantityVal !== undefined && stockQuantityVal !== null ? Number(stockQuantityVal.toString()) : 0;
      const warningQuantityVal: Object = itemObj['warningQuantity'];
      item.warningQuantity = warningQuantityVal !== undefined && warningQuantityVal !== null ? Number(warningQuantityVal.toString()) : 0;
      const contraindicationVal: Object = itemObj['contraindication'];
      item.contraindication = contraindicationVal !== undefined && contraindicationVal !== null ? contraindicationVal.toString() : '';
      const statusVal: Object = itemObj['status'];
      item.status = statusVal !== undefined && statusVal !== null ? statusVal.toString() : '';
      result.data = item;
    }
    return result;
  }
}
