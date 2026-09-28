import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams } from 'react-router-dom';
import { fetchIncidentById, resolveIncident } from '../../redux/slices/incidentSlice';
import { ArrowLeft, Edit, Trash2, UserCheck, CheckCircle, Plus } from 'lucide-react';
import AssignModal from './AssignModal';
import SolutionList from '../Solutions/SolutionList';
import SolutionForm from '../Solutions/SolutionForm';
import LoadingSpinner from '../Common/LoadingSpinner';
import ConfirmDialog from '../Common/ConfirmDialog';
import { formatDate, formatAmount, getIncidentTypeLabel, getStatusLabel, getPriorityLabel } from '../../utils/formatters';
import { STATUS_COLORS, PRIORITY_COLORS } from '../../utils/constants';



const IncidentDetail = () => {
  const { id } = useParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { currentIncident: incident, loading } = useSelector((state) => state.incidents);
  const { user } = useSelector((state) => state.auth);
  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [solutionFormOpen, setSolutionFormOpen] = useState(false);
  const [resolveDialogOpen, setResolveDialogOpen] = useState(false);

  useEffect(() => {
    dispatch(fetchIncidentById(id));
    console.log('Fetching incident:', incident);
  }, [dispatch, id]);

  const handleResolve = async () => {
    await dispatch(resolveIncident(id)).unwrap();
    dispatch(fetchIncidentById(id));
  };

  const canAssign = user?.role === 'ADMIN' || user?.role === 'SUPERVISEUR';
  const canResolve = incident?.assignedToId === user?.userId && incident?.status === 'EN_COURS';
  const canEdit = user?.role === 'ADMIN' || user?.role === 'SUPERVISEUR';

  if (loading || !incident) {
    return <LoadingSpinner />;
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          <button
            onClick={() => navigate('/incidents')}
            className="p-2 text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg"
          >
            <ArrowLeft className="h-5 w-5" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Incident #{incident.id}</h1>
            <p className="text-gray-600 mt-1">{getIncidentTypeLabel(incident.incidentType)}</p>
          </div>
        </div>

        <div className="flex space-x-2">
          {canAssign && incident.status !== 'FERME' && (
            <button
              onClick={() => setAssignModalOpen(true)}
              className="px-4 py-2 border border-blue-600 text-blue-600 rounded-lg hover:bg-blue-50 flex items-center"
            >
              <UserCheck className="h-5 w-5 mr-2" />
              Assigner
            </button>
          )}
          {canResolve && (
            <button
              onClick={() => setResolveDialogOpen(true)}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 flex items-center"
            >
              <CheckCircle className="h-5 w-5 mr-2" />
              Résoudre
            </button>
          )}
          {canEdit && (
            <button
              onClick={() => navigate(`/incidents/${id}/edit`)}
              className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 flex items-center"
            >
              <Edit className="h-5 w-5 mr-2" />
              Modifier
            </button>
          )}
        </div>
      </div>

      {/* Informations principales */}
      <div className="bg-white rounded-lg shadow p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Informations</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="text-sm font-medium text-gray-500">Type</label>
            <p className="text-gray-900 mt-1">{getIncidentTypeLabel(incident.incidentType)}</p>
          </div>

          <div>
            <label className="text-sm font-medium text-gray-500">Statut</label>
            <div className="mt-1">
              <span className={`px-3 py-1 text-sm font-semibold rounded-full ${STATUS_COLORS[incident.status]}`}>
                {getStatusLabel(incident.status)}
              </span>
            </div>
          </div>

          <div>
            <label className="text-sm font-medium text-gray-500">Priorité</label>
            <div className="mt-1">
              <span className={`px-3 py-1 text-sm font-semibold rounded-full ${PRIORITY_COLORS[incident.priority]}`}>
                {getPriorityLabel(incident.priority)}
              </span>
            </div>
          </div>

          <div>
            <label className="text-sm font-medium text-gray-500">Numéro de ligne</label>
            <p className="text-gray-900 mt-1">{incident.lineNumber}</p>
          </div>

          <div>
            <label className="text-sm font-medium text-gray-500">Référence facture</label>
            <p className="text-gray-900 mt-1">{incident.referenceFacture || '-'}</p>
          </div>

          <div>
            <label className="text-sm font-medium text-gray-500">ID Client</label>
            <p className="text-gray-900 mt-1">{incident.clientId || '-'}</p>
          </div>

          <div>
            <label className="text-sm font-medium text-gray-500">Montant erreur</label>
            <p className="text-gray-900 mt-1">{formatAmount(incident.montantErreur)}</p>
          </div>

          <div>
            <label className="text-sm font-medium text-gray-500">Fichier source</label>
            <p className="text-gray-900 mt-1">{incident.sourceFile || '-'}</p>
          </div>

          <div>
            <label className="text-sm font-medium text-gray-500">Assigné à</label>
            <p className="text-gray-900 mt-1">{incident.assignedToUsername || 'Non assigné'}</p>
          </div>

          <div>
            <label className="text-sm font-medium text-gray-500">Créé par</label>
            <p className="text-gray-900 mt-1">{incident.createdByUsername || '-'}</p>
          </div>

          <div>
            <label className="text-sm font-medium text-gray-500">Date création</label>
            <p className="text-gray-900 mt-1">{formatDate(incident.createdAt)}</p>
          </div>

          <div>
            <label className="text-sm font-medium text-gray-500">Dernière mise à jour</label>
            <p className="text-gray-900 mt-1">{formatDate(incident.updatedAt)}</p>
          </div>
        </div>

        <div className="mt-6">
          <label className="text-sm font-medium text-gray-500">Description</label>
          <p className="text-gray-900 mt-2 whitespace-pre-wrap">{incident.description}</p>
        </div>
      </div>

      {/* Solutions */}
      <div className="bg-white rounded-lg shadow p-6">
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-lg font-semibold text-gray-900">Solutions</h2>
          <button
            onClick={() => setSolutionFormOpen(true)}
            className="px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center text-sm"
          >
            <Plus className="h-4 w-4 mr-2" />
            Ajouter une solution
          </button>
        </div>
        <SolutionList incidentId={incident.id} />
      </div>

      {/* Modals */}
      <AssignModal
        isOpen={assignModalOpen}
        onClose={() => setAssignModalOpen(false)}
        incident={incident}
        onSuccess={() => dispatch(fetchIncidentById(id))}
      />

      <SolutionForm
        isOpen={solutionFormOpen}
        onClose={() => setSolutionFormOpen(false)}
        incidentId={incident.id}
        onSuccess={() => {
          setSolutionFormOpen(false);
          dispatch(fetchIncidentById(id));
        }}
      />

      <ConfirmDialog
        isOpen={resolveDialogOpen}
        onClose={() => setResolveDialogOpen(false)}
        onConfirm={handleResolve}
        title="Résoudre l'incident"
        message="Êtes-vous sûr de vouloir marquer cet incident comme résolu ?"
        confirmText="Résoudre"
      />
    </div>
  );
};

export default IncidentDetail;