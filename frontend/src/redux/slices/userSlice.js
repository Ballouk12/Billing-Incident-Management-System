import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import * as userService from '../../services/userService';

// Thunks
export const fetchUsers = createAsyncThunk(
  'users/fetchAll',
  async (params, { rejectWithValue }) => {
    try {
      return await userService.getUsers(params);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors du chargement');
    }
  }
);

export const fetchUserById = createAsyncThunk(
  'users/fetchById',
  async (id, { rejectWithValue }) => {
    try {
      return await userService.getUserById(id);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors du chargement');
    }
  }
);

export const createUser = createAsyncThunk(
  'users/create',
  async (data, { rejectWithValue }) => {
    try {
      return await userService.createUser(data);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors de la création');
    }
  }
);

export const updateUser = createAsyncThunk(
  'users/update',
  async ({ id, data }, { rejectWithValue }) => {
    try {
      return await userService.updateUser(id, data);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors de la mise à jour');
    }
  }
);

export const deleteUser = createAsyncThunk(
  'users/delete',
  async (id, { rejectWithValue }) => {
    try {
      await userService.deleteUser(id);
      return id;
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors de la suppression');
    }
  }
);

export const fetchTechnicians = createAsyncThunk(
  'users/fetchTechnicians',
  async (_, { rejectWithValue }) => {
    try {
      return await userService.getTechnicians();
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors du chargement');
    }
  }
);

export const activateUser = createAsyncThunk(
  'users/activate',
  async (id, { rejectWithValue }) => {
    try {
      return await userService.activateUser(id);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors de l\'activation');
    }
  }
);

export const deactivateUser = createAsyncThunk(
  'users/deactivate',
  async (id, { rejectWithValue }) => {
    try {
      return await userService.deactivateUser(id);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors de la désactivation');
    }
  }
);

export const resetPassword = createAsyncThunk(
  'users/resetPassword',
  async (id, { rejectWithValue }) => {
    try {
      return await userService.resetPassword(id);
    } catch (error) {
      return rejectWithValue(error.response?.data?.message || 'Erreur lors de la réinitialisation');
    }
  }
);

const initialState = {
  users: [],
  technicians: [],
  currentUser: null,
  pagination: {
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0
  },
  loading: false,
  error: null
};

const userSlice = createSlice({
  name: 'users',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null;
    }
  },
  extraReducers: (builder) => {
    builder
      // Fetch users
      .addCase(fetchUsers.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUsers.fulfilled, (state, action) => {
        state.loading = false;
        state.users = action.payload.content;
        state.pagination = {
          page: action.payload.number,
          size: action.payload.size,
          totalElements: action.payload.totalElements,
          totalPages: action.payload.totalPages
        };
      })
      .addCase(fetchUsers.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Fetch by ID
      .addCase(fetchUserById.fulfilled, (state, action) => {
        state.currentUser = action.payload;
      })
      // Create
      .addCase(createUser.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createUser.fulfilled, (state) => {
        state.loading = false;
      })
      .addCase(createUser.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      // Update
      .addCase(updateUser.fulfilled, (state, action) => {
        state.currentUser = action.payload;
      })
      // Delete
      .addCase(deleteUser.fulfilled, (state, action) => {
        state.users = state.users.filter(user => user.id !== action.payload);
      })
      // Fetch technicians
      .addCase(fetchTechnicians.fulfilled, (state, action) => {
        state.technicians = action.payload;
      });
  }
});

export const { clearError } = userSlice.actions;
export default userSlice.reducer;