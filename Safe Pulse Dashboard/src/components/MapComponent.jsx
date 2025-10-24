// MapComponent.js
import { useEffect, useRef } from 'react';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const MapComponent = ({ trackedUsers, onUserUpdate }) => {
  const mapRef = useRef(null);
  const mapInstance = useRef(null);
  const markers = useRef({});
  const polylines = useRef({});
  const footprints = useRef({});

  // Initialize map
  useEffect(() => {
    if (!mapRef.current) return;

    mapInstance.current = L.map(mapRef.current).setView([28.5892, 77.3176], 15);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(mapInstance.current);

    return () => {
      if (mapInstance.current) {
        mapInstance.current.remove();
        mapInstance.current = null;
      }
    };
  }, []);

  // Create online marker icon
  const createOnlineIcon = (color) => {
    return L.divIcon({
      className: 'online-marker',
      html: `<div style="background-color: ${color}; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white;"></div>`,
      iconSize: [30, 30]
    });
  };

  // Create offline marker icon
  const createOfflineIcon = (color) => {
    return L.divIcon({
      className: 'offline-marker',
      html: `<div style="background-color: ${color}; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; opacity: 0.5;"></div>`,
      iconSize: [30, 30]
    });
  };

  // Update user markers on trackedUsers change
  useEffect(() => {
    if (!mapInstance.current) return;

    // Clear all existing markers and paths
    Object.values(markers.current).forEach(marker => {
      if (marker) mapInstance.current.removeLayer(marker);
    });
    markers.current = {};

    Object.values(polylines.current).forEach(polyline => {
      if (polyline) mapInstance.current.removeLayer(polyline);
    });
    polylines.current = {};

    Object.values(footprints.current).forEach(footprintArray => {
      footprintArray.forEach(footprint => {
        if (footprint) mapInstance.current.removeLayer(footprint);
      });
    });
    footprints.current = {};

    // Add new markers and paths for tracked users
    trackedUsers.forEach(user => {
      if (!user.lastPosition) return;

      // Create marker
      markers.current[user.id] = L.marker([user.lastPosition.lat, user.lastPosition.lng], {
        icon: user.isConnected ? createOnlineIcon(user.color) : createOfflineIcon(user.color)
      }).addTo(mapInstance.current);

      // Create popup content
      const popupContent = `
        <b>${user.name}</b><br>
        <small style="color: ${user.isConnected ? (user.isActive ? '#4ad66d' : '#f8961e') : '#6c757d'};">
          ${user.isConnected ? (user.isActive ? 'Online' : 'Idle') : 'Offline'}
        </small><br>
        ${user.lastSeen ? new Date(user.lastSeen).toLocaleString() : 'Never'}<br>
        Lat: ${user.lastPosition.lat.toFixed(6)}<br>
        Lng: ${user.lastPosition.lng.toFixed(6)}
      `;

      markers.current[user.id].bindPopup(popupContent);

      // Create polyline if path exists
      if (user.path && user.path.length > 0) {
        polylines.current[user.id] = L.polyline(user.path, {
          color: user.color,
          weight: 5,
          opacity: 0.7,
          smoothFactor: 1
        }).addTo(mapInstance.current);
      }

      // Add footprints
      footprints.current[user.id] = [];
      if (user.path && user.path.length > 0) {
        user.path.forEach((point, index) => {
          if (index % 5 === 0) { // Add footprint every 5 points for performance
            const footprint = L.circleMarker(point, {
              radius: 5,
              color: user.color,
              fillColor: user.color,
              fillOpacity: 0.7,
              className: 'footprint'
            }).addTo(mapInstance.current);
            footprints.current[user.id].push(footprint);
          }
        });
      }
    });

    // Adjust map view
    if (trackedUsers.length > 0) {
      const bounds = trackedUsers
        .filter(user => user.lastPosition)
        .map(user => [user.lastPosition.lat, user.lastPosition.lng]);

      if (bounds.length > 0) {
        if (bounds.length === 1) {
          mapInstance.current.setView(bounds[0], 15);
        } else {
          mapInstance.current.fitBounds(bounds, { padding: [50, 50] });
        }
      }
    }
  }, [trackedUsers]);

  return <div ref={mapRef} className="map-container" />;
};

export default MapComponent;