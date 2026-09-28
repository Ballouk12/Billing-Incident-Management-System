import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchIncidents, searchIncidents, deleteIncident } from '../../redux/slices/incidentSlice';
import { Plus, Eye, Edit, Trash2 } from 'lucide-react';
import IncidentFilters from './IncidentFilters';
import Pagination from '../Common/Pagination';
import LoadingSpinner from '../Common/LoadingSpinner';
import ConfirmDialog from '../Common/ConfirmDialog';
import { formatDate, truncateText, getIncidentTypeLabel, getStatusLabel, getPriorityLabel } from '../../utils/formatters';
import { STATUS_COLORS, PRIORITY_COLORS } from '../../utils/constants';

const IncidentList = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { incidents, pagination, loading } = useSelector((state) => state.incidents);
  const { user } = useSelector((state) => state.auth);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [incidentToDelete, setIncidentToDelete] = useState(null);
  const [currentFilters, setCurrentFilters] = useState(null);

  useEffect(() => {
    dispatch(fetchIncidents({ page: 0, size: 20, sortBy: 'createdAt', sortDir: 'DESC' }));
  }, [dispatch]);

  const handleFilter = (filters) => {
    setCurrentFilters(filters);
    dispatch(searchIncidents({ ...filters, page: 0, size: 20 }));
  };

  const handleClearFilters = () => {
    setCurrentFilters(null);
    dispatch(fetchIncidents({ page: 0, size: 20, sortBy: 'createdAt', sortDir: 'DESC' }));
  };

  const handlePageChange = (page) => {
    if (currentFilters) {
      dispatch(searchIncidents({ ...currentFilters, page, size: 20 }));
    } else {
      dispatch(fetchIncidents({ page, size: 20, sortBy: 'createdAt', sortDir: 'DESC' }));
    }
  };

  const handleDelete = (incident) => {
    setIncidentToDelete(incident);
    setDeleteDialogOpen(true);
  };

  const confirmDelete = () => {
    if (incidentToDelete) {
      dispatch(deleteIncident(incidentToDelete.id));
    }
  };

  const canCreateOrDelete = user?.role === 'ADMIN' || user?.role === 'SUPERVISEUR';

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Incidents</h1>
          <p className="text-gray-600 mt-1">Gérez tous vos incidents de facturation</p>
        </div>
        {canCreateOrDelete && (
          <button
            onClick={() => navigate('/incidents/new')}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center"
          >
            <Plus className="h-5 w-5 mr-2" />
            Nouvel incident
          </button>
        )}
      </div>

      <IncidentFilters onFilter={handleFilter} onClear={handleClearFilters} />

      <div className="bg-white rounded-lg shadow overflow-hidden">
        {loading ? (
          <LoadingSpinner />
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      ID
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Type
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Description
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Priorité
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Statut
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Assigné à
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Date création
                    </th>
                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Actions
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {incidents?.length === 0 ? (
                    <tr>
                      <td colSpan="8" className="px-6 py-8 text-center text-gray-500">
                        Aucun incident trouvé
                      </td>
                    </tr>
                  ) : (
                    incidents.map((incident) => (
                      <tr key={incident.id} className="hover:bg-gray-50">
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          #{incident.id}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {getIncidentTypeLabel(incident.incidentType)}
                        </td>
                        <td className="px-6 py-4 text-sm text-gray-900">
                          {truncateText(incident.description, 50)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`px-2 py-1 text-xs font-semibold rounded-full ${PRIORITY_COLORS[incident.priority]}`}>
                            {getPriorityLabel(incident.priority)}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <span className={`px-2 py-1 text-xs font-semibold rounded-full ${STATUS_COLORS[incident.status]}`}>
                            {getStatusLabel(incident.status)}
                          </span>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                          {incident.assignedToUsername || '-'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {formatDate(incident.createdAt)}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <div className="flex justify-end space-x-2">
                            <button
                              onClick={() => navigate(`/incidents/${incident.id}`)}
                              className="text-blue-600 hover:text-blue-900"
                              title="Voir détails"
                            >
                              <Eye className="h-5 w-5" />
                            </button>
                            {canCreateOrDelete && (
                              <>
                                <button
                                  onClick={() => navigate(`/incidents/${incident.id}/edit`)}
                                  className="text-green-600 hover:text-green-900"
                                  title="Modifier"
                                >
                                  <Edit className="h-5 w-5" />
                                </button>
                                <button
                                  onClick={() => handleDelete(incident)}
                                  className="text-red-600 hover:text-red-900"
                                  title="Supprimer"
                                >
                                  <Trash2 className="h-5 w-5" />
                                </button>
                              </>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {pagination.totalPages > 1 && (
              <Pagination
                currentPage={pagination.page}
                totalPages={pagination.totalPages}
                onPageChange={handlePageChange}
              />
            )}
          </>
        )}
      </div>

      <ConfirmDialog
        isOpen={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        onConfirm={confirmDelete}
        title="Confirmer la suppression"
        message={`Êtes-vous sûr de vouloir supprimer l'incident #${incidentToDelete?.id} ? Cette action est irréversible.`}
        confirmText="Supprimer"
      />
    </div>
  );
};

export default IncidentList;