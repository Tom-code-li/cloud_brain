export function fmtTime(str) {
  if (!str) return '--';
  return str.replace('T', ' ').slice(0, 16);
}

export function calcAge(birthday) {
  if (!birthday) return '--';
  const today = new Date();
  const date = new Date(birthday);
  let age = today.getFullYear() - date.getFullYear();
  const monthOffset = today.getMonth() - date.getMonth();
  if (monthOffset < 0 || (monthOffset === 0 && today.getDate() < date.getDate())) {
    age -= 1;
  }
  return age;
}

export const fmtAge = calcAge;
