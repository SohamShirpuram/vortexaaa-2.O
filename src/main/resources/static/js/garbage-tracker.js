/**
 * GovFix - Smart Garbage Collection Van Live Tracking
 * Featured PCMC Improvement: GPS Map, Dynamic ETA, Route Polylines, Missed Pickup Reporting
 */

const GarbageTracker = {
  map: null,
  vansData: [],
  activeVanIndex: 0,
  markers: {},
  routeLines: {},
  stopMarkers: [],
  autoSimInterval: null,
  isAutoSimulating: false,

  init() {
    this.initMap();
    this.loadVansData();
    this.bindEvents();
  },

  initMap() {
    if (this.map) return;

    // Centered on Pimpri Chinchwad Municipal Area (PCMC)
    this.map = L.map('garbage-map').setView([18.6298, 73.7997], 12);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors | PCMC GovFix Demo'
    }).addTo(this.map);
  },

  async loadVansData() {
    try {
      const res = await API.getGarbageVans();
      if (res.success && res.data) {
        this.vansData = res.data;
        this.renderVansSelector();
        this.renderActiveVan();
      }
    } catch (err) {
      console.error('Failed to load garbage vans:', err);
    }
  },

  renderVansSelector() {
    const selector = document.getElementById('van-zone-select');
    if (!selector) return;

    selector.innerHTML = this.vansData.map((van, idx) => `
      <option value="${idx}" ${idx === this.activeVanIndex ? 'selected' : ''}>
        ${van.vehicleNumber} (${van.assignedZone})
      </option>
    `).join('');
  },

  renderActiveVan() {
    if (!this.vansData || this.vansData.length === 0) return;
    const van = this.vansData[this.activeVanIndex];
    if (!van) return;

    // Update Details Cards safely
    const setSafe = (id, val) => {
      const el = document.getElementById(id);
      if (el) el.textContent = val;
    };

    setSafe('van-number-display', van.vehicleNumber);
    setSafe('van-driver-display', van.driverName + ' (' + van.contactPhone + ')');
    setSafe('van-zone-display', van.assignedZone);
    setSafe('van-speed-display', van.speedKmph.toFixed(1) + ' km/h');
    setSafe('van-eta-display', van.nextStopEtaMinutes + ' mins');
    setSafe('van-next-stop-display', van.nextStopLocality);
    
    const timeStr = van.lastUpdatedAt ? new Date(van.lastUpdatedAt).toLocaleTimeString() : new Date().toLocaleTimeString();
    setSafe('van-last-update', timeStr);

    // Clear old stops & routes
    this.stopMarkers.forEach(m => this.map.removeLayer(m));
    this.stopMarkers = [];
    if (this.routeLines[van.id]) {
      this.map.removeLayer(this.routeLines[van.id]);
    }

    // Draw Route Polyline & Stops
    const waypoints = [];
    if (van.stops && van.stops.length > 0) {
      van.stops.forEach((stop, index) => {
        waypoints.push([stop.latitude, stop.longitude]);

        // Stop circle marker
        const isDone = stop.isCompleted;
        const color = isDone ? '#10b981' : (index === 0 || !van.stops[index-1]?.isCompleted ? '#f59e0b' : '#64748b');

        const stopMarker = L.circleMarker([stop.latitude, stop.longitude], {
          radius: 8,
          fillColor: color,
          color: '#ffffff',
          weight: 2,
          opacity: 1,
          fillOpacity: 0.9
        }).addTo(this.map);

        stopMarker.bindPopup(`
          <div style="font-size: 0.85rem;">
            <strong>Stop ${stop.stopOrder}: ${stop.localityName}</strong><br/>
            Scheduled: ${stop.scheduledArrivalTime}<br/>
            Status: ${isDone ? '<span style="color:#10b981;font-weight:bold;">Completed</span>' : 'Est. ETA: <strong>' + stop.estimatedArrivalMinutes + ' mins</strong>'}
          </div>
        `);
        this.stopMarkers.push(stopMarker);
      });

      // Route line
      this.routeLines[van.id] = L.polyline(waypoints, {
        color: '#0d9488',
        weight: 4,
        opacity: 0.7,
        dashArray: '8, 8'
      }).addTo(this.map);
    }

    // Vehicle custom Icon
    const vanIcon = L.divIcon({
      className: 'custom-van-marker',
      html: `
        <div style="background-color:#0f2942;color:#ffffff;padding:6px;border-radius:50%;width:34px;height:34px;display:flex;align-items:center;justify-content:center;box-shadow:0 2px 6px rgba(0,0,0,0.3);border:2px solid #14b8a6;transform:rotate(${van.heading || 0}deg);">
          🚛
        </div>
      `,
      iconSize: [34, 34],
      iconAnchor: [17, 17]
    });

    if (this.markers[van.id]) {
      this.markers[van.id].setLatLng([van.currentLatitude, van.currentLongitude]);
      this.markers[van.id].setIcon(vanIcon);
    } else {
      this.markers[van.id] = L.marker([van.currentLatitude, van.currentLongitude], { icon: vanIcon }).addTo(this.map);
    }

    this.markers[van.id].bindPopup(`
      <div style="font-size: 0.85rem;">
        <strong>Vehicle: ${van.vehicleNumber}</strong><br/>
        Driver: ${van.driverName}<br/>
        Speed: ${van.speedKmph.toFixed(1)} km/h<br/>
        Next Locality: ${van.nextStopLocality} (ETA: ${van.nextStopEtaMinutes} min)
      </div>
    `);

    // Pan map to current vehicle
    this.map.panTo([van.currentLatitude, van.currentLongitude]);

    // Render stops horizontal cards
    this.renderStopsTimeline(van);
  },

  renderStopsTimeline(van) {
    const container = document.getElementById('van-stops-container');
    if (!container || !van.stops) return;

    container.innerHTML = van.stops.map(stop => `
      <li class="van-stop-card ${stop.isCompleted ? 'completed' : (stop.localityName === van.nextStopLocality ? 'active-next' : '')}">
        <div style="font-size: 0.75rem; font-weight: bold; color: var(--text-muted);">
          Stop #${stop.stopOrder} • ${stop.scheduledArrivalTime}
        </div>
        <div style="font-size: 0.85rem; font-weight: 700; color: var(--pcmc-navy); margin: 4px 0;">
          ${stop.localityName}
        </div>
        <div style="font-size: 0.75rem;">
          ${stop.isCompleted
            ? '<span style="color:#059669; font-weight:bold;">✓ Collected</span>'
            : '<span style="color:#d97706; font-weight:bold;">⏱ ETA ' + stop.estimatedArrivalMinutes + 'm</span>'}
        </div>
      </li>
    `).join('');
  },

  async triggerTick() {
    try {
      const res = await API.simulateGpsTick();
      if (res.success && res.data) {
        this.vansData = res.data;
        this.renderActiveVan();
      }
    } catch (err) {
      console.error('Tick error:', err);
    }
  },

  toggleAutoSimulation() {
    const btn = document.getElementById('btn-auto-sim');
    if (this.isAutoSimulating) {
      clearInterval(this.autoSimInterval);
      this.isAutoSimulating = false;
      if (btn) {
        btn.textContent = '▶ Start Auto-Tracking (3s)';
        btn.classList.remove('btn-urgent');
      }
    } else {
      this.isAutoSimulating = true;
      this.autoSimInterval = setInterval(() => this.triggerTick(), 3000);
      if (btn) {
        btn.textContent = '⏸ Pause Tracking';
        btn.classList.add('btn-urgent');
      }
    }
  },

  bindEvents() {
    const selector = document.getElementById('van-zone-select');
    if (selector) {
      selector.addEventListener('change', (e) => {
        this.activeVanIndex = parseInt(e.target.value, 10);
        this.renderActiveVan();
      });
    }

    const btnStep = document.getElementById('btn-step-gps');
    if (btnStep) {
      btnStep.addEventListener('click', () => this.triggerTick());
    }

    const btnAuto = document.getElementById('btn-auto-sim');
    if (btnAuto) {
      btnAuto.addEventListener('click', () => this.toggleAutoSimulation());
    }

    const btnReport = document.getElementById('btn-open-missed-report');
    if (btnReport) {
      btnReport.addEventListener('click', () => {
        const van = this.vansData[this.activeVanIndex];
        if (van) {
          document.getElementById('missed-van-number').value = van.vehicleNumber;
          document.getElementById('missed-locality').value = van.nextStopLocality || 'Nigdi';
        }
        document.getElementById('modal-missed-collection').classList.add('active');
      });
    }

    const formMissed = document.getElementById('form-missed-collection');
    if (formMissed) {
      formMissed.addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = {
          vehicleNumber: document.getElementById('missed-van-number').value,
          locality: document.getElementById('missed-locality').value,
          landmark: document.getElementById('missed-landmark').value,
          citizenName: document.getElementById('missed-citizen-name').value,
          citizenContact: document.getElementById('missed-citizen-contact').value,
          notes: document.getElementById('missed-notes').value
        };

        try {
          const res = await API.reportMissedPickup(data);
          alert('Missed Collection Grievance Registered! Ticket ID: ' + res.data.ticketId);
          document.getElementById('modal-missed-collection').classList.remove('active');
          formMissed.reset();
          if (window.App) window.App.refreshData();
        } catch (err) {
          alert('Error submitting report: ' + err.message);
        }
      });
    }
  }
};

