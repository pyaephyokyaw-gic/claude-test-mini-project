import axios from 'axios';

/**
 * Configured Axios instance with interceptors for JWT handling.
 * 
 * Features:
 * - Automatically attaches JWT token to all requests
 * - Handles 401 (Unauthorized) - triggers logout
 * - Handles 403 (Forbidden) - access denied notification
 * - Base URL configuration for API
 * 
 * Security considerations:
 * - Token is attached via Authorization header
 * - Error responses are handled consistently
 * - Prevents infinite loops on auth errors
 */

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

// Create Axios instance with default config
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 second timeout
});

/**
 * Request interceptor - adds JWT token to outgoing requests.
 * 
 * Automatically retrieves token from localStorage and attaches
 * to Authorization header if available.
 */
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    console.error('Request interceptor error:', error);
    return Promise.reject(error);
  }
);

/**
 * Response interceptor - handles authentication errors.
 * 
 * 401 Unauthorized: Token invalid/expired - logout user
 * 403 Forbidden: User lacks required permissions - show message
 * 
 * Note: These handlers dispatch custom events that components
 * can listen to for displaying appropriate UI feedback.
 */
api.interceptors.response.use(
  (response) => {
    // Successful response - return as-is
    return response;
  },
  (error) => {
    const { response } = error;

    if (response) {
      switch (response.status) {
        case 401:
          // Unauthorized - token expired or invalid
          // Clear storage and redirect to login
          // Skip if this is the login request itself
          if (!error.config.url.includes('/api/auth/login')) {
            console.warn('Unauthorized - clearing auth state');
            localStorage.removeItem('accessToken');
            
            // Dispatch custom event for components to handle
            window.dispatchEvent(new CustomEvent('auth:unauthorized', {
              detail: { message: 'Session expired. Please login again.' }
            }));
            
            // Redirect to login page
            if (window.location.pathname !== '/login') {
              window.location.href = '/login';
            }
          }
          break;

        case 403:
          // Forbidden - user doesn't have required permissions
          console.warn('Access denied:', response.data?.message || 'Forbidden');
          
          // Dispatch custom event for UI notification
          window.dispatchEvent(new CustomEvent('auth:forbidden', {
            detail: { 
              message: response.data?.message || 'Access denied. You do not have permission to perform this action.'
            }
          }));
          break;

        case 404:
          // Not found
          console.warn('Resource not found:', error.config.url);
          break;

        case 500:
          // Server error
          console.error('Server error:', response.data?.message || 'Internal server error');
          break;

        default:
          console.error('Request failed:', response.status, response.data);
      }
    } else if (error.request) {
      // Network error - no response received
      console.error('Network error - no response received');
      window.dispatchEvent(new CustomEvent('network:error', {
        detail: { message: 'Unable to connect to server. Please check your connection.' }
      }));
    }

    return Promise.reject(error);
  }
);

export default api;

/**
 * Student API endpoints.
 */
export const studentApi = {
  getAll: () => api.get('/api/students'),
  getById: (id) => api.get(`/api/students/${id}`),
  create: (data) => api.post('/api/students', data),
  update: (id, data) => api.put(`/api/students/${id}`, data),
  delete: (id) => api.delete(`/api/students/${id}`),
  search: (name) => api.get(`/api/students/search?name=${encodeURIComponent(name)}`),
  getLowAttendance: (threshold = 75) => api.get(`/api/students/low-attendance?threshold=${threshold}`),
};

/**
 * User API endpoints (Admin only).
 */
export const userApi = {
  getAll: () => api.get('/api/users'),
  getById: (id) => api.get(`/api/users/${id}`),
  create: (data) => api.post('/api/users', data),
  delete: (id) => api.delete(`/api/users/${id}`),
  toggleStatus: (id) => api.patch(`/api/users/${id}/toggle-status`),
};

/**
 * Auth API endpoints.
 */
export const authApi = {
  login: (credentials) => api.post('/api/auth/login', credentials),
};
