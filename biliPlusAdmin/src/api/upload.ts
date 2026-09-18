import service from '@/utils/request'

export const uploadImage = async (file: File): Promise<string> => {
  const formData = new FormData()
  formData.append('file', file)
  const res: any = await service.post('/admin/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
  return res?.data || ''
}
