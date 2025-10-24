import { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { over } from 'stompjs';
import MapComponent from './MapComponent';
import './LocationTracker.css';

const LocationTracker = () => {
  const [currentUserId] = useState('68245dae1ad1e1353029add5');
  const [trackedUserIds, setTrackedUserIds] = useState([]);
  const [users, setUsers] = useState({
    "6808d2029098fc587e300bca": {
      id: "6808d2029098fc587e300bca",
      name: "dalla@gmail.com",
      color: "#4361ee",
      initial: "D",
      isActive: false,
      isConnected: false,
      isTracking: false,
      path: [],
      footprints: [],
      lastPosition: null,
      lastSeen: null,
      history: []
    },
    "68245dbd1ad1e1353029add7": {
      id: "68245dbd1ad1e1353029add7",
      name: "local@gmail.com",
      color: "#4cc9f0",
      initial: "N",
      isActive: false,
      isConnected: false,
      isTracking: false,
      path: [],
      footprints: [],
      lastPosition: null,
      lastSeen: null,
      history: []
    },
    "68245dae1ad1e1353029add5": {
      id: "68245dae1ad1e1353029add5",
      name: "super@gmail.com",
      color: "#4ad66d",
      initial: "S",
      isActive: false,
      isConnected: false,
      isTracking: false,
      path: [],
      footprints: [],
      lastPosition: null,
      lastSeen: null,
      history: []
    }
  });
  const [historicalData, setHistoricalData] = useState([]);
  const [historyStatus, setHistoryStatus] = useState('Not loaded');
  const [error, setError] = useState('');
  const [lastUpdate, setLastUpdate] = useState('Never');
  const stompClient = useRef(null);

  const formatTimeDifference = (timestamp) => {
    if (!timestamp) return "Never";
    
    try {
      const lastSeen = new Date(timestamp);
      if (isNaN(lastSeen.getTime())) {
        return "Never";
      }
      
      const now = new Date();
      const diffInSeconds = Math.floor((now - lastSeen) / 1000);
      
      if (diffInSeconds < 60) return "Just now";
      if (diffInSeconds < 3600) return `${Math.floor(diffInSeconds/60)} minutes ago`;
      if (diffInSeconds < 86400) return `${Math.floor(diffInSeconds/3600)} hours ago`;
      
      return lastSeen.toLocaleString();
    } catch (e) {
      console.error('Error formatting time:', e);
      return "Unknown";
    }
  };

  const updateTrackedUsersList = () => {
    setLastUpdate(new Date().toLocaleTimeString());
  };

  const markUserOffline = (userId) => {
    setUsers(prevUsers => ({
      ...prevUsers,
      [userId]: {
        ...prevUsers[userId],
        isActive: false,
        isConnected: false,
        lastSeen: prevUsers[userId].lastSeen || new Date().toISOString()
      }
    }));
  };

  const updateUserConnectionStatus = (userId, isConnected) => {
    setUsers(prevUsers => ({
      ...prevUsers,
      [userId]: {
        ...prevUsers[userId],
        isConnected,
        isActive: isConnected ? prevUsers[userId].isActive : false
      }
    }));

    if (!isConnected) {
      markUserOffline(userId);
    }
  };

  const updateUserMap = (userId, location) => {
    const { latitude, longitude, timestamp } = location;
    
    setUsers(prevUsers => {
      const user = prevUsers[userId];
      if (!user) return prevUsers;

      const newPath = [...user.path, [latitude, longitude]];
      const newFootprints = [...user.footprints];
      
      if (newFootprints.length > 100) {
        newFootprints.shift();
      }

      return {
        ...prevUsers,
        [userId]: {
          ...user,
          isActive: true,
          isConnected: true,
          lastSeen: timestamp,
          lastPosition: { lat: latitude, lng: longitude },
          path: newPath,
          footprints: newFootprints
        }
      };
    });

    updateTrackedUsersList();
  };

  const checkInactiveUsers = () => {
    const now = new Date();
    const inactiveThreshold = 5 * 60 * 1000;

    trackedUserIds.forEach(userId => {
      const user = users[userId];
      if (user && user.isActive && user.lastSeen) {
        const lastSeenTime = new Date(user.lastSeen).getTime();
        if (now.getTime() - lastSeenTime > inactiveThreshold) {
          markUserOffline(userId);
        }
      }
    });
  };

  const checkConnectionStatus = () => {
    if (stompClient.current && stompClient.current.connected && trackedUserIds.length > 0) {
      stompClient.current.send(
        "/app/check-multiple-connection-status", 
        {}, 
        JSON.stringify({ userIds: trackedUserIds })
      );
    }
  };

  const sendTrackingRequests = () => {
    trackedUserIds.forEach(userId => {
      const user = users[userId];
      if (!user || user.isTracking) return;

      const request = {
        requestingUserId: currentUserId,
        userIdToTrack: userId
      };

      stompClient.current.send(
        "/app/subscribe-to-user",
        {},
        JSON.stringify(request)
      );

      setUsers(prevUsers => ({
        ...prevUsers,
        [userId]: {
          ...prevUsers[userId],
          isTracking: true
        }
      }));
    });
  };

  const connectWebSocket = () => {
    const socket = new SockJS('http://localhost:7072/ws-location');
    stompClient.current = over(socket);

    const headers = {
      'userId': currentUserId,
      'heart-beat': '5000,5000'
    };

    stompClient.current.connect(headers, (frame) => {
      console.log('Connected to WebSocket');

      stompClient.current.subscribe('/topic/location-updates', (message) => {
        try {
          const location = JSON.parse(message.body);
          if (location.user && trackedUserIds.includes(location.user.userId)) {
            updateUserMap(location.user.userId, location);
          }
        } catch (e) {
          console.error('Error processing location update:', e);
        }
      });

      stompClient.current.subscribe('/topic/connection-status-updates', (message) => {
        try {
          const statusUpdates = JSON.parse(message.body);
          statusUpdates.forEach(status => {
            updateUserConnectionStatus(status.userId, status.connected);
          });
        } catch (e) {
          console.error('Error processing connection status updates:', e);
        }
      });

      stompClient.current.subscribe('/topic/user-status', (message) => {
        try {
          const status = JSON.parse(message.body);
          const userId = status.userId;
          
          if (trackedUserIds.includes(userId)) {
            setUsers(prevUsers => {
              const user = prevUsers[userId];
              if (!user) return prevUsers;

              return {
                ...prevUsers,
                [userId]: {
                  ...user,
                  isConnected: status.online,
                  lastSeen: status.timestamp || new Date().toISOString(),
                  isActive: status.online ? true : user.isActive
                }
              };
            });

            if (!status.online) {
              markUserOffline(userId);
            }
            
            updateTrackedUsersList();
          }
        } catch (e) {
          console.error('Error processing user status:', e);
        }
      });

      if (trackedUserIds.length > 0) {
        checkConnectionStatus();
      }
    }, (error) => {
      console.error('WebSocket connection error:', error);
      setError('Connection error. Please refresh the page.');
      setTimeout(connectWebSocket, 5000);
    });
  };

  const handleUserSelectionChange = (userId, isChecked) => {
    if (isChecked) {
      if (!trackedUserIds.includes(userId)) {
        setTrackedUserIds(prev => [...prev, userId]);
      }
    } else {
      setTrackedUserIds(prev => prev.filter(id => id !== userId));
    }
  };

  const loadHistoricalData = () => {
    const userId = document.getElementById('history-user-select').value;
    if (!userId) {
      setError('Please select a user first');
      return;
    }

    const user = users[userId];
    if (!user) return;

    const dateInput = document.getElementById('history-date');
    if (!dateInput.value) {
      setError('Please select a date');
      return;
    }

    setHistoryStatus('Loading...');
    setError('');

    fetch(`http://localhost:7072/api/relationships/getToUserLocationHistory?fromEmail=${encodeURIComponent(users[currentUserId].name)}&toEmail=${encodeURIComponent(user.name)}&date=${dateInput.value}`)
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then(data => {
        if (data.error) {
          throw new Error(data.error);
        }

        if (!Array.isArray(data)) {
          throw new Error('Invalid data format received');
        }

        setHistoricalData(data);
        setHistoryStatus(`Loaded ${data.length} points`);
      })
      .catch(error => {
        console.error('Error loading historical data:', error);
        setError(error.message);
        setHistoryStatus('Error loading');
      });
  };

  const clearHistoricalData = () => {
    setHistoricalData([]);
    setHistoryStatus('Not loaded');
  };

  useEffect(() => {
    connectWebSocket();

    return () => {
      if (stompClient.current && stompClient.current.connected) {
        stompClient.current.disconnect();
      }
    };
  }, []);

  useEffect(() => {
    if (trackedUserIds.length > 0) {
      sendTrackingRequests();
      checkConnectionStatus();
    }
  }, [trackedUserIds]);

  useEffect(() => {
    const inactiveCheckInterval = setInterval(checkInactiveUsers, 60000);
    const connectionCheckInterval = setInterval(checkConnectionStatus, 30000);

    return () => {
      clearInterval(inactiveCheckInterval);
      clearInterval(connectionCheckInterval);
    };
  }, [trackedUserIds, users]);

  const activeUsersCount = trackedUserIds.filter(id => users[id]?.isActive).length;
  const connectedUsersCount = trackedUserIds.filter(id => users[id]?.isConnected).length;
  const trackedUsersData = trackedUserIds.map(id => users[id]).filter(Boolean);

   return (
    <div className="location-tracker-app">
      <header className="app-header">
        <div className="header-content">
          <i className="fas fa-map-marked-alt header-icon"></i>
          <div>
            <h1 className="app-title">Real-Time Location Tracker</h1>
            <p className="app-subtitle">Monitor multiple users with live location updates</p>
          </div>
        </div>
      </header>

      <div className="app-content">
        <div className="map-section">
          <div className="map-container">
            <MapComponent trackedUsers={trackedUsersData} onUserUpdate={updateUserMap} />
          </div>
          
          <div className="control-section">
            <div className="control-panels-container">
              <div className="control-panel user-controls slide-in">
                <h2 className="panel-title">
                  <i className="fas fa-user-cog panel-icon"></i> Tracking Controls
                </h2>
                
                <div className="form-group">
                  <label className="form-label">Your User ID</label>
                  <input 
                    type="text" 
                    className="form-input" 
                    value={currentUserId} 
                    readOnly 
                  />
                </div>

                <div className="form-group">
                  <label className="form-label">Select Users to Track</label>
                  <div className="user-checkbox-group">
                    {Object.values(users).map(user => (
                      <label key={user.id} className="user-checkbox-label">
                        <input
                          type="checkbox"
                          className="user-checkbox"
                          checked={trackedUserIds.includes(user.id)}
                          onChange={(e) => handleUserSelectionChange(user.id, e.target.checked)}
                        />
                        <span className="custom-checkbox"></span>
                        <span className="user-avatar" style={{ backgroundColor: user.color }}>
                          {user.initial}
                        </span>
                        <span className="user-name">{user.name}</span>
                      </label>
                    ))}
                  </div>
                </div>
              </div>

              <div className="control-panel user-list slide-in">
                <h2 className="panel-title">
                  <i className="fas fa-users panel-icon"></i> Tracked Users
                  <span className="badge">{trackedUserIds.length}</span>
                </h2>
                
                {trackedUserIds.length === 0 ? (
                  <div className="empty-state">
                    <i className="fas fa-user-slash empty-icon"></i>
                    <p>No users being tracked</p>
                  </div>
                ) : (
                  <div className="user-list-container">
                    {trackedUserIds.map(userId => {
                      const user = users[userId];
                      if (!user) return null;
                      
                      return (
                        <div key={userId} className="user-list-item">
                          <div className="user-avatar" style={{ backgroundColor: user.color }}>
                            {user.initial}
                          </div>
                          <div className="user-info">
                            <div className="user-name">{user.name}</div>
                            <div className={`user-status ${user.isConnected ? (user.isActive ? 'active' : 'idle') : 'offline'}`}>
                              {user.isConnected ? (
                                user.isActive ? 'Online' : 'Idle'
                              ) : (
                                `Offline (${formatTimeDifference(user.lastSeen)})`
                              )}
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>

              <div className="control-panel history-panel slide-in">
                <h2 className="panel-title">
                  <i className="fas fa-history panel-icon"></i> Historical Data
                </h2>
                
                <div className="form-group">
                  <label className="form-label">Select Date</label>
                  <input 
                    type="date" 
                    id="history-date"
                    className="form-input" 
                    defaultValue={new Date().toISOString().split('T')[0]} 
                  />
                </div>
                
                <div className="form-group">
                  <label className="form-label">Select User</label>
                  <select id="history-user-select" className="form-select">
                    <option value="">Select a user</option>
                    {Object.values(users).map(user => (
                      <option key={user.id} value={user.id}>{user.name}</option>
                    ))}
                  </select>
                </div>
                
                <div className="button-group">
                  <button className="btn primary-btn" onClick={loadHistoricalData}>
                    <i className="fas fa-calendar-day"></i> Load History
                  </button>
                  <button className="btn secondary-btn" onClick={clearHistoricalData}>
                    <i className="fas fa-trash-alt"></i> Clear
                  </button>
                </div>
                
                <div className="status-indicator">
                  <span className="status-label">Status:</span>
                  <span className="status-value">{historyStatus}</span>
                </div>
              </div>

              <div className="control-panel stats-panel slide-in">
                <h2 className="panel-title">
                  <i className="fas fa-chart-line panel-icon"></i> Tracking Stats
                </h2>
                
                <div className="stats-grid">
                  <div className="stat-item">
                    <div className="stat-value">{trackedUserIds.length}</div>
                    <div className="stat-label">Tracked</div>
                  </div>
                  <div className="stat-item">
                    <div className="stat-value">{connectedUsersCount}</div>
                    <div className="stat-label">Connected</div>
                  </div>
                  <div className="stat-item">
                    <div className="stat-value">{activeUsersCount}</div>
                    <div className="stat-label">Active</div>
                  </div>
                </div>
                
                <div className="last-update">
                  <i className="fas fa-clock"></i> Last update: {lastUpdate}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {error && (
        <div className="error-alert">
          <i className="fas fa-exclamation-circle"></i> {error}
        </div>
      )}
    </div>
  );
};

export default LocationTracker;