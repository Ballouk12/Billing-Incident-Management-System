import { NavLink } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { 
  LayoutDashboard, 
  AlertCircle, 
  Upload, 
  Users, 
  BarChart3,
  X 
} from 'lucide-react';

const Sidebar = ({ isOpen, onClose }) => {
  const { user } = useSelector((state) => state.auth);

  const getMenuItems = () => {
    const baseItems = [
      { path: '/incidents', label: 'Incidents', icon: AlertCircle, roles: ['ADMIN', 'SUPERVISEUR', 'TECHNICIEN'] }
    ];

    if (user?.role === 'ADMIN' || user?.role === 'SUPERVISEUR') {
      baseItems.push(
        { path: '/', label: 'Dashboard', icon: LayoutDashboard, roles: ['ADMIN', 'SUPERVISEUR'] },
        { path: '/upload', label: 'Upload Fichier', icon: Upload, roles: ['ADMIN', 'SUPERVISEUR'] },
        { path: '/statistics', label: 'Statistiques', icon: BarChart3, roles: ['ADMIN', 'SUPERVISEUR'] }
      );
    }

    if (user?.role === 'ADMIN') {
      baseItems.push(
        { path: '/users', label: 'Utilisateurs', icon: Users, roles: ['ADMIN'] }
      );
    }

    return baseItems;
  };

  const menuItems = getMenuItems();

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-20 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed top-0 left-0 z-30 h-full w-64 bg-white border-r border-gray-200 transform transition-transform duration-300 ease-in-out lg:translate-x-0 lg:mt-16 ${
          isOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex items-center justify-between p-4 lg:hidden border-b">
          <h2 className="text-lg font-semibold text-gray-900">Menu</h2>
          <button
            onClick={onClose}
            className="p-2 rounded-md text-gray-600 hover:bg-gray-100"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        <nav className="p-4 space-y-1 mt-16 lg:mt-0">
          {menuItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.path}
                to={item.path}
                onClick={onClose}
                className={({ isActive }) =>
                  `flex items-center px-4 py-3 rounded-lg transition-colors ${
                    isActive
                      ? 'bg-blue-50 text-blue-700'
                      : 'text-gray-700 hover:bg-gray-100'
                  }`
                }
              >
                <Icon className="h-5 w-5 mr-3" />
                <span className="font-medium">{item.label}</span>
              </NavLink>
            );
          })}
        </nav>
      </aside>
    </>
  );
};

export default Sidebar;