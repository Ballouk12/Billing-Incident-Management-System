import api from './api';

export const getUsers = async (params) => {
  const response = await api.get('/users', { params });
  return response.data.data;
};

export const getUserById = async (id) => {
  const response = await api.get(`/users/${id}`);
  return response.data;
};

export const createUser = async (data) => {
  const response = await api.post('/users', data);
  return response.data;
};

export const updateUser = async (id, data) => {
  const response = await api.put(`/users/${id}`, data);
  return response.data;
};

export const deleteUser = async (id) => {
  const response = await api.delete(`/users/${id}`);
  return response.data;
};

export const getTechnicians = async () => {
  const response = await api.get('/users/technicians');
  return response.data.data;
};

export const activateUser = async (id) => {
  const response = await api.post(`/users/${id}/activate`);
  return response.data;
};

export const deactivateUser = async (id) => {
  const response = await api.post(`/users/${id}/deactivate`);
  return response.data;
};

export const resetPassword = async (id) => {
  const response = await api.post(`/users/${id}/reset-password`);
  return response.data;
};