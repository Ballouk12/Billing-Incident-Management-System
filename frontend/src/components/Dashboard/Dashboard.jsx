import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { fetchStatistics, fetchIncidents } from '../../redux/slices/incidentSlice';
import { AlertCircle, CheckCircle, Clock, FileText, TrendingUp } from 'lucide-react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import StatCard from './StatCard';
import LoadingSpinner from '../Common/LoadingSpinner';
import { formatDate, getStatusLabel, getPriorityLabel } from '../../utils/formatters';
import { STATUS_COLORS, PRIORITY_COLORS } from '../../utils/constants';

const Dashboard = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { statistics, incidents, loading } = useSelector((state) => state.incidents);
  const { user } = useSelector((state) => state.auth);
  const [recentIncidents, setRecentIncidents] = useState([]);

  useEffect(() => {
    dispatch(fetchStatistics());
    dispatch(fetchIncidents({ page: 0, size: 5, sortBy: 'createdAt', sortDir: 'DESC' }));
  }, [dispatch]);

  useEffect(() => {
    if (incidents && incidents.length > 0) {
      setRecentIncidents(incidents.slice(0, 5));
    }
  }, [incidents]);

  if (loading) {
    return <LoadingSpinner />;
  }

  // Données pour le graphique en barres (incidents par type)
  const incidentTypeData = statistics ? [
    { name: 'Doublon', value: statistics.doublon || 0 },
    { name: 'Erreur calcul', value: statistics.erreur_calcul || 0 },
    { name: 'Ligne incomplète', value: statistics.ligne_incomplete || 0 },
    { name: 'Référence invalide', value: statistics.reference_invalide || 0 },
    { name: 'Montant incorrect', value: statistics.montant_incorrect || 0 },
    { name: 'Erreur TVA', value: statistics.erreur_tva || 0 }
  ] : [];

  // Données pour le graphique circulaire (statuts)
  const statusData = statistics ? [
    { name: 'Nouveau', value: statistics.nouveau || 0, color: '#3B82F6' },
    { name: 'En cours', value: statistics.en_cours || 0, color: '#EAB308' },
    { name: 'Résolu', value: statistics.resolu || 0, color: '#10B981' },
    { name: 'Fermé', value: statistics.ferme || 0, color: '#6B7280' }
  ] : [];

  // Calcul du taux de résolution
  const resolutionRate = statistics && statistics.total > 0
    ? ((statistics.resolu / statistics.total) * 100).toFixed(1)
    : 0;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mt-1">Vue d'ensemble de vos incidents</p>
      </div>

      {/* Cartes statistiques */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          title="Total incidents"
          value={statistics?.total || 0}
          icon={FileText}
          color="blue"
        />
        <StatCard
          title="En cours"
          value={statistics?.en_cours || 0}
          icon={Clock}
          color="yellow"
        />
        <StatCard
          title="Résolus"
          value={statistics?.resolu || 0}
          icon={CheckCircle}
          color="green"
        />
        <StatCard
          title="Taux résolution"
          value={`${resolutionRate}%`}
          icon={TrendingUp}
          color="purple"
        />
      </div>

      {/* Graphiques */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Incidents par type */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Incidents par type</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={incidentTypeData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" angle={-45} textAnchor="end" height={100} />
              <YAxis />
              <Tooltip />
              <Bar dataKey="value" fill="#3B82F6" />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Répartition par statut */}
        <div className="bg-white rounded-lg shadow p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Répartition par statut</h3>
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={statusData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                outerRadius={100}
                fill="#8884d8"
                dataKey="value"
              >
                {statusData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={entry.color} />
                ))}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Incidents récents */}
      <div className="bg-white rounded-lg shadow">
        <div className="p-6 border-b border-gray-200">
          <h3 className="text-lg font-semibold text-gray-900">Incidents récents</h3>
        </div>
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
                  Statut
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Priorité
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Date
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {recentIncidents.length === 0 ? (
                <tr>
                  <td colSpan="6" className="px-6 py-8 text-center text-gray-500">
                    Aucun incident récent
                  </td>
                </tr>
              ) : (
                recentIncidents.map((incident) => (
                  <tr
                    key={incident.id}
                    onClick={() => navigate(`/incidents/${incident.id}`)}
                    className="hover:bg-gray-50 cursor-pointer"
                  >
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      #{incident.id}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {incident.incidentType}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      {incident.description?.substring(0, 50)}...
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${STATUS_COLORS[incident.status]}`}>
                        {getStatusLabel(incident.status)}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${PRIORITY_COLORS[incident.priority]}`}>
                        {getPriorityLabel(incident.priority)}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatDate(incident.createdAt)}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;