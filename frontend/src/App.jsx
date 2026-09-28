import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Provider } from 'react-redux';
import store from './redux/store';
import Login from './components/Auth/Login';
import PrivateRoute from './components/Auth/PrivateRoute';
import MainLayout from './components/Layout/MainLayout';
import Dashboard from './components/Dashboard/Dashboard';
import IncidentList from './components/Incidents/IncidentList';
import IncidentDetail from './components/Incidents/IncidentDetail';
import IncidentForm from './components/Incidents/IncidentForm';
import FileUpload from './components/Upload/FileUpload';
import UserList from './components/Users/UserList';

function App() {
  return (
    <Provider store={store}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          
          <Route element={<PrivateRoute />}>
            <Route element={<MainLayout />}>
              <Route path="/" element={<Dashboard />} />
              <Route path="/incidents" element={<IncidentList />} />
              <Route path="/incidents/:id" element={<IncidentDetail />} />
              <Route path="/incidents/:id/edit" element={<IncidentForm />} />
              <Route path="/incidents/new" element={<IncidentForm />} />
              <Route path="/upload" element={<FileUpload />} />
              <Route 
                path="/users" 
                element={<PrivateRoute allowedRoles={['ADMIN']} />}
              >
                <Route index element={<UserList />} />
              </Route>
              <Route path="/statistics" element={<Dashboard />} />
            </Route>
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </Provider>
  );
}

export default App;