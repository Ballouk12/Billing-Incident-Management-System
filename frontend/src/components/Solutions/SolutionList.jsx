import { useEffect, useState } from 'react';
import { getSolutions } from '../../services/incidentService';
import { CheckCircle, FileText } from 'lucide-react';
import { formatDate } from '../../utils/formatters';
import LoadingSpinner from '../Common/LoadingSpinner';

const SolutionList = ({ incidentId }) => {
  const [solutions, setSolutions] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSolutions();
  }, [incidentId]);

  const loadSolutions = async () => {
    try {
      setLoading(true);
      const data = await getSolutions(incidentId);
      setSolutions(data);
    } catch (error) {
      console.error('Error loading solutions:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <LoadingSpinner size="sm" />;
  }

  if (solutions.length === 0) {
    return (
      <div className="text-center py-8 text-gray-500">
        <FileText className="h-12 w-12 mx-auto mb-3 text-gray-400" />
        <p>Aucune solution proposée pour cet incident</p>
      </div>
    );
  }

  const getSolutionTypeColor = (type) => {
    const colors = {
      CORRECTION: 'bg-green-100 text-green-800',
      WORKAROUND: 'bg-yellow-100 text-yellow-800',
      PERMANENT: 'bg-blue-100 text-blue-800'
    };
    return colors[type] || 'bg-gray-100 text-gray-800';
  };

  const getSolutionTypeLabel = (type) => {
    const labels = {
      CORRECTION: 'Correction',
      WORKAROUND: 'Contournement',
      PERMANENT: 'Permanent'
    };
    return labels[type] || type;
  };

  return (
    <div className="space-y-4">
      {solutions.map((solution) => (
        <div key={solution.id} className="border border-gray-200 rounded-lg p-4">
          <div className="flex items-start justify-between mb-3">
            <div className="flex items-center space-x-3">
              <span className={`px-2 py-1 text-xs font-semibold rounded-full ${getSolutionTypeColor(solution.solutionType)}`}>
                {getSolutionTypeLabel(solution.solutionType)}
              </span>
              {solution.validated && (
                <span className="flex items-center text-green-600 text-sm">
                  <CheckCircle className="h-4 w-4 mr-1" />
                  Validée
                </span>
              )}
            </div>
            <div className="text-right">
              <p className="text-sm text-gray-600">{solution.createdByUsername}</p>
              <p className="text-xs text-gray-500">{formatDate(solution.createdAt)}</p>
            </div>
          </div>
          
          <div className="mb-2">
            <p className="text-gray-900 whitespace-pre-wrap">{solution.description}</p>
          </div>

          {solution.comments && (
            <div className="mt-3 pt-3 border-t border-gray-100">
              <p className="text-sm text-gray-600">
                <span className="font-medium">Commentaires:</span> {solution.comments}
              </p>
            </div>
          )}
        </div>
      ))}
    </div>
  );
};

export default SolutionList;