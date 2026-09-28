import api from './api';

export const getIncidents = async (params) => {
  const response = await api.get('/incidents', { params });
  console.log('Fetched incidents:', response.data.data);
  return response.data.data;
};

export const getIncidentById = async (id) => {
  const response = await api.get(`/incidents/${id}`);
  console.log('Fetched incident by ID:', response.data.data);
  return response.data.data;
};

export const createIncident = async (data) => {
  const response = await api.post('/incidents', data);
  return response.data;
};

export const updateIncident = async (id, data) => {
  const response = await api.put(`/incidents/${id}`, data);
  return response.data;
};

export const deleteIncident = async (id) => {
  const response = await api.delete(`/incidents/${id}`);
  return response.data;
};

export const searchIncidents = async (params) => {
  const response = await api.get('/incidents/search', { params });
  return response.data;
};

export const assignIncident = async (id, userId) => {
  const response = await api.post(`/incidents/${id}/assign?userId=${userId}`);
  return response.data;
};

export const resolveIncident = async (id) => {
  const response = await api.post(`/incidents/${id}/resolve`);
  return response.data;
};

export const getStatistics = async () => {
  const response = await api.get('/incidents/statistics');
  console.log('Fetched incident statistics:', response.data);
  return response.data.data;
};

export const getSolutions = async (incidentId) => {
  const response = await api.get(`/incidents/${incidentId}/solutions`);
  console.log('Fetched solutions for incident', incidentId, ':', response.data)
  return response.data.data;
};

export const createSolution = async (incidentId, data) => {
    console.log('Creating solution for incident', incidentId, ':', data);
  const response = await api.post(`/incidents/${incidentId}/solutions`, data);
  return response.data;
};