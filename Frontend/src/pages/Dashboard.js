import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { studentApi, userApi } from '../services/api';

/**
 * Dashboard Page Component.
 * 
 * Features:
 * - Overview statistics
 * - Role-based content (Admin sees user count, Teacher sees only student stats)
 * - Quick links to common actions
 */
const Dashboard = () => {
  const { user, isAdmin } = useAuth();
  const [stats, setStats] = useState({
    totalStudents: 0,
    averageGrade: 0,
    lowAttendanceCount: 0,
    totalUsers: 0,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchStats = async () => {
      setLoading(true);
      setError(null);
      
      try {
        // Fetch student data
        const studentsResponse = await studentApi.getAll();
        const students = studentsResponse.data;
        
        // Calculate stats
        const totalStudents = students.length;
        const averageGrade = students.length > 0
          ? students.reduce((sum, s) => sum + s.grade, 0) / students.length
          : 0;
        const lowAttendanceCount = students.filter(s => s.attendance < 75).length;

        // Fetch user count (admin only)
        let totalUsers = 0;
        if (isAdmin) {
          try {
            const usersResponse = await userApi.getAll();
            totalUsers = usersResponse.data.length;
          } catch (err) {
            console.warn('Could not fetch user count:', err);
          }
        }

        setStats({
          totalStudents,
          averageGrade: averageGrade.toFixed(1),
          lowAttendanceCount,
          totalUsers,
        });
      } catch (err) {
        console.error('Error fetching dashboard stats:', err);
        setError('Failed to load dashboard statistics');
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, [isAdmin]);

  if (loading) {
    return (
      <div className="loading-container">
        <div className="spinner"></div>
      </div>
    );
  }

  return (
    <div className="container">
      <div className="page-header">
        <h1 className="page-title">Dashboard</h1>
        <span>Welcome back, {user?.username}!</span>
      </div>

      {error && (
        <div className="alert alert-error">{error}</div>
      )}

      {/* Stats Cards */}
      <div className="dashboard-stats">
        <div className="stat-card">
          <div className="stat-card-header">
            <div>
              <div className="stat-label">Total Students</div>
              <div className="stat-value">{stats.totalStudents}</div>
            </div>
            <div className="stat-icon primary">&#x1F393;</div>
          </div>
          <Link to="/students" className="btn btn-primary btn-sm" style={{ marginTop: '0.5rem' }}>
            View All
          </Link>
        </div>

        <div className="stat-card">
          <div className="stat-card-header">
            <div>
              <div className="stat-label">Average Grade</div>
              <div className="stat-value">{stats.averageGrade}%</div>
            </div>
            <div className="stat-icon success">&#x1F4CA;</div>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-card-header">
            <div>
              <div className="stat-label">Low Attendance</div>
              <div className="stat-value">{stats.lowAttendanceCount}</div>
            </div>
            <div className="stat-icon warning">&#x26A0;</div>
          </div>
          <span style={{ fontSize: '0.75rem', color: '#757575' }}>
            Students with &lt;75% attendance
          </span>
        </div>

        {/* Admin only: User count */}
        {isAdmin && (
          <div className="stat-card">
            <div className="stat-card-header">
              <div>
                <div className="stat-label">Total Users</div>
                <div className="stat-value">{stats.totalUsers}</div>
              </div>
              <div className="stat-icon primary">&#x1F465;</div>
            </div>
            <Link to="/users" className="btn btn-primary btn-sm" style={{ marginTop: '0.5rem' }}>
              Manage Users
            </Link>
          </div>
        )}
      </div>

      {/* Quick Actions */}
      <div className="card" style={{ marginTop: '2rem' }}>
        <div className="card-header">
          <h2 className="card-title">Quick Actions</h2>
        </div>
        <div style={{ display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
          <Link to="/students" className="btn btn-primary">
            View Students
          </Link>
          
          {/* Admin only: Add Student button */}
          {isAdmin && (
            <Link to="/students?action=add" className="btn btn-success">
              Add New Student
            </Link>
          )}
          
          {/* Admin only: Manage Users button */}
          {isAdmin && (
            <Link to="/users" className="btn btn-secondary">
              Manage Users
            </Link>
          )}
        </div>
      </div>

      {/* Role Information */}
      <div className="card" style={{ marginTop: '2rem' }}>
        <div className="card-header">
          <h2 className="card-title">Your Permissions</h2>
        </div>
        <div>
          {isAdmin ? (
            <div>
              <p><strong>Role:</strong> Administrator</p>
              <ul style={{ marginLeft: '1.5rem', marginTop: '0.5rem' }}>
                <li>View, Create, Update, and Delete students</li>
                <li>Manage user accounts</li>
                <li>Full system access</li>
              </ul>
            </div>
          ) : (
            <div>
              <p><strong>Role:</strong> Teacher</p>
              <ul style={{ marginLeft: '1.5rem', marginTop: '0.5rem' }}>
                <li>View all students</li>
                <li>Update student grades and attendance</li>
                <li style={{ color: '#d32f2f' }}>Cannot create or delete students</li>
                <li style={{ color: '#d32f2f' }}>Cannot manage users</li>
              </ul>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
