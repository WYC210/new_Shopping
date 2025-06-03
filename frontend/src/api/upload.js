import apiClient from './client'

export const uploadApi = {
  // 上传图片
  uploadImage(file, type = 'image') {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('type', type)
    return apiClient.post('/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 上传头像
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient.post('/upload/avatar', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 上传商品图片
  uploadProductImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient.post('/upload/product', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 上传评价图片
  uploadReviewImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return apiClient.post('/upload/review', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
} 