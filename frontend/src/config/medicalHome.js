export const medicalHomeDiagnosisLayout = {
  editor: {
    kind: 'textarea',
    rows: 10,
    placeholder: '请输入诊断文本，支持自由描述诊断结论、分型和处理建议'
  },
  assistant: {
    kind: 'compact-ai',
    width: 'narrow',
    title: 'AI 助手',
    tips: [
      '可根据主诉和体检信息生成诊断建议',
      '可一键补全常见诊断表达',
      '可提示检查/检验建议'
    ]
  }
};
