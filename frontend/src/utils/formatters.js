// Formatage des dates
export const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  const day = String(date.getDate()).padStart(2, '0');
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const year = date.getFullYear();
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  return `${day}/${month}/${year} ${hours}:${minutes}`;
};

// Formatage des montants
export const formatAmount = (amount) => {
  if (amount === null || amount === undefined) return '-';
  return new Intl.NumberFormat('fr-FR', {
    style: 'currency',
    currency: 'EUR'
  }).format(amount);
};

// Tronquer le texte
export const truncateText = (text, maxLength = 50) => {
  if (!text) return '-';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

// Obtenir le libellé d'un type d'incident
export const getIncidentTypeLabel = (type) => {
  const types = {
    DOUBLON: 'Doublon',
    ERREUR_CALCUL: 'Erreur de calcul',
    LIGNE_INCOMPLETE: 'Ligne incomplète',
    REFERENCE_INVALIDE: 'Référence invalide',
    MONTANT_INCORRECT: 'Montant incorrect',
    ERREUR_TVA: 'Erreur TVA',
    AUTRE: 'Autre'
  };
  return types[type] || type;
};

// Obtenir le libellé d'un statut
export const getStatusLabel = (status) => {
  const statuses = {
    NOUVEAU: 'Nouveau',
    EN_COURS: 'En cours',
    RESOLU: 'Résolu',
    FERME: 'Fermé'
  };
  return statuses[status] || status;
};

// Obtenir le libellé d'une priorité
export const getPriorityLabel = (priority) => {
  const priorities = {
    FAIBLE: 'Faible',
    MOYENNE: 'Moyenne',
    HAUTE: 'Haute',
    CRITIQUE: 'Critique'
  };
  return priorities[priority] || priority;
};