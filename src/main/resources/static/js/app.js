/**
 * GovFix - Main Frontend Application Logic
 * PCMC Municipal Grievance & Configurable Workflow Management System
 */

const App = {
  activeTab: 'landing',
  currentComplaint: null,
  allOfficers: [],

  init() {
    if (window.GovFixI18n) GovFixI18n.init();
    if (window.GovFix3D) GovFix3D.init();
    this.initAccessibility();
    this.initGlobalSearch();
    this.initRoleSwitcher();
    this.initNavigation();
    this.initComplaintRegistration();
    this.initModals();
    this.initDemoControls();
    this.loadInitialData();
  },

  initAccessibility() {
    const btnDecrease = document.getElementById('btn-font-decrease');
    const btnNormal = document.getElementById('btn-font-normal');
    const btnIncrease = document.getElementById('btn-font-increase');
    const btnContrast = document.getElementById('btn-toggle-contrast');
    const btnMotion = document.getElementById('btn-toggle-motion');

    if (btnDecrease) {
      btnDecrease.addEventListener('click', () => {
        document.body.classList.remove('font-lg', 'font-xl');
      });
    }
    if (btnNormal) {
      btnNormal.addEventListener('click', () => {
        document.body.classList.remove('font-lg', 'font-xl');
      });
    }
    if (btnIncrease) {
      btnIncrease.addEventListener('click', () => {
        if (document.body.classList.contains('font-lg')) {
          document.body.classList.remove('font-lg');
          document.body.classList.add('font-xl');
        } else {
          document.body.classList.add('font-lg');
        }
      });
    }
    if (btnContrast) {
      btnContrast.addEventListener('click', () => {
        document.body.classList.toggle('high-contrast');
      });
    }
    if (btnMotion) {
      btnMotion.addEventListener('click', () => {
        const isReduced = document.body.classList.toggle('reduced-motion');
        if (window.GovFix3D) GovFix3D.setReducedMotion(isReduced);
      });
    }
  },

  initGlobalSearch() {
    const searchInput = document.getElementById('global-search-input');
    const searchBtn = document.getElementById('btn-global-search');
    const autoBox = document.getElementById('search-autocomplete');

    const searchCatalog = [
      { label: 'Property Tax (Kar) Assessment & Payment', tag: 'Revenue', action: () => window.open('https://www.pcmcindia.gov.in/', '_blank') },
      { label: 'Water Supply Pipeline Leakage & Grievance', tag: 'Grievance', action: () => this.openGrievanceWithCategory('Water supply') },
      { label: 'Roads & Pothole Repair (Akurdi, Wakad, Nigdi)', tag: 'Grievance', action: () => this.openGrievanceWithCategory('Roads and potholes') },
      { label: 'Smart Garbage Collection Van Live GPS Tracking', tag: 'Live GPS', action: () => this.switchTab('garbage-tracking') },
      { label: 'Drainage & Open Manhole Reporting', tag: 'Grievance', action: () => this.openGrievanceWithCategory('Drainage and sanitation') },
      { label: 'Streetlight Maintenance & Dark Spots', tag: 'Grievance', action: () => this.openGrievanceWithCategory('Streetlights') },
      { label: 'Building Permission & BPMS Approvals', tag: 'Town Planning', action: () => window.open('https://www.pcmcindia.gov.in/', '_blank') },
      { label: 'Public Gardens & Fallen Tree Trimming', tag: 'Grievance', action: () => this.openGrievanceWithCategory('Public gardens') },
      { label: 'Track Existing Grievance Status (GV-1001)', tag: 'Tracking', action: () => this.switchTab('track') },
      { label: 'Department Directory & Ward Offices A to H', tag: 'Directory', action: () => this.switchTab('departments') },
      { label: 'City Grievance Performance Dashboard & Analytics', tag: 'Analytics', action: () => this.switchTab('dashboard') },
      { label: 'PCMC Sarathi 24x7 Helpline & Emergency Contacts', tag: 'Contact', action: () => this.switchTab('contact') }
    ];

    if (!searchInput || !autoBox) return;

    const handleSearch = (query) => {
      query = (query || '').trim().toLowerCase();
      if (!query) {
        autoBox.classList.remove('active');
        return;
      }
      const matches = searchCatalog.filter(item => 
        item.label.toLowerCase().includes(query) || item.tag.toLowerCase().includes(query)
      );

      if (matches.length === 0) {
        autoBox.innerHTML = `
          <div style="padding:12px 18px; font-size:0.85rem; color:#64748b;">
            No services matching "<strong>${this.escapeHtml(query)}</strong>". Try "pothole", "garbage", or "tax".
          </div>
        `;
        autoBox.classList.add('active');
        return;
      }

      autoBox.innerHTML = matches.slice(0, 6).map((item, idx) => `
        <div class="search-suggestion-item" data-idx="${idx}">
          <span><strong>${item.label}</strong></span>
          <span style="font-size:0.75rem; background:#f1f5f9; padding:2px 8px; border-radius:4px; color:#475569;">${item.tag}</span>
        </div>
      `).join('');

      autoBox.querySelectorAll('.search-suggestion-item').forEach((el) => {
        el.addEventListener('click', () => {
          const idx = parseInt(el.dataset.idx, 10);
          autoBox.classList.remove('active');
          searchInput.value = '';
          matches[idx].action();
        });
      });

      autoBox.classList.add('active');
    };

    searchInput.addEventListener('input', (e) => handleSearch(e.target.value));
    if (searchBtn) {
      searchBtn.addEventListener('click', () => handleSearch(searchInput.value));
    }

    // Quick search pills
    document.querySelectorAll('.search-pill').forEach(pill => {
      pill.addEventListener('click', () => {
        const text = pill.dataset.search || pill.textContent;
        searchInput.value = text;
        handleSearch(text);
      });
    });

    // Close autocomplete on click outside
    document.addEventListener('click', (e) => {
      if (!searchInput.contains(e.target) && !autoBox.contains(e.target)) {
        autoBox.classList.remove('active');
      }
    });
  },

  openGrievanceWithCategory(category) {
    this.switchTab('citizen');
    const catSelect = document.getElementById('reg-category');
    if (catSelect) {
      catSelect.value = category;
    }
    const titleInput = document.getElementById('reg-title');
    if (titleInput) {
      titleInput.focus();
    }
  },

  initRoleSwitcher() {
    const roleSelect = document.getElementById('global-role-select');
    if (!roleSelect) return;

    roleSelect.value = API.currentRole;
    this.updateUserBadge(API.currentRole);

    roleSelect.addEventListener('change', (e) => {
      const selectedRole = e.target.value;
      const defaultUser = API.getDefaultUserForRole(selectedRole);
      API.setRole(selectedRole, defaultUser);
      this.updateUserBadge(selectedRole);
      this.refreshData();
    });
  },

  updateUserBadge(role) {
    const badge = document.getElementById('current-user-display');
    if (!badge) return;
    const usernames = {
      'CITIZEN': 'Rahul Deshmukh (Citizen)',
      'OFFICER': 'Amit Sharma (JE Road Dept)',
      'SUPERVISOR': 'Dr. Anand Kulkarni (Exec. Eng)',
      'ADMIN': 'System Administrator',
      'AUDITOR': 'Vikas Joshi (Auditor Office)'
    };
    badge.textContent = usernames[role] || role;
  },

  initNavigation() {
    const navItems = document.querySelectorAll('.gov-subnav-item');
    navItems.forEach(item => {
      item.addEventListener('click', () => {
        const targetView = item.dataset.target;
        this.switchTab(targetView);
      });
    });

    // Quick links in hero
    document.querySelectorAll('[data-nav-target]').forEach(el => {
      el.addEventListener('click', (e) => {
        e.preventDefault();
        this.switchTab(el.dataset.navTarget);
      });
    });
  },

  switchTab(tabId) {
    if (tabId === 'track') {
      this.activeTab = 'citizen';
      document.querySelectorAll('.gov-subnav-item, .gov-nav-link').forEach(item => {
        item.classList.toggle('active', item.dataset.target === 'track' || item.dataset.target === 'citizen');
      });
      document.querySelectorAll('.tab-view').forEach(view => {
        view.classList.toggle('active', view.id === 'view-citizen');
      });
      const trackInput = document.getElementById('track-ticket-input');
      if (trackInput) {
        trackInput.scrollIntoView({ behavior: 'smooth' });
        trackInput.focus();
      }
      return;
    }

    this.activeTab = tabId;

    document.querySelectorAll('.gov-subnav-item, .gov-nav-link').forEach(item => {
      item.classList.toggle('active', item.dataset.target === tabId);
    });

    document.querySelectorAll('.tab-view').forEach(view => {
      view.classList.toggle('active', view.id === `view-${tabId}`);
    });

    window.scrollTo({ top: 0, behavior: 'smooth' });

    // Special view initializations
    if (tabId === 'garbage-tracking' && window.GarbageTracker) {
      setTimeout(() => {
        GarbageTracker.init();
        if (GarbageTracker.map) GarbageTracker.map.invalidateSize();
      }, 100);
    } else if (tabId === 'dashboard') {
      this.loadDashboardData();
    } else if (tabId === 'officer') {
      this.loadOfficerWorkspace();
    } else if (tabId === 'supervisor') {
      this.loadSupervisorData();
    } else if (tabId === 'workflows') {
      this.loadWorkflowsData();
    } else if (tabId === 'audit') {
      this.loadAuditLogs();
    } else if (tabId === 'ai-assistant') {
      this.loadAiAssistantData();
    }
  },

  async loadInitialData() {
    try {
      await Promise.all([
        this.loadDashboardData(),
        this.loadDemoUsers()
      ]);
    } catch (err) {
      console.warn('Initial data load notice:', err.message);
    }
  },

  async loadDemoUsers() {
    try {
      const res = await API.getDemoUsers();
      if (res.success && res.data) {
        this.allOfficers = res.data.filter(u => u.role === 'OFFICER');
      }
    } catch (err) {
      console.error('Failed to load demo users:', err);
    }
  },

  async refreshData() {
    if (this.activeTab === 'dashboard') await this.loadDashboardData();
    else if (this.activeTab === 'officer') await this.loadOfficerWorkspace();
    else if (this.activeTab === 'supervisor') await this.loadSupervisorData();
    else if (this.activeTab === 'audit') await this.loadAuditLogs();
    else if (this.activeTab === 'ai-assistant') await this.loadAiAssistantData();
    else if (this.activeTab === 'workflows') await this.loadWorkflowsData();
  },

  // ==========================================
  // DASHBOARD & COMPLAINTS
  // ==========================================
  async loadDashboardData() {
    try {
      const [statsRes, complaintsRes] = await Promise.all([
        API.getDashboardStats(),
        this.fetchFilteredComplaints()
      ]);

      if (statsRes.success) {
        this.renderStatsKPIs(statsRes.data);
      }
      if (complaintsRes.success) {
        this.renderComplaintsTable(complaintsRes.data);
      }
    } catch (err) {
      console.error('Error loading dashboard:', err);
    }
  },

  renderStatsKPIs(stats) {
    const setEl = (id, val) => {
      const el = document.getElementById(id);
      if (el) el.textContent = val !== undefined && val !== null ? val : '0';
    };

    setEl('kpi-total', stats.totalComplaints);
    setEl('kpi-pending', stats.pendingComplaints);
    setEl('kpi-assigned', stats.assignedComplaints);
    setEl('kpi-progress', stats.inProgressComplaints);
    setEl('kpi-resolved', stats.resolvedComplaints);
    setEl('kpi-overdue', stats.overdueComplaints);
    setEl('kpi-escalated', stats.escalatedComplaints);

    // Live City Statistics Strip in Landing/Hero
    setEl('strip-total', stats.totalComplaints);
    setEl('strip-progress', stats.inProgressComplaints);
    setEl('strip-resolved', stats.resolvedComplaints);
    setEl('strip-overdue', stats.overdueComplaints);

    // Badges in subnav
    const escBadge = document.getElementById('nav-escalation-badge');
    if (escBadge) escBadge.textContent = stats.escalatedComplaints;

    // Category progress bars
    const catContainer = document.getElementById('category-breakdown-bars');
    if (catContainer && stats.categoryBreakdown) {
      const total = Math.max(1, stats.totalComplaints);
      catContainer.innerHTML = Object.entries(stats.categoryBreakdown).map(([cat, cnt]) => {
        const pct = Math.round((cnt / total) * 100);
        return `
          <div style="margin-bottom: 10px;">
            <div style="display:flex; justify-content:space-between; font-size:0.8rem; font-weight:600; margin-bottom:2px;">
              <span>${cat}</span>
              <span>${cnt} (${pct}%)</span>
            </div>
            <div style="height:6px; background:#e2e8f0; border-radius:3px; overflow:hidden;">
              <div style="width:${pct}%; height:100%; background:var(--pcmc-teal);"></div>
            </div>
          </div>
        `;
      }).join('');
    }
  },

  async fetchFilteredComplaints() {
    const status = document.getElementById('filter-status')?.value || '';
    const category = document.getElementById('filter-category')?.value || '';
    const priority = document.getElementById('filter-priority')?.value || '';
    const locality = document.getElementById('filter-locality')?.value || '';

    const params = {};
    if (status) params.status = status;
    if (category) params.category = category;
    if (priority) params.priority = priority;
    if (locality) params.locality = locality;

    return await API.getComplaints(params);
  },

  renderComplaintsTable(complaints, targetElementId = 'complaints-table-body') {
    const tbody = document.getElementById(targetElementId);
    if (!tbody) return;

    if (!complaints || complaints.length === 0) {
      tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:30px; color:#64748b;">No grievances match current filters.</td></tr>`;
      return;
    }

    tbody.innerHTML = complaints.map(c => {
      const isOverdue = c.deadlineAt && new Date(c.deadlineAt) < new Date() && !['Resolved', 'Closed', 'Rejected'].includes(c.status);
      const isEscalated = c.isEscalated;

      return `
        <tr>
          <td><strong style="color:var(--pcmc-teal);">${c.ticketId}</strong></td>
          <td>
            <div style="font-weight:700; color:var(--pcmc-navy);">${this.escapeHtml(c.title)}</div>
            <div style="font-size:0.75rem; color:#64748b;">${this.escapeHtml(c.locality)} • ${c.wardOrZone || 'PCMC'}</div>
          </td>
          <td><span style="font-size:0.8rem; font-weight:600;">${c.category}</span></td>
          <td><span class="priority-badge priority-${c.priority.toLowerCase()}">${c.priority}</span></td>
          <td>
            <span class="status-pill status-${c.status.toLowerCase().replace(/\s+/g, '-')}">${c.status}</span>
            ${isEscalated ? '<span class="status-pill status-escalated" style="margin-left:4px;">🚨 Escalated</span>' : ''}
            ${isOverdue ? '<span class="status-pill status-rejected" style="margin-left:4px;">⏱ Overdue</span>' : ''}
          </td>
          <td>
            <div style="font-size:0.8rem; font-weight:600;">${c.assignedOfficer ? c.assignedOfficer.fullName : '<span style="color:#94a3b8;">Unassigned</span>'}</div>
          </td>
          <td>
            <button class="btn-demo-action" style="padding:4px 8px;" onclick="App.openComplaintDetails(${c.id})">
              Inspect & Actions
            </button>
          </td>
        </tr>
      `;
    }).join('');
  },

  // ==========================================
  // COMPLAINT DETAILS & ACTIONS MODAL
  // ==========================================
  async openComplaintDetails(complaintId) {
    try {
      const res = await API.getComplaintById(complaintId);
      if (!res.success) return;

      const { complaint, availableTransitions } = res.data;
      this.currentComplaint = complaint;

      document.getElementById('modal-ticket-id').textContent = complaint.ticketId;
      document.getElementById('modal-ticket-status').textContent = complaint.status;
      document.getElementById('modal-ticket-status').className = `status-pill status-${complaint.status.toLowerCase().replace(/\s+/g, '-')}`;
      
      document.getElementById('modal-ticket-title').textContent = complaint.title;
      document.getElementById('modal-ticket-desc').textContent = complaint.description;
      document.getElementById('modal-ticket-category').textContent = complaint.category;
      document.getElementById('modal-ticket-dept').textContent = complaint.department;
      document.getElementById('modal-ticket-locality').textContent = `${complaint.locality} (${complaint.wardOrZone || 'PCMC'})`;
      document.getElementById('modal-ticket-priority').textContent = complaint.priority;
      document.getElementById('modal-ticket-citizen').textContent = `${complaint.citizenName} (${complaint.citizenContact})`;
      document.getElementById('modal-ticket-officer').textContent = complaint.assignedOfficer ? complaint.assignedOfficer.fullName : 'Not Assigned';
      document.getElementById('modal-ticket-deadline').textContent = complaint.deadlineAt ? new Date(complaint.deadlineAt).toLocaleString() : 'N/A';

      // Photo
      const photoEl = document.getElementById('modal-ticket-photo');
      if (complaint.photoUrl) {
        photoEl.src = complaint.photoUrl;
        photoEl.style.display = 'block';
      } else {
        photoEl.style.display = 'none';
      }

      // Evidence list
      const evidenceBox = document.getElementById('modal-ticket-evidence');
      if (complaint.evidenceList && complaint.evidenceList.length > 0) {
        evidenceBox.innerHTML = complaint.evidenceList.map(ev => `
          <div style="background:#f1f5f9; padding:8px 12px; border-radius:6px; margin-bottom:6px; font-size:0.8rem;">
            <strong>${ev.evidenceType}:</strong> ${ev.fileName} <br/>
            <em>Notes:</em> ${ev.notes || 'None'} <br/>
            <a href="${ev.filePath}" target="_blank" style="color:var(--pcmc-blue); text-decoration:underline;">View Evidence File</a>
          </div>
        `).join('');
      } else {
        evidenceBox.innerHTML = '<span style="color:#94a3b8; font-size:0.85rem;">No inspection/resolution evidence attached yet.</span>';
      }

      // Render Permitted Transitions Buttons
      this.renderTransitionButtons(availableTransitions);

      // Load timeline inside modal
      await this.loadModalTimeline(complaint.id);

      document.getElementById('modal-complaint-details').classList.add('active');
    } catch (err) {
      alert('Error opening details: ' + err.message);
    }
  },

  renderTransitionButtons(availableTransitions) {
    const container = document.getElementById('modal-permitted-transitions');
    if (!container) return;

    if (!availableTransitions || availableTransitions.length === 0) {
      container.innerHTML = `
        <span style="font-size:0.85rem; color:#64748b;">
          No standard transitions configured from current state '${this.currentComplaint.status}'.
        </span>
      `;
      return;
    }

    container.innerHTML = availableTransitions.map(t => `
      <button class="btn-primary" style="margin:4px;" onclick="App.triggerTransition('${t.toStateKey}', '${t.actionName}', ${t.requiresNotes}, ${t.requiresEvidence})">
        ${t.actionName} → ${t.toStateKey}
      </button>
    `).join('') + `
      <button class="btn-demo-action" style="margin:4px; border-color:#ef4444; color:#ef4444;" onclick="App.triggerInvalidTransitionDemo()">
        ⚠️ Demo: Attempt Invalid Transition (To Show Rejection & Audit Log)
      </button>
    `;
  },

  async triggerTransition(targetState, actionName, requiresNotes, requiresEvidence) {
    let notes = '';
    if (requiresNotes) {
      notes = prompt(`Action '${actionName}' requires notes. Please enter inspection/action notes:`);
      if (notes === null) return;
      if (!notes.trim()) {
        alert('Notes are mandatory for this transition!');
        return;
      }
    } else {
      notes = `Transition to ${targetState} by ${API.currentUser}`;
    }

    let evidenceUrl = null;
    if (requiresEvidence) {
      evidenceUrl = prompt('This step requires evidence! Enter a photo URL or press OK to use demo evidence photo:', 'https://images.unsplash.com/photo-1584467735871-8e85353a8413?w=600');
      if (evidenceUrl === null) return;
    }

    try {
      const res = await API.executeTransition(this.currentComplaint.id, {
        targetState,
        notes,
        evidenceUrl,
        evidenceType: 'AFTER_PHOTO'
      });

      alert(`Success! Ticket state transitioned to '${res.data.status}'`);
      this.openComplaintDetails(this.currentComplaint.id);
      this.refreshData();
    } catch (err) {
      alert('Transition Rejected: ' + err.message);
      this.loadModalTimeline(this.currentComplaint.id);
    }
  },

  async triggerInvalidTransitionDemo() {
    try {
      // Attempt an illegal jump directly to Resolved
      await API.executeTransition(this.currentComplaint.id, {
        targetState: 'Resolved',
        notes: 'Testing invalid skip jump from demo UI'
      });
      alert('Unexpected success?');
    } catch (err) {
      alert(`[BACKEND ENFORCED REJECTION]\n${err.message}\n\nNotice: This rejected attempt has been logged in the append-only Audit Trail!`);
      await this.loadModalTimeline(this.currentComplaint.id);
    }
  },

  async loadModalTimeline(complaintId) {
    const listEl = document.getElementById('modal-timeline-list');
    if (!listEl) return;

    try {
      const res = await API.getTimeline(complaintId);
      if (res.success && res.data) {
        listEl.innerHTML = res.data.map(event => `
          <li class="timeline-step ${event.outcome === 'REJECTED' ? 'rejected' : ''}">
            <div class="timeline-step-header">
              <span>${event.action}</span>
              <span class="status-pill ${event.outcome === 'REJECTED' ? 'status-rejected' : 'status-resolved'}">
                ${event.outcome}
              </span>
            </div>
            <div class="timeline-step-meta">
              Actor: <strong>${event.actorUsername}</strong> (${event.actorRole}) • ${new Date(event.timestamp).toLocaleString()}
            </div>
            <div class="timeline-step-content">
              ${event.reason ? this.escapeHtml(event.reason) : ''}
              ${event.previousState && event.requestedState ? `<br/><span style="color:#64748b;">[${event.previousState} → ${event.requestedState}]</span>` : ''}
            </div>
          </li>
        `).join('');
      }
    } catch (err) {
      listEl.innerHTML = `<li>Error loading timeline</li>`;
    }
  },

  // ==========================================
  // CITIZEN PORTAL: REGISTRATION & TRACKING
  // ==========================================
  initComplaintRegistration() {
    const form = document.getElementById('form-register-complaint');
    if (form) {
      form.addEventListener('submit', async (e) => {
        e.preventDefault();
        const data = {
          title: document.getElementById('reg-title').value,
          description: document.getElementById('reg-desc').value,
          category: document.getElementById('reg-category').value,
          locality: document.getElementById('reg-locality').value,
          wardOrZone: document.getElementById('reg-ward').value,
          landmark: document.getElementById('reg-landmark').value,
          priority: document.getElementById('reg-priority').value,
          citizenName: document.getElementById('reg-citizen-name').value,
          citizenContact: document.getElementById('reg-citizen-contact').value,
          photoUrl: document.getElementById('reg-photo-url').value
        };

        try {
          const res = await API.createComplaint(data);
          alert(`Grievance Registered Successfully!\nYour Unique Ticket ID: ${res.data.ticketId}\nSave this ticket ID to track your complaint status.`);
          form.reset();
          document.getElementById('track-ticket-input').value = res.data.ticketId;
          this.trackTicket(res.data.ticketId);
          this.refreshData();
        } catch (err) {
          alert('Submission failed: ' + err.message);
        }
      });
    }

    // AI Categorization Helper button in Form
    const btnAiSuggest = document.getElementById('btn-form-ai-suggest');
    if (btnAiSuggest) {
      btnAiSuggest.addEventListener('click', async () => {
        const title = document.getElementById('reg-title').value;
        const description = document.getElementById('reg-desc').value;
        if (!title && !description) {
          alert('Please enter a brief complaint title or description first so AI can analyze it.');
          return;
        }

        btnAiSuggest.textContent = '✨ Analyzing with AI...';
        try {
          const res = await API.suggestCategory({ title, description });
          if (res.success && res.data) {
            const sug = res.data;
            document.getElementById('reg-category').value = sug.suggestedCategory;
            document.getElementById('reg-priority').value = sug.suggestedPriority;
            alert(`[AI Recommendation]\nCategory: ${sug.suggestedCategory}\nDepartment: ${sug.suggestedDepartment}\nPriority: ${sug.suggestedPriority}\nConfidence: ${(sug.confidenceScore * 100).toFixed(0)}%\n\nExplanation: ${sug.explanation}`);
          }
        } catch (err) {
          alert('AI suggestion error: ' + err.message);
        } finally {
          btnAiSuggest.textContent = '✨ Auto-Categorize with AI';
        }
      });
    }

    // Citizen Ticket Tracking Input
    const btnTrack = document.getElementById('btn-track-ticket');
    if (btnTrack) {
      btnTrack.addEventListener('click', () => {
        const ticketId = document.getElementById('track-ticket-input').value.trim();
        if (ticketId) this.trackTicket(ticketId);
      });
    }
  },

  async trackTicket(ticketId) {
    const displayBox = document.getElementById('ticket-tracking-result');
    if (!displayBox) return;

    try {
      const res = await API.trackComplaint(ticketId);
      if (!res.success || !res.data) {
        displayBox.innerHTML = `<div class="gov-card" style="color:#ef4444;">Ticket '${ticketId}' not found. Please verify the ticket ID.</div>`;
        return;
      }

      const c = res.data;
      const timelineRes = await API.getTimeline(c.id);
      const timeline = timelineRes.data || [];

      displayBox.innerHTML = `
        <div class="gov-card">
          <div class="gov-card-header">
            <div>
              <span class="status-pill status-${c.status.toLowerCase().replace(/\s+/g, '-')}">${c.status}</span>
              <h3 style="font-size:1.3rem; margin-top:6px; color:var(--pcmc-navy);">${this.escapeHtml(c.title)}</h3>
              <div style="font-size:0.8rem; color:#64748b;">Ticket: <strong>${c.ticketId}</strong> | Category: <strong>${c.category}</strong></div>
            </div>
            <div style="text-align:right;">
              <div style="font-size:0.75rem; color:#64748b;">Current Stage Deadline</div>
              <div style="font-size:0.9rem; font-weight:700; color:${new Date(c.deadlineAt) < new Date() ? '#ef4444' : '#0f766e'};">
                ${c.deadlineAt ? new Date(c.deadlineAt).toLocaleString() : 'N/A'}
              </div>
            </div>
          </div>

          <div style="margin-bottom:20px; font-size:0.9rem; line-height:1.6;">
            <p><strong>Description:</strong> ${this.escapeHtml(c.description)}</p>
            <p><strong>Locality:</strong> ${this.escapeHtml(c.locality)} (${c.wardOrZone || 'PCMC Ward'})</p>
            <p><strong>Assigned Officer:</strong> ${c.assignedOfficer ? c.assignedOfficer.fullName : 'Pending Allocation'}</p>
          </div>

          <h4 style="font-size:1rem; font-weight:700; margin-bottom:14px; color:var(--pcmc-navy);">Grievance Resolution Lifecycle</h4>
          <ul class="timeline-stepper">
            ${timeline.map(t => `
              <li class="timeline-step ${t.outcome === 'REJECTED' ? 'rejected' : ''}">
                <div class="timeline-step-header">
                  <span>${t.action}</span>
                  <span class="status-pill ${t.outcome === 'REJECTED' ? 'status-rejected' : 'status-resolved'}">${t.outcome}</span>
                </div>
                <div class="timeline-step-meta">${new Date(t.timestamp).toLocaleString()} • ${t.actorRole}</div>
                <div class="timeline-step-content">${t.reason ? this.escapeHtml(t.reason) : ''}</div>
              </li>
            `).join('')}
          </ul>

          ${c.status === 'Resolved' || c.status === 'Closed' ? `
            <div style="margin-top:20px; padding:16px; background:#f0fdf4; border-radius:8px; border:1px solid #bbf7d0;">
              <h5 style="font-weight:700; color:#166534; margin-bottom:8px;">Citizen Satisfaction & Feedback</h5>
              <p style="font-size:0.85rem; margin-bottom:10px;">Are you satisfied with the work done by PCMC?</p>
              <div style="display:flex; gap:10px;">
                <button class="btn-primary" onclick="App.openFeedbackModal(${c.id})">⭐ Rate & Give Feedback</button>
                <button class="btn-demo-action" style="color:#b91c1c; border-color:#f87171;" onclick="App.openReopenModal(${c.id})">🔄 Not Satisfied? Request Reopen</button>
              </div>
            </div>
          ` : ''}
        </div>
      `;
    } catch (err) {
      displayBox.innerHTML = `<div class="gov-card" style="color:#ef4444;">Error looking up ticket: ${err.message}</div>`;
    }
  },

  openFeedbackModal(complaintId) {
    const rating = prompt('Enter rating from 1 to 5 stars (e.g. 5):', '5');
    if (!rating) return;
    const comments = prompt('Enter your comments on the work quality:');
    API.submitFeedback(complaintId, { rating: parseInt(rating, 10), comments: comments || 'Good service' })
      .then(res => {
        alert('Thank you! Your feedback has been recorded.');
        this.trackTicket(res.data.ticketId);
      })
      .catch(err => alert('Error: ' + err.message));
  },

  openReopenModal(complaintId) {
    const reason = prompt('Please explain why this grievance requires reopening:');
    if (!reason) return;
    API.requestReopen(complaintId, { reason })
      .then(res => {
        alert('Reopen request logged. Complaint status set to Reopened for review.');
        this.trackTicket(res.data.ticketId);
        this.refreshData();
      })
      .catch(err => alert('Error: ' + err.message));
  },

  // ==========================================
  // OFFICER WORKSPACE
  // ==========================================
  async loadOfficerWorkspace() {
    try {
      const res = await API.getComplaints();
      if (!res.success) return;

      // Filter to officer assigned cases or in-progress cases
      const officerCases = res.data.filter(c => 
        (c.assignedOfficer && c.assignedOfficer.username === API.currentUser) ||
        ['Assigned', 'Inspection Required', 'In Progress'].includes(c.status)
      );

      this.renderComplaintsTable(officerCases, 'officer-table-body');
    } catch (err) {
      console.error('Error loading officer workspace:', err);
    }
  },

  // ==========================================
  // SUPERVISOR DASHBOARD & ESCALATIONS
  // ==========================================
  async loadSupervisorData() {
    try {
      const [escRes, complaintsRes] = await Promise.all([
        API.getEscalations(),
        API.getComplaints()
      ]);

      const tableBody = document.getElementById('supervisor-table-body');
      if (!tableBody) return;

      const escalatedComplaints = complaintsRes.data.filter(c => c.isEscalated || ['Escalated', 'Under Review'].includes(c.status));

      if (escalatedComplaints.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:20px;">No overdue or escalated cases requiring review.</td></tr>`;
        return;
      }

      tableBody.innerHTML = escalatedComplaints.map(c => `
        <tr>
          <td><strong style="color:#b91c1c;">${c.ticketId}</strong></td>
          <td>
            <div style="font-weight:700;">${this.escapeHtml(c.title)}</div>
            <div style="font-size:0.75rem; color:#64748b;">${c.locality}</div>
          </td>
          <td><span class="status-pill status-${c.status.toLowerCase().replace(/\s+/g, '-')}">${c.status}</span></td>
          <td><span class="status-pill status-escalated">🚨 Escalated</span></td>
          <td>${c.assignedOfficer ? c.assignedOfficer.fullName : '<span style="color:#94a3b8;">Unassigned</span>'}</td>
          <td>
            <button class="btn-demo-action" onclick="App.openReassignModal(${c.id})">Reassign</button>
            <button class="btn-primary" style="padding:4px 8px; font-size:0.75rem;" onclick="App.openComplaintDetails(${c.id})">Review & Transition</button>
          </td>
        </tr>
      `).join('');
    } catch (err) {
      console.error('Error loading supervisor dashboard:', err);
    }
  },

  openReassignModal(complaintId) {
    if (this.allOfficers.length === 0) {
      alert('Loading officers list...');
      return;
    }

    const officerNames = this.allOfficers.map(o => `${o.id}: ${o.fullName} (${o.department})`).join('\n');
    const officerId = prompt(`Select Officer ID to reassign:\n\n${officerNames}`, this.allOfficers[0].id);
    if (!officerId) return;

    API.assignOfficer(complaintId, { officerId: parseInt(officerId, 10), notes: 'Reassigned by supervisor' })
      .then(res => {
        alert('Grievance reassigned successfully!');
        this.loadSupervisorData();
        this.refreshData();
      })
      .catch(err => alert('Reassignment error: ' + err.message));
  },

  // ==========================================
  // WORKFLOW BUILDER
  // ==========================================
  async loadWorkflowsData() {
    try {
      const res = await API.getWorkflows();
      if (!res.success) return;

      const container = document.getElementById('workflow-list-container');
      if (!container) return;

      container.innerHTML = res.data.map(wf => `
        <div class="gov-card" style="margin-bottom:20px;">
          <div class="gov-card-header">
            <div>
              <h3 style="font-size:1.15rem; color:var(--pcmc-navy);">${this.escapeHtml(wf.name)}</h3>
              <div style="font-size:0.8rem; color:#64748b;">Key: <code>${wf.workflowKey}</code> | Pattern: <code>${wf.categoryPattern || 'Any'}</code></div>
            </div>
            <span class="status-pill status-closed">${wf.isActive ? 'Active' : 'Draft'}</span>
          </div>
          <p style="font-size:0.85rem; color:#475569; margin-bottom:14px;">${this.escapeHtml(wf.description || '')}</p>

          <h5 style="font-size:0.85rem; font-weight:700; text-transform:uppercase; color:#64748b; margin-bottom:8px;">Configured Stages</h5>
          <div style="display:flex; flex-wrap:wrap; gap:8px; margin-bottom:16px;">
            ${(wf.states || []).map(s => `
              <div style="background:${s.colorCode}15; border:1px solid ${s.colorCode}; color:${s.colorCode}; padding:4px 10px; border-radius:20px; font-size:0.75rem; font-weight:700;">
                #${s.stageOrder} ${s.displayName} (${s.deadlineHours}h SLA)
              </div>
            `).join(' → ')}
          </div>

          <h5 style="font-size:0.85rem; font-weight:700; text-transform:uppercase; color:#64748b; margin-bottom:8px;">Configured State Transitions</h5>
          <div class="gov-table-wrapper">
            <table class="gov-table" style="font-size:0.8rem;">
              <thead>
                <tr>
                  <th>From State</th>
                  <th>Action</th>
                  <th>To State</th>
                  <th>Allowed Roles</th>
                  <th>Notes Req</th>
                  <th>Evidence Req</th>
                  <th>Approval Req</th>
                </tr>
              </thead>
              <tbody>
                ${(wf.transitions || []).map(t => `
                  <tr>
                    <td><strong>${t.fromStateKey}</strong></td>
                    <td style="color:var(--pcmc-teal); font-weight:600;">${t.actionName}</td>
                    <td><strong>${t.toStateKey}</strong></td>
                    <td><span style="background:#e2e8f0; padding:2px 6px; border-radius:4px;">${t.allowedRoles}</span></td>
                    <td>${t.requiresNotes ? '✓' : '-'}</td>
                    <td>${t.requiresEvidence ? '✓' : '-'}</td>
                    <td>${t.requiresApproval ? '✓' : '-'}</td>
                  </tr>
                `).join('')}
              </tbody>
            </table>
          </div>
        </div>
      `).join('');
    } catch (err) {
      console.error('Error loading workflows:', err);
    }
  },

  // ==========================================
  // AUDIT LOGS VIEW
  // ==========================================
  async loadAuditLogs() {
    const filterTicket = document.getElementById('audit-ticket-filter')?.value || '';
    const filterOutcome = document.getElementById('audit-outcome-filter')?.value || '';
    const tableBody = document.getElementById('audit-table-body');
    if (!tableBody) return;

    try {
      const res = await API.getAuditLogs(filterTicket);
      if (!res.success) return;

      let logs = res.data || [];
      if (filterOutcome) {
        logs = logs.filter(l => l.outcome === filterOutcome);
      }

      if (logs.length === 0) {
        tableBody.innerHTML = `<tr><td colspan="7" style="text-align:center; padding:30px;">No audit records found matching criteria.</td></tr>`;
        return;
      }

      tableBody.innerHTML = logs.map(a => {
        const isRejected = a.outcome === 'REJECTED';
        return `
          <tr style="${isRejected ? 'background-color:#fff1f2;' : ''}">
            <td style="font-size:0.75rem; white-space:nowrap;">${new Date(a.timestamp).toLocaleString()}</td>
            <td>
              <strong>${this.escapeHtml(a.actorUsername)}</strong>
              <div style="font-size:0.75rem; color:#64748b;">${a.actorRole}</div>
            </td>
            <td><strong style="color:var(--pcmc-teal);">${a.ticketId || '-'}</strong></td>
            <td><strong>${a.action}</strong></td>
            <td style="font-size:0.8rem;">
              ${a.previousState ? a.previousState + ' → ' : ''}${a.requestedState || '-'}
            </td>
            <td>
              <span class="status-pill ${isRejected ? 'status-rejected' : 'status-resolved'}">
                ${a.outcome}
              </span>
            </td>
            <td style="font-size:0.8rem; color:${isRejected ? '#b91c1c' : '#334155'};">
              ${this.escapeHtml(a.reason || '')}
            </td>
          </tr>
        `;
      }).join('');
    } catch (err) {
      console.error('Error loading audit logs:', err);
    }
  },

  // ==========================================
  // AI ASSISTANT VIEW
  // ==========================================
  async loadAiAssistantData() {
    const btnTest = document.getElementById('btn-test-ai');
    if (btnTest && !btnTest.dataset.bound) {
      btnTest.dataset.bound = 'true';
      btnTest.addEventListener('click', async () => {
        const text = document.getElementById('ai-test-text').value;
        if (!text) return;
        try {
          const res = await API.suggestCategory({ title: text, description: text });
          const out = document.getElementById('ai-test-output');
          if (res.success && out) {
            const sug = res.data;
            out.innerHTML = `
              <div style="padding:14px; background:#f8fafc; border-radius:8px; border:1px solid #e2e8f0;">
                <div style="display:flex; justify-content:space-between; margin-bottom:8px;">
                  <strong style="color:var(--pcmc-navy); font-size:1rem;">Recommended: ${sug.suggestedCategory}</strong>
                  <span style="color:#059669; font-weight:bold;">${(sug.confidenceScore * 100).toFixed(0)}% Confidence</span>
                </div>
                <div style="font-size:0.85rem; color:#475569; margin-bottom:6px;">
                  Responsible Department: <strong>${sug.suggestedDepartment}</strong> | Priority: <strong>${sug.suggestedPriority}</strong>
                </div>
                <p style="font-size:0.85rem; color:#64748b;">${sug.explanation}</p>
              </div>
            `;
          }
        } catch (err) {
          alert('AI error: ' + err.message);
        }
      });
    }

    // Load pending AI decisions queue
    try {
      const res = await API.getPendingAiRecommendations();
      const container = document.getElementById('ai-pending-queue');
      if (!container) return;

      if (!res.success || !res.data || res.data.length === 0) {
        container.innerHTML = '<div style="color:#64748b; font-size:0.9rem; padding:16px;">No pending AI recommendations awaiting human approval.</div>';
        return;
      }

      container.innerHTML = res.data.map(rec => `
        <div class="gov-card" style="margin-bottom:14px; border-left:4px solid var(--pcmc-purple);">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
            <strong style="color:var(--pcmc-navy);">Ticket: ${rec.complaint ? rec.complaint.ticketId : 'Linked Case'}</strong>
            <span class="status-pill status-under-review">Pending Human Review</span>
          </div>
          <div style="font-size:0.85rem; margin-bottom:10px;">
            AI Suggestion: <strong>${rec.suggestedCategory}</strong> • Dept: <strong>${rec.suggestedDepartment}</strong> • Priority: <strong>${rec.suggestedPriority}</strong>
            <p style="font-size:0.8rem; color:#64748b; margin-top:4px;">${this.escapeHtml(rec.explanation || '')}</p>
          </div>
          <div style="display:flex; gap:8px;">
            <button class="btn-primary" style="padding:6px 14px; font-size:0.8rem;" onclick="App.handleAiDecision(${rec.id}, 'APPROVE')">
              ✓ Approve & Apply
            </button>
            <button class="btn-demo-action" style="padding:6px 14px; font-size:0.8rem;" onclick="App.handleAiDecision(${rec.id}, 'EDIT')">
              ✎ Edit & Apply
            </button>
            <button class="btn-demo-action" style="padding:6px 14px; font-size:0.8rem; color:#b91c1c; border-color:#f87171;" onclick="App.handleAiDecision(${rec.id}, 'REJECT')">
              ✕ Reject
            </button>
          </div>
        </div>
      `).join('');
    } catch (err) {
      console.error('Error loading pending AI recommendations:', err);
    }
  },

  async handleAiDecision(recId, decision) {
    let payload = { recommendationId: recId, decision };
    if (decision === 'EDIT') {
      const cat = prompt('Enter new Category:', 'Roads and potholes');
      if (!cat) return;
      const dept = prompt('Enter Department:', 'Roads and Infrastructure');
      const priority = prompt('Enter Priority (LOW, MEDIUM, HIGH, CRITICAL):', 'HIGH');
      payload.editedCategory = cat;
      payload.editedDepartment = dept;
      payload.editedPriority = priority;
    }

    try {
      await API.submitAiDecision(payload);
      alert(`Decision recorded: ${decision}`);
      this.loadAiAssistantData();
      this.refreshData();
    } catch (err) {
      alert('Error applying decision: ' + err.message);
    }
  },

  // ==========================================
  // DEMO CONTROLS (ADVANCE TIME & RESET)
  // ==========================================
  initDemoControls() {
    const btnAdvance = document.getElementById('btn-advance-time');
    if (btnAdvance) {
      btnAdvance.addEventListener('click', async () => {
        const hours = prompt('Advance simulated time by how many hours? [Default: 24]', '24');
        if (!hours) return;

        btnAdvance.textContent = '⏳ Advancing Clock...';
        try {
          const res = await API.advanceDemoTime(parseInt(hours, 10));
          alert(`[SIMULATION COMPLETED]\nAdvanced time by ${hours} hours.\nNew escalations triggered: ${res.data.newEscalationsTriggered}\nCheck Supervisor Dashboard or Audit Logs to see escalated complaints.`);
          this.refreshData();
        } catch (err) {
          alert('Time advance error: ' + err.message);
        } finally {
          btnAdvance.textContent = '⏱ Simulate Time Advance (+24h)';
        }
      });
    }

    const btnReset = document.getElementById('btn-reset-demo');
    if (btnReset) {
      btnReset.addEventListener('click', async () => {
        if (!confirm('Are you sure you want to completely reset the demo to pristine seed state?')) return;
        btnReset.textContent = '🔄 Resetting...';
        try {
          await API.resetDemo();
          alert('Demo data successfully restored to initial pristine state!');
          location.reload();
        } catch (err) {
          alert('Reset error: ' + err.message);
          btnReset.textContent = '🔄 Reset Demo';
        }
      });
    }
  },

  initModals() {
    document.querySelectorAll('.modal-close-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.modal-backdrop').forEach(m => m.classList.remove('active'));
      });
    });

    document.querySelectorAll('.modal-backdrop').forEach(backdrop => {
      backdrop.addEventListener('click', (e) => {
        if (e.target === backdrop) backdrop.classList.remove('active');
      });
    });
  },

  escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  }
};

document.addEventListener('DOMContentLoaded', () => {
  window.App = App;
  App.init();
});

