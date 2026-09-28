import { useState, useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams } from 'react-router-dom';
import { createIncident, updateIncident, fetchIncidentById } from '../../redux/slices/incidentSlice';
import { Save, X } from 'lucide-react';
import { INCIDENT_TYPES, PRIORITIES } from '../../utils/constants';
import LoadingSpinner from '../Common/LoadingSpinner';
import ErrorMessage from '../Common/ErrorMessage';

const IncidentForm = () => {
  const { id } = useParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { currentIncident, loading, error } = useSelector((state) => state.incidents);
  const isEditMode = !!id;

  const [formData, setFormData] = useState({
    incidentType: '',
    description: '',
    priority: 'MOYENNE',
    lineNumber: '',
    referenceFacture: '',
    clientId: '',
    montantErreur: ''
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (isEditMode) {
      dispatch(fetchIncidentById(id));
    }
  }, [dispatch, id, isEditMode]);

  useEffect(() => {
    if (isEditMode && currentIncident) {
      setFormData({
        incidentType: currentIncident.incidentType || '',
        description: currentIncident.description || '',
        priority: currentIncident.priority || 'MOYENNE',
        lineNumber: currentIncident.lineNumber || '',
        referenceFacture: currentIncident.referenceFacture || '',
        clientId: currentIncident.clientId || '',
        montantErreur: currentIncident.montantErreur || ''
      });
    }
  }, [currentIncident, isEditMode]);

  const handleChange = (field, value) => {
    setFormData({ ...formData, [field]: value });
    if (errors[field]) {
      setErrors({ ...errors, [field]: '' });
    }
  };

  const validate = () => {
    const newErrors = {};

    if (!formData.incidentType) {
      newErrors.incidentType = 'Le type d\'incident est requis';
    }
    if (!formData.description || formData.description.trim().length < 10) {
      newErrors.description = 'La description doit contenir au moins 10 caractères';
    }
    if (!formData.priority) {
      newErrors.priority = 'La priorité est requise';
    }
    if (!formData.lineNumber || formData.lineNumber < 1) {
      newErrors.lineNumber = 'Le numéro de ligne est requis et doit être positif';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!validate()) {
      return;
    }

    const payload = {
      ...formData,
      lineNumber: parseInt(formData.lineNumber),
      montantErreur: formData.montantErreur ? parseFloat(formData.montantErreur) : null
    };

    try {
      if (isEditMode) {
        await dispatch(updateIncident({ id, data: payload })).unwrap();
      } else {
        await dispatch(createIncident(payload)).unwrap();
      }
      navigate('/incidents');
    } catch (err) {
      console.error('Error saving incident:', err);
    }
  };

  if (loading && isEditMode && !currentIncident) {
    return <LoadingSpinner />;
  }

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">
          {isEditMode ? 'Modifier l\'incident' : 'Nouvel incident'}
        </h1>
        <p className="text-gray-600 mt-1">
          {isEditMode ? 'Modifiez les informations de l\'incident' : 'Créez un nouvel incident de facturation'}
        </p>
      </div>

      {error && <ErrorMessage message={error} />}

      <form onSubmit={handleSubmit} className="bg-white rounded-lg shadow p-6 space-y-6">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Type d'incident <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.incidentType}
              onChange={(e) => handleChange('incidentType', e.target.value)}
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.incidentType ? 'border-red-500' : 'border-gray-300'
              }`}
            >
              <option value="">Sélectionner un type</option>
              {INCIDENT_TYPES.map((type) => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
            {errors.incidentType && (
              <p className="text-red-500 text-sm mt-1">{errors.incidentType}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Priorité <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.priority}
              onChange={(e) => handleChange('priority', e.target.value)}
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.priority ? 'border-red-500' : 'border-gray-300'
              }`}
            >
              {PRIORITIES.map((priority) => (
                <option key={priority.value} value={priority.value}>
                  {priority.label}
                </option>
              ))}
            </select>
            {errors.priority && (
              <p className="text-red-500 text-sm mt-1">{errors.priority}</p>
            )}
          </div>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Description <span className="text-red-500">*</span>
          </label>
          <textarea
            value={formData.description}
            onChange={(e) => handleChange('description', e.target.value)}
            rows={4}
            className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
              errors.description ? 'border-red-500' : 'border-gray-300'
            }`}
            placeholder="Décrivez l'incident en détail..."
          />
          {errors.description && (
            <p className="text-red-500 text-sm mt-1">{errors.description}</p>
          )}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Numéro de ligne <span className="text-red-500">*</span>
            </label>
            <input
              type="number"
              value={formData.lineNumber}
              onChange={(e) => handleChange('lineNumber', e.target.value)}
              min="1"
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent ${
                errors.lineNumber ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {errors.lineNumber && (
              <p className="text-red-500 text-sm mt-1">{errors.lineNumber}</p>
            )}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Référence facture
            </label>
            <input
              type="text"
              value={formData.referenceFacture}
              onChange={(e) => handleChange('referenceFacture', e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="REF-2024-001"
            />
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              ID Client
            </label>
            <input
              type="text"
              value={formData.clientId}
              onChange={(e) => handleChange('clientId', e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="CLIENT-123"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Montant erreur (€)
            </label>
            <input
              type="number"
              step="0.01"
              value={formData.montantErreur}
              onChange={(e) => handleChange('montantErreur', e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="0.00"
            />
          </div>
        </div>

        <div className="flex justify-end space-x-3 pt-4 border-t">
          <button
            type="button"
            onClick={() => navigate('/incidents')}
            className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors flex items-center"
          >
            <X className="h-5 w-5 mr-2" />
            Annuler
          </button>
          <button
            type="submit"
            disabled={loading}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 flex items-center"
          >
            <Save className="h-5 w-5 mr-2" />
            {loading ? 'Enregistrement...' : 'Enregistrer'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default IncidentForm;