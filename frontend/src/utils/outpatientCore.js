export function filterPatients(list, criteria = {}) {
  const medicalNo = (criteria.medicalNo || '').trim();
  const name = (criteria.name || '').trim();

  return list.filter((patient) => {
    const medicalMatched = !medicalNo || patient.id.includes(medicalNo);
    const nameMatched = !name || patient.name.includes(name);
    return medicalMatched && nameMatched;
  });
}

export function sumItemPrices(items) {
  return Number(items.reduce((sum, item) => sum + Number(item.price || 0), 0).toFixed(2));
}

export function appendUniqueByCode(items, nextItem) {
  if (items.some((item) => item.code === nextItem.code)) {
    return items;
  }

  return [...items, { ...nextItem }];
}

export function removeBySelectedCodes(items, selectedItems) {
  const selectedCodes = new Set(selectedItems.map((item) => item.code));
  return items.filter((item) => !selectedCodes.has(item.code));
}

export function clone(value) {
  return JSON.parse(JSON.stringify(value));
}
