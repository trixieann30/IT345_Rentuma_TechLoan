import api from '../../shared/api'

export const inventoryService = {
  getAvailable:  ()               => api.get('/inventory/available'),
  getAll:        ()               => api.get('/inventory/all'),
  getById:       (id)             => api.get(`/inventory/${id}`),
  getByCategory: (category)       => api.get(`/inventory/category/${category}`),
  search:        (query)          => api.get('/inventory/search', { params: { query } }),
  getCategories: ()               => api.get('/inventory/categories'),

  createItem: (data)      => api.post('/inventory', data),
  updateItem: (id, data)  => api.put(`/inventory/${id}`, data),
  deleteItem: (id)        => api.delete(`/inventory/${id}`),

  uploadImage: (id, file) => {
    const formData = new FormData()
    formData.append('image', file)
    return api.post(`/inventory/${id}/upload-image`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
