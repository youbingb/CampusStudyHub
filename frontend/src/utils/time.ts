import dayjs from 'dayjs'

export function fmt(d: string | Date | null | undefined, pattern = 'YYYY-MM-DD HH:mm'): string {
  if (!d) return '-'
  return dayjs(d).format(pattern)
}

export function fmtDate(d: string | Date | null | undefined): string {
  return fmt(d, 'YYYY-MM-DD')
}

export function fmtTime(d: string | Date | null | undefined): string {
  return fmt(d, 'HH:mm')
}

export function isoNow(): string {
  return dayjs().format('YYYY-MM-DD HH:mm:ss')
}
