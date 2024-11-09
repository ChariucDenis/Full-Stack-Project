import axios from 'axios';
import jwt_decode from 'jwt-decode';

const API_URL = 'http://localhost:8080/api/v1/auth/';

const AuthService = {
  login: async (email, password) => {
    try {
      const response = await axios.post(`${API_URL}authenticate`, { email, password });

      if (response.data.accessToken) {
        localStorage.setItem('token', response.data.accessToken);
        localStorage.setItem('user', JSON.stringify(jwt_decode(response.data.accessToken)));
      }

      return response.data;
    } catch (error) {
      throw new Error('Login failed');
    }
  },

  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  getCurrentUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },

  isAuthenticated: () => {
    const token = localStorage.getItem('token');
    if (token) {
      const decodedToken = jwt_decode(token);
      const currentTime = Date.now() / 1000;

      if (decodedToken.exp < currentTime) {
        AuthService.logout();
        return false;
      }
      return true;
    }
    return false;
  },
};

export default AuthService;
