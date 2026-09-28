import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as incidentService from '../../services/incidentService';

// Thunks
export const fetchIncidents = createAsyncThunk(
  'incidents/fetchAll',
  async (params, { rejectWithValue }) => {
    try {
      return await incidentService.getIncidents(params);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors du chargement');
    }
  }
);

export const fetchIncidentById = createAsyncThunk(
  'incidents/fetchById',
  async (id, { rejectWithValue }) => {
    try {
      return await incidentService.getIncidentById(id);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors du chargement');
    }
  }
);

export const createIncident = createAsyncThunk(
  'incidents/create',
  async (data, { rejectWithValue }) => {
    try {
      return await incidentService.createIncident(data);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors de la création');
    }
  }
);

export const updateIncident = createAsyncThunk(
  'incidents/update',
  async ({ id, data }, { rejectWithValue }) => {
    try {
      return await incidentService.updateIncident(id, data);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors de la mise à jour');
    }
  }
);

export const deleteIncident = createAsyncThunk(
  'incidents/delete',
  async (id, { rejectWithValue }) => {
    try {
      await incidentService.deleteIncident(id);
      return id;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors de la suppression');
    }
  }
);

export const searchIncidents = createAsyncThunk(
  'incidents/search',
  async (params, { rejectWithValue }) => {
    try {
      return await incidentService.searchIncidents(params);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors de la recherche');
    }
  }
);

export const assignIncident = createAsyncThunk(
  'incidents/assign',
  async ({ id, userId }, { rejectWithValue }) => {
    try {
      return await incidentService.assignIncident(id, userId);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors de l\'assignation');
    }
  }
);

export const resolveIncident = createAsyncThunk(
  'incidents/resolve',
  async (id, { rejectWithValue }) => {
    try {
      return await incidentService.resolveIncident(id);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors de la résolution');
    }
  }
);

export const fetchStatistics = createAsyncThunk(
  'incidents/statistics',
  async (_, { rejectWithValue }) => {
    try {
      return await incidentService.getStatistics();
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors du chargement des statistiques');
    }
  }
);

const initialState = {
  incidents: [],
  currentIncident: null,
  pagination: {
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0
  },
  filters: {
    incidentType: '',
    status: '',
    priority: '',
    assignedToId: '',
    startDate: '',
    endDate: '',
    searchText: ''
  },
  statistics: null,
  loading: false,
  error: null
};

const incidentSlice = createSlice({
  name: 'incidents',
  initialState,
  reducers: {
    setFilters: (state, action) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    clearFilters: (state) => {
      state.filters = initialState.filters;
    },
    clearError: (state) => {
      state.error = null;
    }
  },
  extraReducers: (builder) => {
    builder
      // Fetch incidents
      .addCase(fetchIncidents.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchIncidents.fulfilled, (state, action) => {
        state.loading = false;
        state.incidents = action.payload.content;
        state.pagination = {
          page: action.payload.number,
          size: action.payload.size,
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages
        };
      })
      .addCase(fetchIncidents.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Fetch by ID
      .addCase(fetchIncidentById.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchIncidentById.fulfilled, (state, action) => {
        state.loading = false;
        state.currentIncident = action.payload;
      })
      .addCase(fetchIncidentById.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Create
      .addCase(createIncident.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createIncident.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(createIncident.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Update
      .addCase(updateIncident.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateIncident.fulfilled, (state, action) => {
        state.loading = false;
        state.currentIncident = action.payload;
      })
      .addCase(updateIncident.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Delete
      .addCase(deleteIncident.fulfilled, (state, action) => {
        state.incidents = state.incidents.filter(inc => inc.id !== action.payload);
      })
      // Search
      .addCase(searchIncidents.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(searchIncidents.fulfilled, (state, action) => {
        state.loading = false;
        state.incidents = action.payload.content;
        state.pagination = {
          page: action.payload.number,
          size: action.payload.size,
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages
        };
      })
      .addCase(searchIncidents.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Assign
      .addCase(assignIncident.fulfilled, (state, action) => {
        if (state.currentIncident?.id === action.payload.id) {
          state.currentIncident = action.payload;
        }
      })
      // Resolve
      .addCase(resolveIncident.fulfilled, (state, action) => {
        if (state.currentIncident?.id === action.payload.id) {
          state.currentIncident = action.payload;
        }
      })
      // Statistics
      .addCase(fetchStatistics.fulfilled, (state, action) => {
        state.statistics = action.payload;
      });
  }
});

export const { setFilters, clearFilters, clearError } = incidentSlice.actions;
export default incidentSlice.reducer;