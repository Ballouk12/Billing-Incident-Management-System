import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchTechnicians } from '../../redux/slices/userSlice';
import { assignIncident } from '../../redux/slices/incidentSlice';
import Modal from '../Common/Modal';
import { UserCheck } from 'lucide-react';
import LoadingSpinner from '../Common/LoadingSpinner';

const AssignModal = ({ isOpen, onClose, incident, onSuccess }) => {
  const dispatch = useDispatch();
  const { technicians } = useSelector((state) => state.users);
  const [selectedUserId, setSelectedUserId] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen) {
      dispatch(fetchTechnicians());
    }
  }, [isOpen, dispatch]);

  const handleAssign = async () => {
    if (!selectedUserId) return;

    setLoading(true);
    try {
      await dispatch(assignIncident({ id: incident.id, userId: parseInt(selectedUserId) })).unwrap();
      onSuccess?.();
      onClose();
    } catch (error) {
      console.error('Error assigning incident:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Assigner l'incident" size="md">
      <div className="space-y-4">
        <div>
          <p className="text-sm text-gray-600 mb-4">
            Sélectionnez un technicien pour assigner l'incident #{incident?.id}
          </p>

          <label className="block text-sm font-medium text-gray-700 mb-2">
            Technicien
          </label>
          <select
            value={selectedUserId}
            onChange={(e) => setSelectedUserId(e.target.value)}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="">Sélectionner un technicien</option>
            {technicians.map((tech) => (
              <option key={tech.id} value={tech.id}>
                {tech.firstName} {tech.lastName} ({tech.username})
              </option>
            ))}
          </select>
        </div>

        <div className="flex justify-end space-x-3 pt-4">
          <button
            onClick={onClose}
            className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50"
            disabled={loading}
          >
            Annuler
          </button>
          <button
            onClick={handleAssign}
            disabled={!selectedUserId || loading}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 flex items-center"
          >
            {loading ? (
              <LoadingSpinner size="sm" text="" />
            ) : (
              <>
                <UserCheck className="h-5 w-5 mr-2" />
                Assigner
              </>
            )}
          </button>
        </div>
      </div>
    </Modal>
  );
};

export default AssignModal;