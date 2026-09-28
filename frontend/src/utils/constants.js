// Types d'incidents
export const INCIDENT_TYPES = [
  { value: 'DOUBLON', label: 'Doublon' },
  { value: 'ERREUR_CALCUL', label: 'Erreur de calcul' },
  { value: 'LIGNE_INCOMPLETE', label: 'Ligne incomplète' },
  { value: 'REFERENCE_INVALIDE', label: 'Référence invalide' },
  { value: 'MONTANT_INCORRECT', label: 'Montant incorrect' },
  { value: 'ERREUR_TVA', label: 'Erreur TVA' },
  { value: 'AUTRE', label: 'Autre' }
];

// Statuts
export const STATUSES = [
  { value: 'NOUVEAU', label: 'Nouveau', color: 'blue' },
  { value: 'EN_COURS', label: 'En cours', color: 'yellow' },
  { value: 'RESOLU', label: 'Résolu', color: 'green' },
  { value: 'FERME', label: 'Fermé', color: 'gray' }
];

// Priorités
export const PRIORITIES = [
  { value: 'FAIBLE', label: 'Faible', color: 'green' },
  { value: 'MOYENNE', label: 'Moyenne', color: 'yellow' },
  { value: 'HAUTE', label: 'Haute', color: 'orange' },
  { value: 'CRITIQUE', label: 'Critique', color: 'red' }
];

// Rôles
export const ROLES = [
  { value: 'ADMIN', label: 'Administrateur' },
  { value: 'SUPERVISEUR', label: 'Superviseur' },
  { value: 'TECHNICIEN', label: 'Technicien' }
];

// Types de solution
export const SOLUTION_TYPES = [
  { value: 'CORRECTION', label: 'Correction' },
  { value: 'WORKAROUND', label: 'Contournement' },
  { value: 'PERMANENT', label: 'Permanent' }
];

// Couleurs pour badges
export const STATUS_COLORS = {
  NOUVEAU: 'bg-blue-100 text-blue-800',
  EN_COURS: 'bg-yellow-100 text-yellow-800',
  RESOLU: 'bg-green-100 text-green-800',
  FERME: 'bg-gray-100 text-gray-800'
};

export const PRIORITY_COLORS = {
  FAIBLE: 'bg-green-100 text-green-800',
  MOYENNE: 'bg-yellow-100 text-yellow-800',
  HAUTE: 'bg-orange-100 text-orange-800',
  CRITIQUE: 'bg-red-100 text-red-800'
};