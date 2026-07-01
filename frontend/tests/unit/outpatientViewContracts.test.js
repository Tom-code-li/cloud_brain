import { describe, expect, it } from 'vitest';
import fs from 'node:fs';

function read(path) {
  return fs.readFileSync(path, 'utf8');
}

describe('outpatient view contracts', () => {
  it('patient view starts an encounter before navigating', () => {
    const source = read('src/views/PatientView.vue');
    expect(source).toContain('startEncounter');
    expect(source).toContain("visitStatus: '待接诊'");
  });

  it('medical home uses encounter tabs and ai helper utilities', () => {
    const source = read('src/views/MedicalHomeView.vue');
    expect(source).toContain('EncounterTabs');
    expect(source).toContain('buildAiAssistantText');
    expect(source).toContain('requireAiSuccessData');
  });

  it('exam request supports skip logic and submitted order details', () => {
    const source = read('src/views/ExamRequestView.vue');
    expect(source).toContain('skipExam');
    expect(source).toContain('RequestOrderDetailDialog');
    expect(source).toContain("loadRequestDraft('exam')");
  });

  it('lab request supports skip logic and submitted order details', () => {
    const source = read('src/views/LabRequestView.vue');
    expect(source).toContain('skipLab');
    expect(source).toContain('RequestOrderDetailDialog');
    expect(source).toContain("loadRequestDraft('lab')");
  });

  it('report detail marks reviewed before diagnosis routing', () => {
    const source = read('src/views/AiReportDetailView.vue');
    expect(source).toContain('markExamLabReportReviewed');
    expect(source).toContain('buildAiPanelSections');
    expect(source).toContain("router.push('/diagnosis-detail')");
  });

  it('diagnosis detail saves final diagnosis', () => {
    const source = read('src/views/DiagnosisDetailView.vue');
    expect(source).toContain('saveFinalDiagnosis');
    expect(source).toContain('finalDiagnosis');
  });

  it('prescription detail submits dosage frequency and usage method', () => {
    const source = read('src/views/PrescriptionDetailView.vue');
    expect(source).toContain('dosage');
    expect(source).toContain('frequency');
    expect(source).toContain('usageMethod');
    expect(source).toContain('days');
  });

  it('fee query reads live fee orders for the current visit', () => {
    const source = read('src/views/FeeQueryView.vue');
    expect(source).toContain('fetchFeeOrders');
    expect(source).toContain('visitId');
  });
});
