export function joinList(items = []) {
  return Array.isArray(items) && items.length > 0 ? items.join('、') : '';
}

export function pickDiagnosisDraft(suggestion = {}, fallback = '') {
  return suggestion.diagnosisDraft || fallback;
}

export function buildExamSuggestionText(suggestion = {}) {
  if (Array.isArray(suggestion.examRecommendations) && suggestion.examRecommendations.length > 0) {
    return suggestion.examRecommendations
      .map((item) => `${item.type}：${item.name}（${item.reason}）`)
      .join('\n');
  }
  return joinList(suggestion.examSuggestions);
}

export function buildAiAssistantText(suggestion = {}) {
  const lines = [];

  if (suggestion.diagnosisDraft) {
    lines.push(`诊断建议：${suggestion.diagnosisDraft}`);
  }
  if (Array.isArray(suggestion.possibleDiagnoses) && suggestion.possibleDiagnoses.length > 0) {
    lines.push(`可能诊断：${suggestion.possibleDiagnoses.map((item) => `${item.name}（${item.reason}）`).join('；')}`);
  }
  const examText = buildExamSuggestionText(suggestion);
  if (examText) {
    lines.push(`检查检验建议：${examText}`);
  }
  const riskText = joinList(suggestion.riskFlags);
  if (riskText) {
    lines.push(`风险提示：${riskText}`);
  }
  if (Array.isArray(suggestion.evidence) && suggestion.evidence.length > 0) {
    lines.push(`依据：${suggestion.evidence.join('；')}`);
  }

  return lines.join('\n\n');
}

export function buildDoctorOpinionText(suggestion = {}) {
  const lines = [];

  if (suggestion.planDraft) {
    lines.push(`处理建议：${suggestion.planDraft}`);
  }
  const drugText = joinList(suggestion.drugSuggestions);
  if (drugText) {
    lines.push(`处方建议：${drugText}`);
  }
  const riskText = joinList(suggestion.riskFlags);
  if (riskText) {
    lines.push(`风险提示：${riskText}`);
  }

  return lines.join('\n\n');
}

export function buildPrescriptionSuggestionText(suggestion = {}) {
  const drugText = joinList(suggestion.drugSuggestions);
  return drugText ? `处方建议：${drugText}` : '';
}

export function buildRiskText(suggestion = {}) {
  const riskText = joinList(suggestion.riskFlags);
  return riskText ? `注意事项：${riskText}` : '';
}

export function buildAiPanelSections(suggestion = {}) {
  const sections = [];

  if (suggestion.diagnosisDraft) {
    sections.push({
      key: 'diagnosis',
      title: 'AI 诊断结果',
      content: suggestion.diagnosisDraft
    });
  }
  if (suggestion.planDraft) {
    sections.push({
      key: 'plan',
      title: 'AI 处置建议',
      content: suggestion.planDraft
    });
  }
  if (Array.isArray(suggestion.drugSuggestions) && suggestion.drugSuggestions.length > 0) {
    sections.push({
      key: 'prescription',
      title: 'AI 处方建议',
      content: joinList(suggestion.drugSuggestions)
    });
  }
  if (Array.isArray(suggestion.riskFlags) && suggestion.riskFlags.length > 0) {
    sections.push({
      key: 'risk',
      title: 'AI 注意事项',
      content: joinList(suggestion.riskFlags)
    });
  }

  return sections;
}

export function requireAiSuccessData(response) {
  const payload = response?.data;
  if (!payload) {
    throw new Error('AI 接口无响应数据');
  }
  if (payload.code !== 0) {
    throw new Error(payload.message || 'AI 接口调用失败');
  }
  if (!payload.data) {
    throw new Error('AI 接口未返回建议内容');
  }
  return payload.data;
}
