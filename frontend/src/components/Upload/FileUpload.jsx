import { useState } from 'react';
import { uploadFile } from '../../services/fileService';
import { Upload, File, CheckCircle, AlertCircle, X } from 'lucide-react';

const FileUpload = () => {
  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [dragOver, setDragOver] = useState(false);

  const handleFileSelect = (e) => {
    const selectedFile = e.target.files[0];
    validateAndSetFile(selectedFile);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    setDragOver(false);
    const droppedFile = e.dataTransfer.files[0];
    validateAndSetFile(droppedFile);
  };

  const validateAndSetFile = (selectedFile) => {
    setError(null);
    setResult(null);

    if (!selectedFile) return;

    // Vérifier le type de fichier
    const allowedTypes = [
      'text/csv',
      'application/vnd.ms-excel',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    ];
    
    const fileExtension = selectedFile.name.split('.').pop().toLowerCase();
    const isValidExtension = ['csv', 'xls', 'xlsx'].includes(fileExtension);

    if (!allowedTypes.includes(selectedFile.type) && !isValidExtension) {
      setError('Format de fichier non valide. Formats acceptés: CSV, XLS, XLSX');
      return;
    }

    // Vérifier la taille (50MB max)
    if (selectedFile.size > 50 * 1024 * 1024) {
      setError('Le fichier est trop volumineux. Taille maximale: 50MB');
      return;
    }

    setFile(selectedFile);
  };

  const handleUpload = async () => {
    if (!file) return;

    setUploading(true);
    setError(null);
    setResult(null);
    setUploadProgress(0);

    try {
      const response = await uploadFile(file, (progressEvent) => {
        const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total);
        setUploadProgress(progress);
      });

      setResult(response);
      setFile(null);
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors de l\'upload du fichier');
    } finally {
      setUploading(false);
      setUploadProgress(0);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    setDragOver(true);
  };

  const handleDragLeave = () => {
    setDragOver(false);
  };

  const removeFile = () => {
    setFile(null);
    setError(null);
  };

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Upload de fichier</h1>
        <p className="text-gray-600 mt-1">
          Importez un fichier CSV ou Excel contenant les incidents de facturation
        </p>
      </div>

      <div className="bg-white rounded-lg shadow p-6 space-y-6">
        {/* Zone de drop */}
        <div
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          className={`border-2 border-dashed rounded-lg p-12 text-center transition-colors ${
            dragOver
              ? 'border-blue-500 bg-blue-50'
              : 'border-gray-300 hover:border-gray-400'
          }`}
        >
          <Upload className={`h-12 w-12 mx-auto mb-4 ${dragOver ? 'text-blue-500' : 'text-gray-400'}`} />
          <p className="text-lg font-medium text-gray-900 mb-2">
            Glissez-déposez votre fichier ici
          </p>
          <p className="text-sm text-gray-600 mb-4">ou</p>
          <label className="inline-flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 cursor-pointer">
            <span>Choisir un fichier</span>
            <input
              type="file"
              onChange={handleFileSelect}
              accept=".csv,.xls,.xlsx"
              className="hidden"
              disabled={uploading}
            />
          </label>
          <p className="text-xs text-gray-500 mt-4">
            Formats acceptés: CSV, XLS, XLSX (max 50MB)
          </p>
        </div>

        {/* Fichier sélectionné */}
        {file && (
          <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
            <div className="flex items-center space-x-3">
              <File className="h-8 w-8 text-blue-600" />
              <div>
                <p className="text-sm font-medium text-gray-900">{file.name}</p>
                <p className="text-xs text-gray-500">
                  {(file.size / 1024 / 1024).toFixed(2)} MB
                </p>
              </div>
            </div>
            {!uploading && (
              <button
                onClick={removeFile}
                className="text-gray-400 hover:text-gray-600"
              >
                <X className="h-5 w-5" />
              </button>
            )}
          </div>
        )}

        {/* Barre de progression */}
        {uploading && (
          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-sm font-medium text-gray-700">Upload en cours...</span>
              <span className="text-sm font-medium text-gray-700">{uploadProgress}%</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div
                className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                style={{ width: `${uploadProgress}%` }}
              />
            </div>
          </div>
        )}

        {/* Message d'erreur */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <div className="flex items-start">
              <AlertCircle className="h-5 w-5 text-red-600 mr-3 flex-shrink-0 mt-0.5" />
              <p className="text-sm text-red-800">{error}</p>
            </div>
          </div>
        )}

        {/* Résultat */}
        {result && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <div className="flex items-start">
              <CheckCircle className="h-5 w-5 text-green-600 mr-3 flex-shrink-0 mt-0.5" />
              <div className="flex-1">
                <p className="text-sm font-medium text-green-800 mb-2">
                  Fichier importé avec succès !
                </p>
                <div className="text-sm text-green-700 space-y-1">
                  <p>• Fichier: {result.filename}</p>
                  <p>• Incidents extraits: {result.incidentsExtracted}</p>
                  <p>• Incidents créés: {result.incidentsCreated}</p>
                  {result.message && <p>• {result.message}</p>}
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Bouton upload */}
        {file && !uploading && (
          <button
            onClick={handleUpload}
            className="w-full px-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center"
          >
            <Upload className="h-5 w-5 mr-2" />
            Uploader le fichier
          </button>
        )}
      </div>

      {/* Instructions */}
      <div className="mt-6 bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="text-sm font-semibold text-blue-900 mb-2">Instructions</h3>
        <ul className="text-sm text-blue-800 space-y-1 list-disc list-inside">
          <li>Le fichier doit contenir les colonnes requises pour les incidents</li>
          <li>Les formats acceptés sont: CSV, XLS, XLSX</li>
          <li>La taille maximale du fichier est de 50MB</li>
          <li>Les incidents en doublon seront automatiquement détectés</li>
        </ul>
      </div>
    </div>
  );
};

export default FileUpload;