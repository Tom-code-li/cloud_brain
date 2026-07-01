import { describe, expect, it } from 'vitest';
import {
  buildAiAssistantText,
  buildAiPanelSections,
  buildDoctorOpinionText,
  buildExamSuggestionText,
  buildRiskText,
  joinList,
  pickDiagnosisDraft,
  requireAiSuccessData
} from '../../src/utils/aiAssistant.js';

describe('aiAssistant helpers', () => {
  it('joins list values with ideographic commas', () => {
    expect(joinList(['血常规', '胸片'])).toBe('血常规、胸片');
    expect(joinList([])).toBe('');
  });

  it('prefers the AI diagnosis draft when present', () => {
    expect(pickDiagnosisDraft({ diagnosisDraft: '肺炎' }, '旧值')).toBe('肺炎');
    expect(pickDiagnosisDraft({}, '旧值')).toBe('旧值');
  });

  it('formats structured exam recommendations', () => {
    const text = buildExamSuggestionText({
      examRecommendations: [
        { type: '检查', name: '胸片', reason: '评估肺部感染' },
        { type: '检验', name: '血常规', reason: '判断感染程度' }
      ]
    });

    expect(text).toBe('检查：胸片（评估肺部感染）\n检验：血常规（判断感染程度）');
  });

  it('joins plain exam suggestion arrays with ideographic commas', () => {
    const text = buildExamSuggestionText({
      examSuggestions: ['血常规', '胸片']
    });

    expect(text).toBe('血常规、胸片');
  });

  it('builds a readable AI assistant summary', () => {
    const text = buildAiAssistantText({
      diagnosisDraft: '社区获得性肺炎',
      possibleDiagnoses: [{ name: '肺炎', reason: '发热伴咳嗽' }],
      examSuggestions: ['血常规', '胸片'],
      riskFlags: ['青霉素过敏'],
      evidence: ['发热伴咳嗽', '胸片提示炎症']
    });

    expect(text).toBe([
      '诊断建议：社区获得性肺炎',
      '可能诊断：肺炎（发热伴咳嗽）',
      '检查检验建议：血常规、胸片',
      '风险提示：青霉素过敏',
      '依据：发热伴咳嗽；胸片提示炎症'
    ].join('\n\n'));
  });

  it('builds doctor opinion text from plan, drugs, and risks', () => {
    const text = buildDoctorOpinionText({
      planDraft: '建议抗感染治疗',
      drugSuggestions: ['阿莫西林'],
      riskFlags: ['注意药物过敏']
    });

    expect(text).toBe([
      '处理建议：建议抗感染治疗',
      '处方建议：阿莫西林',
      '风险提示：注意药物过敏'
    ].join('\n\n'));
  });

  it('builds ai panel sections in diagnosis-plan-prescription-risk order', () => {
    const sections = buildAiPanelSections({
      diagnosisDraft: '肺炎',
      planDraft: '抗感染',
      drugSuggestions: ['阿莫西林'],
      riskFlags: ['药物过敏']
    });

    expect(sections.map((item) => item.key)).toEqual(['diagnosis', 'plan', 'prescription', 'risk']);
  });

  it('only emits ai panel sections when matching data exists', () => {
    const sections = buildAiPanelSections({
      planDraft: '抗感染',
      drugSuggestions: [],
      riskFlags: []
    });

    expect(sections).toEqual([
      {
        key: 'plan',
        title: 'AI 处置建议',
        content: '抗感染'
      }
    ]);
  });

  it('builds risk text only when risk flags exist', () => {
    expect(buildRiskText({ riskFlags: ['药物过敏'] })).toBe('注意事项：药物过敏');
    expect(buildRiskText({ riskFlags: [] })).toBe('');
  });

  it('rejects invalid AI responses', () => {
    expect(() => requireAiSuccessData()).toThrow('AI 接口无响应数据');
    expect(() => requireAiSuccessData({ data: { code: 1, message: 'bad' } })).toThrow('bad');
    expect(() => requireAiSuccessData({ data: { code: 0, data: null } })).toThrow('AI 接口未返回建议内容');
  });

  it('returns successful AI response data', () => {
    expect(requireAiSuccessData({
      data: {
        code: 0,
        data: {
          diagnosisDraft: '社区获得性肺炎'
        }
      }
    })).toEqual({
      diagnosisDraft: '社区获得性肺炎'
    });
  });
});
