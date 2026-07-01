export const patients = [
  { id: '1000013', name: '梦琪', age: 23, gender: '女', status: '医生接诊', registerTime: '2022-05-18 10:48:18' },
  { id: '1000014', name: '张伟', age: 35, gender: '男', status: '等待就诊', registerTime: '2022-05-18 11:05:22' },
  { id: '1000015', name: '李纳', age: 28, gender: '女', status: '医生接诊', registerTime: '2022-05-18 11:15:41' },
  { id: '1000016', name: '王强', age: 42, gender: '男', status: '等待就诊', registerTime: '2022-05-18 11:25:41' }
];

export const medicalRecords = {
  '1000013': {
    chiefComplaint: '咳嗽、发热 2 天',
    presentIllness: '患者 2 天前无明显诱因出现咳嗽、发热，体温最高 38.5℃，伴少量白痰。',
    currentTreatment: '自行服用退烧药后体温下降，咳嗽仍反复。',
    pastHistory: '既往体健，无慢性病史。',
    allergyHistory: '否认药物及食物过敏史。',
    physicalExam: 'T 38.3℃，P 88 次/分，R 20 次/分，双肺呼吸音粗，未闻及明显湿啰音。',
    diagnosisText: '社区获得性肺炎，考虑细菌感染可能大。',
    examSuggestion: '建议完善血常规、胸部影像及炎症指标检查，必要时复查。',
    notes: '注意休息，多饮水，避免受凉。'
  }
};

export const diagnosisLibrary = [
  { code: 'J18.901', name: '社区获得性肺炎', icd: 'J18.9', type: '初步诊断' },
  { code: 'J06.901', name: '急性上呼吸道感染', icd: 'J06.9', type: '初步诊断' },
  { code: 'J20.901', name: '急性支气管炎', icd: 'J20.9', type: '初步诊断' },
  { code: 'R50.901', name: '发热待查', icd: 'R50.9', type: '辅助诊断' }
];

export const examLibrarySource = [
  { code: 'JSDMZYS', name: '脊髓动脉造影术', spec: '江北', price: 31.08, feeType: '其他检查费' },
  { code: 'XG001', name: '胸部正位片', spec: '正位', price: 28, feeType: '检查费' },
  { code: 'CT002', name: '胸部CT平扫', spec: '平扫', price: 220, feeType: '检查费' },
  { code: 'US003', name: '腹部彩超', spec: '常规', price: 68, feeType: '检查费' }
];

export const labLibrarySource = [
  { code: 'LAB001', name: '血常规五分类', spec: '次', price: 18.5, feeType: '检验费' },
  { code: 'LAB002', name: 'C反应蛋白', spec: '次', price: 26, feeType: '检验费' },
  { code: 'LAB003', name: '肝功能全套', spec: '次', price: 52, feeType: '检验费' },
  { code: 'LAB004', name: '尿常规', spec: '次', price: 12, feeType: '检验费' }
];

export const drugLibrarySource = [
  { code: '86979474000395', name: '复方丹参滴丸', spec: '27mg*180粒/盒', price: 23.97, factory: '天士力制药集团' },
  { code: '86979474000955', name: '咽立爽口含滴丸', spec: '1g*1盒', price: 31.56, factory: '贵州黄果树立爽药业' },
  { code: '86979474002695', name: '冠心丹参滴丸', spec: '0.040g*180粒/盒', price: 12.92, factory: '中发实业集团' },
  { code: '86979474003146', name: '芪参益气滴丸', spec: '0.5g*15袋/盒', price: 7.83, factory: '天士力制药集团' }
];

export const requestDefaults = {
  '1000013': {
    examItems: [
      { code: 'JSDMZYS', name: '脊髓动脉造影术', spec: '江北', price: 31.08, feeType: '其他检查费' }
    ],
    labItems: [
      { code: 'LAB001', name: '血常规五分类', spec: '次', price: 18.5, feeType: '检验费' },
      { code: 'LAB002', name: 'C反应蛋白', spec: '次', price: 26, feeType: '检验费' }
    ],
    diagnosisText: '社区获得性肺炎，考虑细菌感染可能大。'
  },
  '1000014': {
    examItems: [],
    labItems: [],
    diagnosisText: ''
  },
  '1000015': {
    examItems: [
      { code: 'XG001', name: '胸部正位片', spec: '正位', price: 28, feeType: '检查费' }
    ],
    labItems: [
      { code: 'LAB004', name: '尿常规', spec: '次', price: 12, feeType: '检验费' }
    ],
    diagnosisText: '急性上呼吸道感染。'
  }
};
