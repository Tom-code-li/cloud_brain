export class AiConsultResponse {
  mode: string = '';
  summary: string = '';
  riskLevel: string = '';
  recommendedDept: string = '';
  recommendedDoctor: string = '';
  candidates: Array<string> = [];
  aiResult: string = '';
  note: string = '';
}
